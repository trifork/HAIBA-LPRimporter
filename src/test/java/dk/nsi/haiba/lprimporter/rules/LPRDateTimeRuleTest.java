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
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class LPRDateTimeRuleTest {

	@Configuration
    @Import({LPRTestConfiguration.class})
	static class TestConfiguration {
	}
	
	@Autowired
	LPRDateTimeRule lprDateTimeRule;

	String cpr = "1111111111";
	long recordNummer = 1234;
	String sygehusCode = "csgh";
	String afdelingsCode = "afd";
	DateTime in = new DateTime(2010, 5, 3, 0, 0, 0);
	DateTime out = new DateTime(2010, 6, 4, 12, 0, 0);

	String oprCode1 = "J03.9";
	String oprType1 = "A";
	String extraOprCode1 = "tilA";
	DateTime op1 = new DateTime(2010, 5, 3, 8, 0, 0);

	@Before
	public void init() {
    	// Init Administration data
		cpr = "1111111111";
    	recordNummer = 1234;
    	sygehusCode = "csgh";
    	afdelingsCode = "afd";
    	in = new DateTime(2010, 5, 3, 0, 0, 0);
    	out = new DateTime(2010, 6, 4, 12, 0, 0);

    	// Init Procedure data
    	oprCode1 = "J03.9";
    	oprType1 = "A";
    	extraOprCode1 = "tilA";
    	op1 = new DateTime(2010, 5, 3, 8, 0, 0);
	}
	
	
	/*
	 * If data is as expected, the rule shouldn't modify data
	 */
	@Test
	public void ruleDoesntModifyData() {
		
		List<Administration> contacts = setupContacts();

		lprDateTimeRule.setContacts(contacts);
		lprDateTimeRule.doProcessing();
		
		assertNotNull("1 contact is still expected", contacts);
		assertEquals(1, contacts.size());
		assertEquals(in.toDate(), contacts.get(0).getIndlaeggelsesDatetime());
		assertEquals(out.toDate(), contacts.get(0).getUdskrivningsDatetime());
		assertEquals(1, contacts.get(0).getLprProcedures().size());
		assertEquals("Procedure datetime should not have been modified", op1.toDate(), contacts.get(0).getLprProcedures().get(0).getProcedureDatetime());
		
	}

	/*
	 * Admission enddate is 0, so it should be set to the next day.
	 */
	@Test
	public void ruleAdds24HoursToAdmissionEndDate() {

		out = new DateTime(2010, 6, 5, 0, 0, 0);
		
		List<Administration> contacts = setupContacts();
		
		lprDateTimeRule.setContacts(contacts);
		lprDateTimeRule.doProcessing();
		
		assertNotNull("1 contact is still expected", contacts);
		assertEquals(1, contacts.size());
		assertEquals(in.toDate(), contacts.get(0).getIndlaeggelsesDatetime());
		assertEquals("Expect end date is the next day", out.plusHours(24).toDate(), contacts.get(0).getUdskrivningsDatetime());
		assertEquals(1, contacts.get(0).getLprProcedures().size());
		assertEquals("Procedure datetime should not have been modified", op1.toDate(), contacts.get(0).getLprProcedures().get(0).getProcedureDatetime());
		
	}
	
	/*
	 * Procedure date is 0, so it should be set to 12 the same day.
	 */
	@Test
	public void ruleAdds12HoursToProcedureDate() {

    	op1 = new DateTime(2010, 5, 3, 0, 0, 0);
    	
		List<Administration> contacts = setupContacts();
		
		lprDateTimeRule.setContacts(contacts);
		lprDateTimeRule.doProcessing();
		
		assertNotNull("1 contact is still expected", contacts);
		assertEquals(1, contacts.size());
		assertEquals(in.toDate(), contacts.get(0).getIndlaeggelsesDatetime());
		assertEquals(out.toDate(), contacts.get(0).getUdskrivningsDatetime());
		assertEquals(1, contacts.get(0).getLprProcedures().size());
		assertEquals("Expect 12 hours added to  procedure date", op1.plusHours(12).toDate(), contacts.get(0).getLprProcedures().get(0).getProcedureDatetime());
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
		procedure.setProcedureDatetime(op1.toDate());
		procedure.setTillaegsProcedureCode(extraOprCode1);
		procedures.add(procedure);
		contact.setLprProcedures(procedures);
		contacts.add(contact);
		
		return contacts;
	}
	
}
