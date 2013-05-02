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
import dk.nsi.haiba.lprimporter.model.haiba.Indlaeggelse;
import dk.nsi.haiba.lprimporter.model.haiba.Statistics;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRDiagnose;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ContactToAdmissionRuleTest {
	
	@Configuration
    @Import({LPRTestConfiguration.class})
	static class TestConfiguration {
	}
	
	@Autowired
	ContactToAdmissionRule contactToAdmissionRule;

	String cpr;
	long recordNummer;
	long recordNummer2;
	String sygehusCode;
	String afdelingsCode;
	String sygehusCode2;
	String afdelingsCode2;
	DateTime in;
	DateTime out;
	DateTime in2;
	DateTime out2;

	String oprCode1;
	String oprType1;
	String extraOprCode1;
	DateTime op1;
	
	String diagnosisCode;
	String diagnosisType;
	String tillaegsDiagnosis;

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
    	in2 = new DateTime(2010, 6, 4, 12, 0, 0);
    	out2 = new DateTime(2010, 6, 10, 12, 0, 0);

    	// Init Procedure data
    	oprCode1 = "J03.9";
    	oprType1 = "A";
    	extraOprCode1 = "tilA";
    	op1 = new DateTime(2010, 5, 3, 8, 0, 0);

    	// Init Diagnose data
    	diagnosisCode = "d345";
    	diagnosisType = "A";
    	tillaegsDiagnosis = "B";

	}

	@Test
	public void ruleConvertsLPRDataToHAIBADataSameHospitalAndDepartment() {
		
		List<Administration> contacts = setupContacts();
		contactToAdmissionRule.setContacts(contacts);
		
		LPRRule nextRule = contactToAdmissionRule.doProcessing(Statistics.getInstance());
		 
		assertTrue(nextRule instanceof ConnectAdmissionsRule);
		
		ConnectAdmissionsRule connectAdmissionsRule = (ConnectAdmissionsRule)nextRule;
		List<Indlaeggelse> admissions = connectAdmissionsRule.getAdmissions();
		assertNotNull(admissions);
		assertEquals("Expected 1 admission", 1, admissions.size());
		assertEquals("Expected 2 contact references", 2, admissions.get(0).getLprReferencer().size());
	}

	@Test
	public void ruleConvertsLPRDataToHAIBADataSameHospitalDifferentDepartment() {
		
    	afdelingsCode2 = "af2";
		List<Administration> contacts = setupContacts();
		contactToAdmissionRule.setContacts(contacts);
		
		LPRRule nextRule = contactToAdmissionRule.doProcessing(Statistics.getInstance());
		 
		assertTrue(nextRule instanceof ConnectAdmissionsRule);
		
		ConnectAdmissionsRule connectAdmissionsRule = (ConnectAdmissionsRule)nextRule;
		List<Indlaeggelse> admissions = connectAdmissionsRule.getAdmissions();
		assertNotNull(admissions);
		assertEquals("Expected 2 admissions", 2, admissions.size());
		for (Indlaeggelse indlaeggelse : admissions) {
			assertEquals("Expected 1 contact references", 1, indlaeggelse.getLprReferencer().size());
		}
	}

	/*
	 * 3 overlapping contacts, One with same starttime and endtime but on another department  
	 */
	@Test 
	public void overlappingContactsOneWith0TimeOnAnotherDepartment() {
	   	in = new DateTime(2010, 6, 12, 14, 0, 0);
	   	out = new DateTime(2010, 6, 17, 16, 0, 0);
	   	in2 = new DateTime(2010, 6, 17, 16, 0, 0);
	   	out2 = new DateTime(2010, 6, 17, 21, 0, 0);
	   	
	   	DateTime in3 = new DateTime(2010, 6, 17, 16, 0, 0);
	   	DateTime out3  = new DateTime(2010, 6, 17, 16, 0, 0);
		String afdelingsCode3 = "xxx";

	   	Administration contact3 = new Administration();
		contact3.setRecordNumber(recordNummer2+1);
		contact3.setSygehusCode(sygehusCode2);
		contact3.setAfdelingsCode(afdelingsCode3);
		contact3.setCpr(cpr);
		contact3.setIndlaeggelsesDatetime(in3.toDate());
		contact3.setUdskrivningsDatetime(out3.toDate());
	   	
		
		List<Administration> contacts = setupContacts();
		contacts.add(contact3);

		contactToAdmissionRule.setContacts(contacts);
		
		LPRRule nextRule = contactToAdmissionRule.doProcessing(Statistics.getInstance());
		ConnectAdmissionsRule connectAdmissionsRule = (ConnectAdmissionsRule)nextRule;
		List<Indlaeggelse> admissions = connectAdmissionsRule.getAdmissions();
		assertNotNull(admissions);
		assertEquals("Expected 3 admissions", 3, admissions.size());
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
		LPRDiagnose diagnosis = new LPRDiagnose();
		diagnosis.setDiagnoseCode(diagnosisCode);
		diagnosis.setDiagnoseType(diagnosisType);
		diagnosis.setRecordNumber(recordNummer);
		diagnosis.setTillaegsDiagnose(tillaegsDiagnosis);
		diagnoses.add(diagnosis);
		contact.setLprDiagnoses(diagnoses);

		Administration contact2 = new Administration();
		contact2.setRecordNumber(recordNummer2);
		contact2.setSygehusCode(sygehusCode2);
		contact2.setAfdelingsCode(afdelingsCode2);
		contact2.setCpr(cpr);
		contact2.setIndlaeggelsesDatetime(in2.toDate());
		contact2.setUdskrivningsDatetime(out2.toDate());

		contacts.add(contact);
		contacts.add(contact2);
		
		return contacts;
	}


}
