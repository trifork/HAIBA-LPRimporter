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
import static org.junit.Assert.fail;

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
import dk.nsi.haiba.lprimporter.exception.RuleAbortedException;
import dk.nsi.haiba.lprimporter.model.haiba.Statistics;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ExtendContactEndtimeRuleTest {

	@Configuration
    @Import({LPRTestConfiguration.class})
	static class TestConfiguration {
	}
	
	@Autowired
	ExtendContactEndtimeRule extendContactEndtimeRule;

	String cpr;
	String recordNummer;
	String sygehusCode;
	String afdelingsCode;
	DateTime in;
	DateTime out;

	String oprCode1;
	String oprType1;
	String extraOprCode1;
	DateTime op1;
	DateTime op2;

	@Before
	public void init() {
    	// Init Administration data
		cpr = "1111111111";
    	recordNummer = "1234";
    	sygehusCode = "csgh";
    	afdelingsCode = "afd";
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
	 * The Contact enddatetime is after the proceduredatetime, so no action should be taken.
	 */
	@Test 
	public void contactDateIsAfterLatestProcedureDate() {
		assertNotNull(extendContactEndtimeRule);
		
		List<Administration> contacts = setupContacts();
		
		extendContactEndtimeRule.setContacts(contacts);
		extendContactEndtimeRule.doProcessing(Statistics.getInstance());
		
		assertEquals(out.toDate(), contacts.get(0).getUdskrivningsDatetime());
	}
	
	/*
	 * The proceduredatetime is after the contact enddatetime, but not more than 24 hours
	 * so contact enddatetime should be the same as proceduredatetime
	 */
	@Test 
	public void latestProcedureDateIsAfterContactDate() {
		
    	op1 = new DateTime(2010, 6, 4, 16, 0, 0);
		
		List<Administration> contacts = setupContacts();
		
		extendContactEndtimeRule.setContacts(contacts);
		extendContactEndtimeRule.doProcessing(Statistics.getInstance());
		
		assertEquals(op1.toDate(), contacts.get(0).getUdskrivningsDatetime());
	}
	
	/*
	 * The proceduredatetime is after the contact enddatetime, with more than 24 hours
	 * so we expect a businessrule error
	 */
	@Test
	public void expectBusinessRuleErrorBecauseProcedureDateIsToLate() {

    	op1 = new DateTime(2010, 6, 5, 13, 0, 0);
		List<Administration> contacts = setupContacts();
		
		extendContactEndtimeRule.setContacts(contacts);
		
		try {
			extendContactEndtimeRule.doProcessing(Statistics.getInstance());
		} catch(RuleAbortedException e) {
			BusinessRuleError businessRuleError = e.getBusinessRuleError();
			assertEquals(recordNummer, businessRuleError.getLprReference());
			assertEquals("Proceduretidspunkt ligger mere end 24 timer efter udskrivningstidspunkt", businessRuleError.getDescription());
			assertEquals("Forlæng ift. procedurer efter udskrivning", businessRuleError.getAbortedRuleName());
		} catch(Exception e) {
			fail("Unexpected exception: "+ e);
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
		
		return contacts;
	}
	
}
