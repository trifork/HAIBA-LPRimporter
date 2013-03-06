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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
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

import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;
import dk.nsi.haiba.lprimporter.rules.LPRRulesEngine;
import dk.nsi.haiba.lprimporter.rules.RulesEngine;

/*
 * Tests the RulesEngine
 * Spring transaction ensures rollback after test is finished
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional("haibaTransactionManager")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class RulesEngineIT {
	
	@Configuration
    @PropertySource("classpath:test.properties")
    @Import(LPRIntegrationTestConfiguration.class)
	static class TestConfiguration {
        @Bean()
        public RulesEngine rulesEngine() {
            return new LPRRulesEngine();
        }
        
	}

    @Autowired
    @Qualifier("haibaJdbcTemplate")
    JdbcTemplate jdbc;

	@Autowired
	RulesEngine rulesEngine;
	
	String cpr;
	long recordNummer;
	String sygehusCode;
	String afdelingsCode;
	DateTime in;
	DateTime out;

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

    	// Init Procedure data
    	oprCode1 = "J03.9";
    	oprType1 = "A";
    	extraOprCode1 = "tilA";
    	op1 = new DateTime(2010, 5, 3, 8, 0, 0);
    	
    	// Clear content of logfile before each test is executed
    	clearBusinessRuleLogFile();
   	
	}
	
	
	private void clearBusinessRuleLogFile() {
		try {
			PrintWriter writer = new PrintWriter(FileUtils.getFile("forretningsregel-fejl.log"));
			writer.print("");
			writer.close();
		} catch (FileNotFoundException e) {
			//ignore
			e.printStackTrace();
		}
	}


	@Test
	public void CheckErrorMessagesAreLoggedCorrectly() {
		op1 = null;

		List<Administration> contacts = setupContacts();
		rulesEngine.processRuleChain(contacts);
		
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from RegelFejlbeskeder"));
		assertEquals(recordNummer, jdbc.queryForLong("select LPR_recordnummer from RegelFejlbeskeder"));
		
		File file = FileUtils.getFile("forretningsregel-fejl.log");
		assertNotNull(file);

		try {
			List<String> lines = FileUtils.readLines(file);
			assertEquals(1, lines.size());
			assertTrue(lines.get(0).contains("Proceduredato findes ikke"));
		} catch (IOException e) {
			fail("Lines are expected");
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
		contact.setPatientType(2);
		
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
		contacts.add(contact);
		
		return contacts;
	}

}
