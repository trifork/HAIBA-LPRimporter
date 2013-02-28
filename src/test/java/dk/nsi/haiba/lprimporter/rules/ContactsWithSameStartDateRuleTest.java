/**
 * The MIT License
 *
 * Original work sponsored and donated by National Board of e-Health (NSI), Denmark
 * (http://www.nsi.dk)
 *
 * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dk.nsi.haiba.lprimporter.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import dk.nsi.haiba.lprimporter.config.LPRTestConfiguration;
import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ContactsWithSameStartDateRuleTest {

	@Configuration
    @Import({LPRTestConfiguration.class})
	static class TestConfiguration {
		@Bean
		public HAIBADAO haibaDao() {
			return Mockito.mock(HAIBADAO.class);
		}
	}
	
	@Autowired
	HAIBADAO haibaDao;

	@Autowired
	ContactsWithSameStartDateRule contactsWithSameStartDateRule;

	String cpr;
	long recordNummer;
	long recordNummer2;
	long recordNummer3;
	String sygehusCode;
	String afdelingsCode;
	String sygehusCode2;
	String afdelingsCode2;
	String sygehusCode3;
	String afdelingsCode3;
	DateTime in;
	DateTime out;
	DateTime in2;
	DateTime out2;
	DateTime in3;
	DateTime out3;

	String oprCode1;
	String oprType1;
	String extraOprCode1;
	DateTime op1;

	@Before
	public void init() {
    	// Init Administration data
		cpr = "1111111111";
    	recordNummer = 1234;
    	sygehusCode = "csgh";
    	afdelingsCode = "afd";
    	in = new DateTime(2010, 5, 3, 0, 0, 0);
    	out = new DateTime(2010, 6, 4, 12, 0, 0);

    	recordNummer2 = 1235;
    	sygehusCode2 = "csgh";
    	afdelingsCode2 = "afd";
    	in2 = new DateTime(2010, 5, 3, 0, 0, 0);
    	out2 = new DateTime(2010, 6, 4, 12, 0, 0);

    	recordNummer3 = 1236;
    	sygehusCode3 = "abcd";
    	afdelingsCode3 = "afd";
    	in3 = new DateTime(2010, 8, 3, 0, 0, 0);
    	out3 = new DateTime(2010, 8, 10, 12, 0, 0);

    	// Init Procedure data
    	oprCode1 = "J03.9";
    	oprType1 = "A";
    	extraOprCode1 = "tilA";
    	op1 = new DateTime(2010, 5, 4, 0, 0, 0);
    	
    	Mockito.reset(haibaDao);
	}
	
	@Test 
	public void contactsWithSameInAndOutdateButDifferentHospitalShouldResultInError() {
		assertNotNull(contactsWithSameStartDateRule);
		
    	sygehusCode2 = "test";
		List<Administration> contacts = setupContacts();

		contactsWithSameStartDateRule.setContacts(contacts);
		contactsWithSameStartDateRule.doProcessing();
		
		List<Administration> processedContacts = contactsWithSameStartDateRule.getContacts();
		
		assertTrue("Expecting 2 contacts", processedContacts.size() == 2);

		// Expect 1 error to be logged
		Mockito.verify(haibaDao, Mockito.atLeastOnce()).saveBusinessRuleError((BusinessRuleError) Mockito.any());

		Collections.sort(processedContacts, new AdministrationInDateComparator());
		
		// contact #3 should still be processed, don't know which one of the 2 with same in and outdate that is kept - it is up to the sortingalgorithm - and it doesn't matter anyway because the other one is logged as an error.
		Administration last = processedContacts.get(1);
		assertEquals(recordNummer3, last.getRecordNumber());
	}

	@Test 
	public void contactsWithSameIndateButDifferentOutDateKeepContactWithLatestOutDate() {
		assertNotNull(contactsWithSameStartDateRule);
    	out2 = new DateTime(2010, 6, 10, 12, 0, 0);
		List<Administration> contacts = setupContacts();

		contactsWithSameStartDateRule.setContacts(contacts);
		contactsWithSameStartDateRule.doProcessing();
		
		List<Administration> processedContacts = contactsWithSameStartDateRule.getContacts();
		
		assertTrue("Expecting 2 contacts", processedContacts.size() == 2);

		// Expect no errors to be logged
		Mockito.verify(haibaDao, Mockito.never()).saveBusinessRuleError((BusinessRuleError) Mockito.any());

		Collections.sort(processedContacts, new AdministrationInDateComparator());

		//Contact #2 should be the one we keep
		Administration second = processedContacts.get(0);
		assertEquals(out2.toDate(), second.getUdskrivningsDatetime());
		assertEquals("Expected 2 procedures",2, second.getLprProcedures().size());
		assertEquals("Expect ref to old contact is kept", recordNummer, second.getLprReferencer().get(0).getLprRecordNumber());
		
		// contact #3 should still be processed
		Administration last = processedContacts.get(1);
		assertEquals(recordNummer3, last.getRecordNumber());
	
	
	}

	private List<Administration> setupContacts() {
		List<Administration> contacts = new ArrayList<Administration>();
		Administration contact = new Administration();
		contact.setRecordNumber(recordNummer);
		contact.setSygehusCode(sygehusCode);
		contact.setAfdelingsCode(afdelingsCode);
		contact.setCpr(cpr);
		contact.setIndlaeggelsesDatetime(in.toDate());
		if(out != null) {
			contact.setUdskrivningsDatetime(out.toDate());
		}
		List<LPRProcedure> procedures = new ArrayList<LPRProcedure>();
		LPRProcedure procedure = new LPRProcedure();
		procedure.setAfdelingsCode(afdelingsCode);
		procedure.setSygehusCode(sygehusCode);
		procedure.setRecordNumber(recordNummer);
		procedure.setProcedureCode(oprCode1);
		procedure.setProcedureType(oprType1);
		if(op1 != null) {
			procedure.setProcedureDatetime(op1.toDate());
		}
		procedure.setTillaegsProcedureCode(extraOprCode1);
		procedures.add(procedure);
		contact.setLprProcedures(procedures);
		
		
		
		Administration contact2 = new Administration();
		contact2.setRecordNumber(recordNummer2);
		contact2.setSygehusCode(sygehusCode2);
		contact2.setAfdelingsCode(afdelingsCode2);
		contact2.setCpr(cpr);
		contact2.setIndlaeggelsesDatetime(in2.toDate());
		if(out2 != null) {
			contact2.setUdskrivningsDatetime(out2.toDate());
		}
		// doesn't matter procedure is the same
		contact2.setLprProcedures(procedures);
		
		Administration contact3 = new Administration();
		contact3.setRecordNumber(recordNummer3);
		contact3.setSygehusCode(sygehusCode3);
		contact3.setAfdelingsCode(afdelingsCode3);
		contact3.setCpr(cpr);
		contact3.setIndlaeggelsesDatetime(in3.toDate());
		contact3.setUdskrivningsDatetime(out3.toDate());
		// doesn't matter procedure is the same
		contact3.setLprProcedures(procedures);

		contacts.add(contact3);
		contacts.add(contact2);
		contacts.add(contact);

		return contacts;
	}
	
}
