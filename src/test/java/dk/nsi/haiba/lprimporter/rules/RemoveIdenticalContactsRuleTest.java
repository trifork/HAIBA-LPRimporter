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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import dk.nsi.haiba.lprimporter.config.LPRTestConfiguration;
import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;
import dk.nsi.haiba.lprimporter.model.haiba.Statistics;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class RemoveIdenticalContactsRuleTest {

	@Configuration
    @Import({LPRTestConfiguration.class})
	static class TestConfiguration {
	}
	
	@Autowired
	RemoveIdenticalContactsRule removeIdenticalContactsRule;

	String cpr;
	long recordNummer;
	long recordNummer2;
	long recordNummer3;
	String sygehusCode;
	String afdelingsCode;
	DateTime in;
	DateTime out;
	String sygehusCode3;
	String afdelingsCode3;

	String oprCode1;
	String oprType1;
	String extraOprCode1;
	DateTime op1;
	DateTime op2;

	@Before
	public void init() {
    	// Init Administration data
		cpr = "1111111111";
    	recordNummer = 1234;
    	recordNummer2 = 4321;
    	recordNummer3 = 5678;
    	sygehusCode = "csgh";
    	sygehusCode3 = "hgfd";
    	afdelingsCode = "afd";
    	afdelingsCode3 = "dfa";
    	in = new DateTime(2010, 5, 3, 0, 0, 0);
    	out = new DateTime(2010, 6, 4, 12, 0, 0);

    	// Init Procedure data
    	oprCode1 = "J03.9";
    	oprType1 = "A";
    	extraOprCode1 = "tilA";
    	op1 = new DateTime(2010, 5, 3, 8, 0, 0);
    	op2 = new DateTime(2010, 5, 12, 9, 0, 0);
	}
	
	/*
	 * 2 contacts with same fields are in the list, one of the should be removed
	 */
	@Test 
	public void removeIdenticalContactFromListWithDifferentRecordNumbers() {
		assertNotNull(removeIdenticalContactsRule);
		
		List<Administration> contacts = setupContacts();

		removeIdenticalContactsRule.setContacts(contacts);
		removeIdenticalContactsRule.doProcessing(Statistics.getInstance());
		
		assertTrue("Expecting 1 contact removed from the list", removeIdenticalContactsRule.getContacts().size() == 2);
		
		// check if there still is a reference to the removed contacts
		for (Administration contact : contacts) {
			if(contact.getRecordNumber() == recordNummer) {
				assertTrue("Expect recordnumber2 to be in list of references", contact.getLprReferencer().contains(new LPRReference(recordNummer2)));
			}
		}
	}
	
	/*
	 * 2 contacts with same fields are in the list, one of the should be removed
	 * They have different procedures and Diagnoses attached, test all are preserved
	 */
	@Test 
	public void removeIdenticalContactFromListAndPreserveDiagnosesAndProcedures() {
	
		List<Administration> contacts = setupContacts();

		removeIdenticalContactsRule.setContacts(contacts);
		removeIdenticalContactsRule.doProcessing(Statistics.getInstance());
		
		List<Administration> contactsAfterProcessing = removeIdenticalContactsRule.getContacts();
		
		assertTrue("Expecting 1 contact removed from the list", contactsAfterProcessing.size() == 2);
		for (Administration administration : contactsAfterProcessing) {
			if(administration.getRecordNumber() == recordNummer) {
				assertTrue("Expecting 3 Procedures after merge of identical contacts", administration.getLprProcedures().size() == 3);
			} else if(administration.getRecordNumber() == recordNummer3) {
				assertTrue("Expecting 0 Procedures after merge of identical contacts", administration.getLprProcedures().size() == 0);
			}
		}
	}
	
	private List<Administration> setupContacts() {
		List<Administration> contacts = new ArrayList<Administration>();
		Administration contact = new Administration();
		contact.setRecordNumber(recordNummer);
		contact.setSygehusCode(sygehusCode);
		contact.setAfdelingsCode(afdelingsCode);
		contact.setCpr(cpr);
		contact.setIndlaeggelsesDatetime(in.toDate());
		contact.setUdskrivningsDatetime(out.toDate());
		
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

		// procedure 2
		LPRProcedure procedure2 = new LPRProcedure();
		procedure2.setAfdelingsCode(afdelingsCode);
		procedure2.setSygehusCode(sygehusCode);
		procedure2.setRecordNumber(recordNummer);
		procedure2.setProcedureCode(oprCode1);
		procedure2.setProcedureType(oprType1);
		if(op2 != null) {
			procedure2.setProcedureDatetime(op2.toDate());
		}
		procedure2.setTillaegsProcedureCode(extraOprCode1);
		procedures.add(procedure2);
		
		contact.setLprProcedures(procedures);
		contacts.add(contact);
		
		Administration contact2 = new Administration();
		contact2.setRecordNumber(recordNummer2);
		contact2.setSygehusCode(sygehusCode);
		contact2.setAfdelingsCode(afdelingsCode);
		contact2.setCpr(cpr);
		contact2.setIndlaeggelsesDatetime(in.toDate());
		contact2.setUdskrivningsDatetime(out.toDate());
		contacts.add(contact2);
		List<LPRProcedure> procedures2 = new ArrayList<LPRProcedure>();
		LPRProcedure procedure21 = new LPRProcedure();
		procedure21.setAfdelingsCode(afdelingsCode);
		procedure21.setSygehusCode(sygehusCode);
		procedure21.setRecordNumber(recordNummer2);
		procedure21.setProcedureCode(oprCode1);
		procedure21.setProcedureType(oprType1);
		if(op1 != null) {
			procedure21.setProcedureDatetime(op1.toDate());
		}
		procedure21.setTillaegsProcedureCode(extraOprCode1);
		procedures2.add(procedure21);
		contact2.setLprProcedures(procedures2);		
		
		Administration contact3 = new Administration();
		contact3.setRecordNumber(recordNummer3);
		contact3.setSygehusCode(sygehusCode3);
		contact3.setAfdelingsCode(afdelingsCode3);
		contact3.setCpr(cpr);
		contact3.setIndlaeggelsesDatetime(in.toDate());
		contact3.setUdskrivningsDatetime(out.toDate());
		contacts.add(contact3);
		
		assertTrue(contact.equals(contact2));

		return contacts;
	}
	
}
