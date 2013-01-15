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

import static org.junit.Assert.assertNull;

import java.util.ArrayList;
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
import dk.nsi.haiba.lprimporter.model.lpr.LPRDiagnose;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ContactToAdmissionRuleTest {
	
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
	ContactToAdmissionRule contactToAdmissionRule;

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

	@Test
	public void ruleConvertsLPRDataToHAIBAData() {
		
		List<Administration> contacts = setupContacts();
		contactToAdmissionRule.setContacts(contacts);
		
		assertNull("This is the last rule", contactToAdmissionRule.doProcessing());
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

		List<LPRDiagnose> diagnoses = new ArrayList<LPRDiagnose>();
		contact.setLprDiagnoses(diagnoses);
		
		contacts.add(contact);
		
		return contacts;
	}


}
