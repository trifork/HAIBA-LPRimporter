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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
import dk.nsi.haiba.lprimporter.dao.impl.HAIBADAOImpl;
import dk.nsi.haiba.lprimporter.exception.DAOException;
import dk.nsi.haiba.lprimporter.model.haiba.Diagnose;
import dk.nsi.haiba.lprimporter.model.haiba.Indlaeggelse;
import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;
import dk.nsi.haiba.lprimporter.model.haiba.Procedure;
import dk.nsi.haiba.lprimporter.model.haiba.Statistics;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRDiagnose;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;
import dk.nsi.haiba.lprimporter.rules.BusinessRuleError;

/*
 * Tests the HAIBADAO class
 * Spring transaction ensures rollback after test is finished
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional("haibaTransactionManager")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class HAIBADAOIT {
	
    @Configuration
    @PropertySource("classpath:test.properties")
    @Import(LPRIntegrationTestConfiguration.class)
    static class ContextConfiguration {
        @Bean
        public HAIBADAO haibaDao() {
            return new HAIBADAOImpl();
        }

    }

    @Autowired
    @Qualifier("haibaJdbcTemplate")
    JdbcTemplate jdbc;
    
    @Autowired
    HAIBADAO haibaDao;
    
	String cpr;
	String sygehusCode;
	String afdelingsCode;
	Date in;
	Date out;
	String recordNumber;

	@Before
    public void init() {
		cpr = "1234567890";
		sygehusCode = "qwer";
		afdelingsCode = "asd";
		Calendar calendar = new GregorianCalendar();
		in = calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		out = calendar.getTime();
		recordNumber = "99999";
    }
    
    /*
     * Inserts an IndlaeggelsesForloeb into the HAIBA db, and tests data is inserted correct by DAO
     */
    @Test
	public void insertsSingleIndlaeggelse() {
    	
		List<Indlaeggelse> indlaeggelser = createIndlaeggelser(false);
		
    	assertNotNull(haibaDao);
		haibaDao.saveIndlaeggelsesForloeb(indlaeggelser);
		
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Indlaeggelser"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Indlaeggelsesforloeb"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from LPR_Reference"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Diagnoser"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Procedurer"));

        assertEquals("Expected db id", 545, jdbc.queryForInt("select LPR_dbid from LPR_Reference where LPR_recordnummer = " + recordNumber));
        
		assertEquals(sygehusCode, jdbc.queryForObject("select sygehuskode from Indlaeggelser", String.class));
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
		assertEquals(sdf.format(in), sdf.format(jdbc.queryForObject("select indlaeggelsesdatotid from Indlaeggelser", Date.class)));
		assertEquals(sdf.format(out), sdf.format(jdbc.queryForObject("select udskrivningsdatotid from Indlaeggelser", Date.class)));

		assertEquals(sdf.format(in), sdf.format(jdbc.queryForObject("select proceduredatotid from Procedurer", Date.class)));
    
    }

    /*
     * Inserts an Ambulant Contact into the HAIBA db, and tests data is inserted correct by DAO
     */
    @Test
	public void insertsSingleAmbulantContact() {
		List<Administration> contacts = new ArrayList<Administration>();
		Administration contact = new Administration();
		contact.setRecordNumber("1234");
		contact.setSygehusCode(sygehusCode);
		contact.setAfdelingsCode(afdelingsCode);
		contact.setCpr(cpr);
		contact.setIndlaeggelsesDatetime(in);
		contact.setUdskrivningsDatetime(out);
		contact.setPatientType(2);
		
		List<LPRProcedure> procedures = new ArrayList<LPRProcedure>();
		LPRProcedure procedure = new LPRProcedure();
		procedure.setAfdelingsCode(afdelingsCode);
		procedure.setSygehusCode(sygehusCode);
		procedure.setRecordNumber("1234");
		procedure.setProcedureCode("A");
		procedure.setProcedureType("B");
		procedure.setProcedureDatetime(out);
		procedure.setTillaegsProcedureCode("1");
		procedures.add(procedure);
		contact.setLprProcedures(procedures);
		
		List<LPRDiagnose> diagnoses = new ArrayList<LPRDiagnose>();
		LPRDiagnose diagnosis = new LPRDiagnose();
		diagnosis.setRecordNumber("1234");
		diagnosis.setDiagnoseCode("B");
		diagnosis.setDiagnoseType("A");
		diagnosis.setTillaegsDiagnose("C");
		diagnoses.add(diagnosis);
		contact.setLprDiagnoses(diagnoses);
		
		List<LPRReference> lprRefs = new ArrayList<LPRReference>();
		lprRefs.add(new LPRReference(789, "2345"));
		contact.setLprReferencer(lprRefs);
		
		contacts.add(contact);
    	
		
    	assertNotNull(haibaDao);
		haibaDao.saveAmbulantIndlaeggelser(contacts);
		
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from AmbulantKontakt"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from AmbulantLPR_Reference"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from AmbulantDiagnoser"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from AmbulantProcedurer"));

		assertEquals("Expected db id", 789, jdbc.queryForInt("select LPR_dbid from AmbulantLPR_Reference where LPR_recordnummer = 2345"));

		assertEquals(sygehusCode, jdbc.queryForObject("select sygehuskode from AmbulantKontakt", String.class));
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
		assertEquals(sdf.format(in), sdf.format(jdbc.queryForObject("select indlaeggelsesdatotid from AmbulantKontakt", Date.class)));
		assertEquals(sdf.format(out), sdf.format(jdbc.queryForObject("select udskrivningsdatotid from AmbulantKontakt", Date.class)));

		assertEquals(sdf.format(out), sdf.format(jdbc.queryForObject("select proceduredatotid from AmbulantProcedurer", Date.class)));
    
    }

    
    @Test
	public void insertsIndlaeggelseAndDeleteIt() {
    	
		List<Indlaeggelse> indlaeggelser = createIndlaeggelser(false);
		
    	assertNotNull(haibaDao);
		haibaDao.saveIndlaeggelsesForloeb(indlaeggelser);
		
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Indlaeggelser"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Indlaeggelsesforloeb"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from LPR_Reference"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Diagnoser"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Procedurer"));
		
		// test it works as intended with a cpr number not in the database
		haibaDao.prepareCPRNumberForImport("other");
    
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Indlaeggelser"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Indlaeggelsesforloeb"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from LPR_Reference"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Diagnoser"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Procedurer"));

		haibaDao.prepareCPRNumberForImport(cpr);
		
		assertEquals("Expected 0 row", 0, jdbc.queryForInt("select count(*) from Indlaeggelser"));
		assertEquals("Expected 0 row", 0, jdbc.queryForInt("select count(*) from Indlaeggelsesforloeb"));
		assertEquals("Expected 0 row", 0, jdbc.queryForInt("select count(*) from LPR_Reference"));
		assertEquals("Expected 0 row", 0, jdbc.queryForInt("select count(*) from Diagnoser"));
		assertEquals("Expected 0 row", 0, jdbc.queryForInt("select count(*) from Procedurer"));
    }
    
    @Test
	public void insertsAndFetchesCurrentPatient() {
    	
		List<Indlaeggelse> indlaeggelser = createIndlaeggelser(true);
		
    	assertNotNull(haibaDao);
		haibaDao.saveIndlaeggelsesForloeb(indlaeggelser);
		
		List<String> currentPatients = haibaDao.getCurrentPatients();
		
		assertEquals("Expected 1 row", 1, currentPatients.size());
		assertEquals(cpr, currentPatients.get(0));
    }
    
    @Test
    public void insertsBusinessruleError() {
        int dbId = 1;
    	String refno = "1234";
    	String description = "description";
    	String abortedRuleName = "abortedRuleName";
    	
    	BusinessRuleError error = new BusinessRuleError(dbId, refno, description, abortedRuleName);
    	
    	haibaDao.saveBusinessRuleError(error);
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from RegelFejlbeskeder"));
		assertEquals(refno, jdbc.queryForObject("select LPR_recordnummer from RegelFejlbeskeder", String.class));
		assertEquals(description, jdbc.queryForObject("select Fejlbeskrivelse from RegelFejlbeskeder", String.class));
		assertEquals(abortedRuleName, jdbc.queryForObject("select AfbrudtForretningsregel from RegelFejlbeskeder", String.class));
		assertEquals(dbId, jdbc.queryForInt("select LPR_dbid from RegelFejlbeskeder"));
    }
    
    @Test 
    public void fetchSygehusWhenSygehusCodeIs3800() {
    	
    	jdbc.update("insert into klass_shak (Nummer, Navn, Organisationstype, CreatedDate, ModifiedDate, ValidFrom, ValidTo) values ('3800999', 'TST Testafdeling', 'test', '2009-01-01', '2009-01-01', '2009-01-01', '2045-01-01')");
    	
    	String sygehusInitials = haibaDao.getSygehusInitials("3800", "999", new Date());
    	
    	assertEquals("TST", sygehusInitials);
    	
    }

    @Test(expected=DAOException.class)
    public void insertsBrokenBusinessruleError() {
    	
    	haibaDao.saveBusinessRuleError(null);
    	
    }
    
    /*
     * Utility method to create a list of indlaeggelser as the rules would have done.
     */
    private List<Indlaeggelse> createIndlaeggelser(boolean current) {
		List<Indlaeggelse> indlaeggelser = new ArrayList<Indlaeggelse>();
		LPRReference lprRef = new LPRReference(545, recordNumber);
		Diagnose d = new Diagnose("d1", "A", "d2");
		Procedure p = new Procedure("p1", "p", "p2", sygehusCode, afdelingsCode, in);
		Indlaeggelse indlaeggelse = new Indlaeggelse(cpr,sygehusCode,afdelingsCode, in, out, current);
		indlaeggelse.addLPRReference(lprRef);
		indlaeggelse.addDiagnose(d);
		indlaeggelse.addProcedure(p);
		indlaeggelser.add(indlaeggelse);
		return indlaeggelser;
	}


    @Test 
    public void testStatistics() {

    	Statistics stat = Statistics.getInstance();
    	stat.resetInstance();
    	stat = Statistics.getInstance();
    	
    	stat.rule1Counter +=1;
    	stat.rule1Counter +=1;
    	DateTime d = new DateTime(stat.getDate().getTime()).withMillisOfSecond(0);
    	
    	haibaDao.saveStatistics(stat);
    	
    	DateTime d2 = new DateTime(jdbc.queryForObject("select KoerselsDato from Statistik", Date.class).getTime());
    	assertEquals(d, d2);
    	
    	assertEquals("Expected 2", 2, jdbc.queryForInt("select Regel1 from Statistik"));
    }

}
