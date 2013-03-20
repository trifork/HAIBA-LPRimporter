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

import java.util.List;

import org.joda.time.DateTime;
import org.junit.After;
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
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.rules.LPRPrepareDataRule;
import dk.nsi.haiba.lprimporter.rules.LPRRule;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional("haibaTransactionManager")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ProcessRulesIT {

    @Configuration
    @PropertySource("classpath:test.properties")
    @Import(LPRIntegrationTestConfiguration.class)
    static class ContextConfiguration {
        @Bean
        public HAIBADAO haibaDao() {
            return new HAIBADAOImpl();
        }
        @Bean
        public LPRDAO lprDao() {
            return new LPRDAOImpl();
        }
    }

    @Autowired
    JdbcTemplate jdbcTemplate;
    
    @Autowired
    @Qualifier("haibaJdbcTemplate")
    JdbcTemplate jdbc;

    @Autowired
	HAIBADAO haibaDao;
	
	@Autowired
	LPRDAO lprDao;

	@Autowired
	LPRPrepareDataRule lprPrepareDataRule;

	String cpr;
	long recordNummer0;
	long recordNummer1;
	long recordNummer2;
	long recordNummer3;
	long recordNummer4;
	String sygehusCode0;
	String afdelingsCode0;
	String sygehusCode1;
	String afdelingsCode1;
	String sygehusCode2;
	String afdelingsCode2;
	String sygehusCode3;
	String afdelingsCode3;
	String sygehusCode4;
	String afdelingsCode4;
	DateTime in0;
	DateTime out0;
	DateTime in1;
	DateTime out1;
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

	@Before
	public void init() {
    	// Init Administration data
		cpr = "1111111111";
    	recordNummer0 = 1233;
    	sygehusCode0 = "xxxx";
    	afdelingsCode0 = "yyy";
    	in0 = new DateTime(2019, 5, 3, 0, 0, 0);
    	out0 = new DateTime(2019, 5, 3, 0, 0, 0);
		
		recordNummer1 = 1234;
    	sygehusCode1 = "csgh";
    	afdelingsCode1 = "234";
    	in1 = new DateTime(2010, 5, 3, 0, 0, 0);
    	out1 = new DateTime(2010, 6, 4, 0, 0, 0);

    	recordNummer2 = 1235;
    	sygehusCode2 = "csgh";
    	afdelingsCode2 = "235";
    	in2 = new DateTime(2010, 5, 3, 0, 0, 0);
    	out2 = new DateTime(2010, 6, 4, 0, 0, 0);

    	recordNummer3 = 1236;
    	sygehusCode3 = "abcd";
    	afdelingsCode3 = "236";
    	in3 = new DateTime(2010, 8, 3, 0, 0, 0);
    	out3 = new DateTime(2010, 8, 10, 0, 0, 0);

    	recordNummer4 = 1237;
    	sygehusCode4 = "gggg";
    	afdelingsCode4 = "123";
    	in4 = new DateTime(2011, 8, 3, 0, 0, 0);
    	out4 = new DateTime(2011, 8, 10, 0, 0, 0);
	}
	
	@After
	public void cleanUp() {
    	// Spring junit doesn't support rollback for multiple transaction managers - roll back data manually
    	jdbcTemplate.execute("delete from T_ADM");
	}
	
	@Test 
	public void threeContactsWithSameInAndOutdateButDifferentDepartmentShouldResultInError() {
		assertNotNull(lprPrepareDataRule);

    	sygehusCode3 = "csgh";
    	in3 = new DateTime(2010, 5, 3, 0, 0, 0);
    	out3 = new DateTime(2010, 6, 4, 0, 0, 0);
		
    	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
    			new Long(recordNummer0), cpr, sygehusCode0, afdelingsCode0, in0.toDate(), out0.toDate(), 0);
    	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
    			new Long(recordNummer1), cpr, sygehusCode1, afdelingsCode1, in1.toDate(), out1.toDate(), 0);
    	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
    			new Long(recordNummer2), cpr, sygehusCode2, afdelingsCode2, in2.toDate(), out2.toDate(), 0);
    	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
    			new Long(recordNummer3), cpr, sygehusCode3, afdelingsCode3, in3.toDate(), out3.toDate(), 0);
    	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
    			new Long(recordNummer4), cpr, sygehusCode4, afdelingsCode4, in4.toDate(), out4.toDate(), 0);
    	List<Administration> contactsByCPR = lprDao.getContactsByCPR(cpr);

		lprPrepareDataRule.setContacts(contactsByCPR);
		LPRRule next = lprPrepareDataRule.doProcessing();
		
		// Process rest of the rules and save admission
		while(next != null) {
			next = next.doProcessing();
		}
		
		// Expect 2 errors logged
		assertEquals(2, jdbc.queryForInt("select count(*) from RegelFejlbeskeder"));

		// check updated status flag in T_ADM
		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer0, String.class));
		assertEquals("FAILURE",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer1, String.class));
		assertEquals("FAILURE",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer2, String.class));
		assertEquals("FAILURE",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer3, String.class));
		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer4, String.class));
		
		assertEquals(2, jdbc.queryForInt("select count(*) from Indlaeggelser"));
	}
	
	@Test 
	public void threeIdenticalContactsButDifferentInTimeShouldBeMergedToOneAdmission() {
		assertNotNull(lprPrepareDataRule);
		
		sygehusCode0 = sygehusCode1;
		sygehusCode2 = sygehusCode1;
		afdelingsCode0 = afdelingsCode1;
		afdelingsCode2 = afdelingsCode1;
    	in0 = new DateTime(2010, 5, 3, 9, 0, 0);
    	out0 = new DateTime(2010, 5, 3, 0, 0, 0);
    	in1 = new DateTime(2010, 5, 3, 10, 0, 0);
    	out1 = new DateTime(2010, 5, 3, 0, 0, 0);
    	in2 = new DateTime(2010, 5, 3, 11, 0, 0);
    	out2 = new DateTime(2010, 5, 3, 0, 0, 0);
		
    	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
    			new Long(recordNummer0), cpr, sygehusCode0, afdelingsCode0, in0.toDate(), out0.toDate(), 0);
    	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
    			new Long(recordNummer1), cpr, sygehusCode1, afdelingsCode1, in1.toDate(), out1.toDate(), 0);
    	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
    			new Long(recordNummer2), cpr, sygehusCode2, afdelingsCode2, in2.toDate(), out2.toDate(), 0);

    	List<Administration> contactsByCPR = lprDao.getContactsByCPR(cpr);

		lprPrepareDataRule.setContacts(contactsByCPR);
		LPRRule next = lprPrepareDataRule.doProcessing();
		
		// Process rest of the rules and save admission
		while(next != null) {
			next = next.doProcessing();
		}

		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer0, String.class));
		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer1, String.class));
		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer2, String.class));

		assertEquals(1, jdbc.queryForInt("select count(*) from Indlaeggelser"));
	}
	
}
