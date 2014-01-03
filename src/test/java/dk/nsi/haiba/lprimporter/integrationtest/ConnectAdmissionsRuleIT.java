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
package dk.nsi.haiba.lprimporter.integrationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.dao.impl.HAIBADAOImpl;
import dk.nsi.haiba.lprimporter.dao.impl.LPRDAOImpl;
import dk.nsi.haiba.lprimporter.model.haiba.Diagnose;
import dk.nsi.haiba.lprimporter.model.haiba.Indlaeggelse;
import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;
import dk.nsi.haiba.lprimporter.model.haiba.Procedure;
import dk.nsi.haiba.lprimporter.model.haiba.Statistics;
import dk.nsi.haiba.lprimporter.rules.ConnectAdmissionsRule;
import dk.nsi.haiba.lprimporter.rules.LPRRule;

/*
 * Tests the that last rule saves admissions correctly'
 * Spring transaction ensures rollback after test is finished
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional("haibaTransactionManager")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ConnectAdmissionsRuleIT {
	
    @Configuration
    @PropertySource("classpath:test.properties")
    @Import(LPRIntegrationTestConfiguration.class)
    static class ContextConfiguration {
        @Autowired
        DataSource lprDataSource;

        @Bean
        public HAIBADAO haibaDao() {
            return new HAIBADAOImpl();
        }
        @Bean
        public LPRDAO lprDao() {
            return new LPRDAOImpl(lprDataSource);
        }
    }

    @Autowired
    @Qualifier("haibaJdbcTemplate")
    JdbcTemplate jdbc;
    
    @Autowired
    HAIBADAO haibaDao;
	
	@Autowired
	LPRDAO lprDao;

	@Autowired
	ConnectAdmissionsRule connectAdmissionsRule;

	String cpr;
	long recordNummer;
	long recordNummer2;
	long recordNummer3;
	long recordNummer4;
	String sygehusCode;
	String afdelingsCode;
	String sygehusCode2;
	String afdelingsCode2;
	String sygehusCode3;
	String afdelingsCode3;
	String sygehusCode4;
	String afdelingsCode4;
	DateTime in;
	DateTime out;
	DateTime in2;
	DateTime out2;
	DateTime in3;
	DateTime out3;
	DateTime in4;
	DateTime out4;
	
	String oprCode1;
	String oprType1;
	String extraOprCode1;
	DateTime op1;
	
	String diagnosisCode;
	String diagnosisType;
	String tillaegsDiagnosis;

	@Before
	public void init() {
    	// Init Indlaeggelse data
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

    	recordNummer3 = 1236;
    	sygehusCode3 = "abcd";
    	afdelingsCode3 = "afd";
    	in3 = new DateTime(2010, 8, 4, 0, 0, 0);
    	out3 = new DateTime(2010, 8, 10, 12, 0, 0);

    	recordNummer4 = 1237;
    	sygehusCode4 = "abcd";
    	afdelingsCode4 = "afd";
    	in4 = new DateTime(2010, 8, 10, 12, 0, 0);
    	out4 = new DateTime(2010, 9, 11, 12, 0, 0);
    	
    	// Init Procedure data
    	oprCode1 = "J03.9";
    	oprType1 = "A";
    	extraOprCode1 = "tilA";
    	op1 = new DateTime(2010, 8, 4, 0, 0, 0);
    	
    	//Init Diagnose data
    	diagnosisCode = "d345";
    	diagnosisType = "A";
    	tillaegsDiagnosis = "B";
	}

	@Test
	public void checkAdmissionsAreSaved() {
		
		List<Indlaeggelse> admissions = setupAdmissions();
		connectAdmissionsRule.setAdmissions(admissions);
		LPRRule nextRule = connectAdmissionsRule.doProcessing(Statistics.getInstance());

		assertNull("This is the last rule", nextRule);
		
		assertEquals("Expected 4 admission sequences, because its a ref. table", 4, jdbc.queryForInt("Select count(*) from Indlaeggelsesforloeb"));
		assertEquals("Expected 2 different sequence id's", 2, jdbc.queryForInt("Select count(distinct indlaeggelsesforloebid) from Indlaeggelsesforloeb "));
		assertEquals("Expected 4 different admission id's", 4, jdbc.queryForInt("Select count(distinct indlaeggelsesid) from Indlaeggelsesforloeb "));
		
		Long admissionId1 = jdbc.queryForLong("Select indlaeggelsesId from LPR_Reference where LPR_recordnummer =" +recordNummer);
		assertEquals("Expected 1 procedure connected to admission1", 1, jdbc.queryForInt("Select count(*) from Procedurer where IndlaeggelsesID = "+admissionId1));
		
		Long admissionId2 = jdbc.queryForLong("Select indlaeggelsesId from LPR_Reference where LPR_recordnummer =" +recordNummer2);
		assertEquals("Expected 1 diagnosis connected to admission2", 1, jdbc.queryForInt("Select count(*) from Diagnoser where IndlaeggelsesID = "+admissionId2));
		
	}

	private List<Indlaeggelse> setupAdmissions() {
		List<Indlaeggelse> admissions = new ArrayList<Indlaeggelse>();
		Indlaeggelse a1 = new Indlaeggelse();
		a1.addLPRReference(new LPRReference(recordNummer));
		a1.setSygehusCode(sygehusCode);
		a1.setAfdelingsCode(afdelingsCode);
		a1.setCpr(cpr);
		a1.setIndlaeggelsesDatetime(in.toDate());
		if (out != null) {
			a1.setUdskrivningsDatetime(out.toDate());
		}
		List<Procedure> procedures = new ArrayList<Procedure>();
		Procedure procedure = new Procedure();
		procedure.setAfdelingsCode(afdelingsCode);
		procedure.setSygehusCode(sygehusCode);
		procedure.setProcedureCode(oprCode1);
		procedure.setProcedureType(oprType1);
		if(op1 != null) {
			procedure.setProcedureDatetime(op1.toDate());
		}
		procedure.setTillaegsProcedureCode(extraOprCode1);
		// add twice, to test duplicates are removed
		procedures.add(procedure);
		procedures.add(procedure);
		a1.setProcedures(procedures);

		
		Indlaeggelse a2 = new Indlaeggelse();
		a2.addLPRReference(new LPRReference(recordNummer2));
		a2.setSygehusCode(sygehusCode2);
		a2.setAfdelingsCode(afdelingsCode2);
		a2.setCpr(cpr);
		a2.setIndlaeggelsesDatetime(in2.toDate());
		if (out2 != null) {
			a2.setUdskrivningsDatetime(out2.toDate());
		}
		a2.setProcedures(procedures);
		List<Diagnose> diagnoses = new ArrayList<Diagnose>();
		Diagnose diagnose = new Diagnose();
		diagnose.setDiagnoseCode(diagnosisCode);
		diagnose.setDiagnoseType(diagnosisType);
		diagnose.setTillaegsDiagnose(tillaegsDiagnosis);
		// add twice, to test duplicates are removed
		diagnoses.add(diagnose);
		diagnoses.add(diagnose);
		a2.setDiagnoses(diagnoses);

		Indlaeggelse a3 = new Indlaeggelse();
		a3.addLPRReference(new LPRReference(recordNummer3));
		a3.setSygehusCode(sygehusCode3);
		a3.setAfdelingsCode(afdelingsCode3);
		a3.setCpr(cpr);
		a3.setIndlaeggelsesDatetime(in3.toDate());
		if (out3 != null) {
			a3.setUdskrivningsDatetime(out3.toDate());
		}

		Indlaeggelse a4 = new Indlaeggelse();
		a4.addLPRReference(new LPRReference(recordNummer4));
		a4.setSygehusCode(sygehusCode4);
		a4.setAfdelingsCode(afdelingsCode4);
		a4.setCpr(cpr);
		a4.setIndlaeggelsesDatetime(in4.toDate());
		if (out4 != null) {
			a4.setUdskrivningsDatetime(out4.toDate());
		}

		admissions.add(a1);
		admissions.add(a3);
		admissions.add(a2);
		admissions.add(a4);
		
		return admissions;
	}

}
