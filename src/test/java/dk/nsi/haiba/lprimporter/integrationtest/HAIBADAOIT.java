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
    
    /*
     * Inserts an IndlaeggelsesForloeb into the HAIBA db, and tests data is inserted correct by DAO
     */
    @Test
	public void insertsSingleIndlaeggelse() {
    	
		List<Indlaeggelse> indlaeggelser = new ArrayList<Indlaeggelse>();
		String cpr = "1234567890";
		String sygehusCode = "qwer";
		String afdelingsCode = "asd";
		Calendar calendar = new GregorianCalendar();
		Date d1 = calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		Date d2 = calendar.getTime();
		LPRReference lprRef = new LPRReference();
		lprRef.setLprRecordNumber(99999);
		Diagnose d = new Diagnose("d1", "A", "d2");
		Procedure p = new Procedure("p1", "p", "p2", sygehusCode, afdelingsCode, d1);
		
		Indlaeggelse indlaeggelse = new Indlaeggelse(cpr,sygehusCode,afdelingsCode, d1, d2);
		indlaeggelse.addLPRReference(lprRef);
		indlaeggelse.addDiagnose(d);
		indlaeggelse.addProcedure(p);
		
		indlaeggelser.add(indlaeggelse);
		
    	assertNotNull(haibaDao);
		haibaDao.saveIndlaeggelsesForloeb(indlaeggelser);
		
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Indlaeggelser"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Indlaeggelsesforloeb"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from LPR_Reference"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Diagnoser"));
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from Procedurer"));

		assertEquals(sygehusCode, jdbc.queryForObject("select sygehuskode from Indlaeggelser", String.class));
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
		assertEquals(sdf.format(d1), sdf.format(jdbc.queryForObject("select indlaeggelsesdatotid from Indlaeggelser", Date.class)));
		assertEquals(sdf.format(d2), sdf.format(jdbc.queryForObject("select udskrivningsdatotid from Indlaeggelser", Date.class)));

		assertEquals(sdf.format(d1), sdf.format(jdbc.queryForObject("select proceduredatotid from Procedurer", Date.class)));
    
    }
    
    @Test
    public void insertsBusinessruleError() {
    	long refno = 1234;
    	String description = "description";
    	String abortedRuleName = "abortedRuleName";
    	
    	BusinessRuleError error = new BusinessRuleError(refno, description, abortedRuleName);
    	
    	haibaDao.saveBusinessRuleError(error);
		assertEquals("Expected 1 row", 1, jdbc.queryForInt("select count(*) from RegelFejlbeskeder"));
		assertEquals(refno, jdbc.queryForLong("select LPR_recordnummer from RegelFejlbeskeder"));
		assertEquals(description, jdbc.queryForObject("select Fejlbeskrivelse from RegelFejlbeskeder", String.class));
		assertEquals(abortedRuleName, jdbc.queryForObject("select AfbrudtForretningsregel from RegelFejlbeskeder", String.class));
    }

    @Test(expected=DAOException.class)
    public void insertsBrokenBusinessruleError() {
    	
    	haibaDao.saveBusinessRuleError(null);
    	
    }
    
}
