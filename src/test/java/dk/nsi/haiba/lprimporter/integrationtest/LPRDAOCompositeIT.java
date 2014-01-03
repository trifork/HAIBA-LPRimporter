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

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
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

import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.dao.impl.LPRDAOComposite;
import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRDiagnose;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;
import dk.nsi.haiba.lprimporter.status.ImportStatus.Outcome;

/*
 * Tests the LPRDAO class
 * Spring transaction ensures rollback after test is finished
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional("lprTransactionManager")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class LPRDAOCompositeIT {

    @Configuration
    @PropertySource("classpath:test.properties")
    @Import(LPRIntegrationTestConfiguration.class)
    static class ContextConfiguration {
        @Bean
        public LPRDAO lprdao() {
            return new LPRDAOComposite();
        }
    }

    @Autowired
    @Qualifier(value = "jdbcTemplate")
    JdbcTemplate ssiJdbcTemplate;
    @Autowired
    @Qualifier(value = "minipasJdbcTemplate")
    JdbcTemplate minipasJdbcTemplate;

    @Autowired
    LPRDAO lprdao;

    /*
     * Inserts a single contact into the T_ADM table, and tests data is fetched correct from the DAO
     */
    @Test
    public void fetchSingleContact() {

        assertNotNull(lprdao);

        String cpr = "1111111111";
        long recordNummer = 1234;
        String sygehusCode = "csgh";
        String afdelingCode = "afd";
        DateTime in = new DateTime(2010, 5, 3, 8, 0, 0);
        DateTime out = new DateTime(2010, 6, 4, 16, 0, 0);

        ssiJdbcTemplate
                .update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
                        new Long(recordNummer), cpr, sygehusCode, afdelingCode, in.toDate(), out.toDate(), 2);

        List<Administration> contactsByCPR = lprdao.getContactsByCPR(cpr);
        assertNotNull("Expected 1 contact from LPR", contactsByCPR);
        assertEquals(1, contactsByCPR.size());

        Administration adm = contactsByCPR.get(0);

        assertEquals(recordNummer, adm.getRecordNumber());
        assertEquals(cpr, adm.getCpr());
        assertEquals(sygehusCode, adm.getSygehusCode());
        assertEquals(afdelingCode, adm.getAfdelingsCode());
        assertEquals(in.toDate(), adm.getIndlaeggelsesDatetime());
        assertEquals(out.toDate(), adm.getUdskrivningsDatetime());
    }

    /*
     * Inserts a single contact into the T_ADM table with processed date set, and tests that it is not fetched again by
     * DAO
     */
    @Test
    public void doNotfetchContactThatHasBeenImported() {

        String cpr = "1111111111";
        long recordNummer = 1234;
        String sygehusCode = "csgh";
        String afdelingCode = "afd";
        DateTime in = new DateTime(2010, 5, 3, 8, 0, 0);
        DateTime out = new DateTime(2010, 6, 4, 16, 0, 0);
        DateTime processed = new DateTime(2012, 12, 31, 11, 59, 23);

        ssiJdbcTemplate
                .update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, d_importdto) values (?, ?, ?, ?, ?, ?, ?)",
                        new Long(recordNummer), cpr, sygehusCode, afdelingCode, in.toDate(), out.toDate(),
                        processed.toDate());

        List<String> unprocessedCPRNumbers = lprdao.getCPRnumberBatch(20);
        assertNotNull("Expected 0 contacts from LPR", unprocessedCPRNumbers);
        assertEquals(0, unprocessedCPRNumbers.size());
    }

    /*
     * Inserts 2 diagnoses into the T_DIAG table, and tests data is fetched correct from the DAO
     */
    @Test
    public void fetchDiagnosesFromContact() {

        long recordNumber = 1234;
        String cpr = "1111111111";
        String diagnosisCode1 = "D03.9";
        String diagnosisCode2 = "D00.9";
        String diagnosisType1 = "A";
        String diagnosisType2 = "B";
        String extraDiagnosisCode1 = "tilA";
        String extraDiagnosisCode2 = "tilB";

        ssiJdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_pattype) values (?, ?, ?)",
                new Long(recordNumber), cpr, 2);
        ssiJdbcTemplate.update(
                "insert into T_KODER (v_recnum, c_kode, c_kodeart, c_tilkode,v_type) values (?, ?, ?, ?, ?)", new Long(
                        recordNumber), diagnosisCode1, diagnosisType1, extraDiagnosisCode1, "DIA");
        ssiJdbcTemplate.update(
                "insert into T_KODER (v_recnum, c_kode, c_kodeart, c_tilkode,v_type) values (?, ?, ?, ?, ?)", new Long(
                        recordNumber), diagnosisCode2, diagnosisType2, extraDiagnosisCode2, "DIA");

        Administration contact = lprdao.getContactsByCPR(cpr).get(0);

        List<LPRDiagnose> diagnoses = contact.getLprDiagnoses();

        assertNotNull("Expected 2 diagnoses from LPR", diagnoses);
        assertEquals(2, diagnoses.size());

        boolean diagnosis1checked = false;
        boolean diagnosis2checked = false;
        for (LPRDiagnose d : diagnoses) {
            if (diagnosisCode1.equals(d.getDiagnoseCode())) {
                assertEquals(diagnosisType1, d.getDiagnoseType());
                assertEquals(extraDiagnosisCode1, d.getTillaegsDiagnose());
                diagnosis1checked = true;
            } else if (diagnosisCode2.equals(d.getDiagnoseCode())) {
                assertEquals(diagnosisType2, d.getDiagnoseType());
                assertEquals(extraDiagnosisCode2, d.getTillaegsDiagnose());
                diagnosis2checked = true;
            } else {
                fail("did not expect an diagnosis with code: " + d.getDiagnoseCode());
            }
        }
        assertTrue(diagnosis1checked);
        assertTrue(diagnosis2checked);
    }

    /*
     * Inserts 2 procedures into the T_PROCEDURER table, and tests data is fetched correct from the DAO
     */
    @Test
    public void fetchProceduresFromContact() {

        long recordNumber = 1234;
        String cpr = "1111111111";
        String oprCode1 = "B03.9";
        String oprCode2 = "K00.9";
        String oprType1 = "A";
        String oprType2 = "B";
        String extraOprCode1 = "tilA";
        String extraOprCode2 = "tilB";
        String sygehusCode1 = "sgh1";
        String sygehusCode2 = "sgh2";
        String afdelingCode1 = "af1";
        String afdelingCode2 = "af2";
        DateTime op1 = new DateTime(2010, 5, 3, 8, 0, 0);
        DateTime op2 = new DateTime(2010, 5, 3, 12, 0, 0);

        ssiJdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_pattype) values (?, ?, ?)",
                new Long(recordNumber), cpr, 2);
        System.out.println(op1.toDate());
        ssiJdbcTemplate
                .update("insert into T_KODER (v_recnum, c_kode, c_kodeart, c_tilkode, c_psgh, c_pafd, d_pdto, v_type) values (?, ?, ?, ?, ?, ?, ?, ?)",
                        new Long(recordNumber), oprCode1, oprType1, extraOprCode1, sygehusCode1, afdelingCode1,
                        op1.toDate(), "PRO");
        ssiJdbcTemplate
                .update("insert into T_KODER (v_recnum, c_kode, c_kodeart, c_tilkode, c_psgh, c_pafd, d_pdto, v_type) values (?, ?, ?, ?, ?, ?, ?, ?)",
                        new Long(recordNumber), oprCode2, oprType2, extraOprCode2, sygehusCode2, afdelingCode2,
                        op2.toDate(), "PRO");

        Administration contact = lprdao.getContactsByCPR(cpr).get(0);

        List<LPRProcedure> procedures = contact.getLprProcedures();

        assertNotNull("Expected 2 procedures from LPR", procedures);
        assertEquals(2, procedures.size());

        boolean procedure1checked = false;
        boolean procedure2checked = false;
        for (LPRProcedure p : procedures) {

            if (oprCode1.equals(p.getProcedureCode())) {
                assertEquals(oprType1, p.getProcedureType());
                assertEquals(extraOprCode1, p.getTillaegsProcedureCode());
                assertEquals(sygehusCode1, p.getSygehusCode());
                assertEquals(afdelingCode1, p.getAfdelingsCode());
                assertEquals(op1.toDate(), p.getProcedureDatetime());
                procedure1checked = true;
            } else if (oprCode2.equals(p.getProcedureCode())) {
                assertEquals(oprType2, p.getProcedureType());
                assertEquals(extraOprCode2, p.getTillaegsProcedureCode());
                assertEquals(sygehusCode2, p.getSygehusCode());
                assertEquals(afdelingCode2, p.getAfdelingsCode());
                assertEquals(op2.toDate(), p.getProcedureDatetime());
                procedure2checked = true;
            } else {
                fail("did not expect an procedure with code: " + p.getProcedureCode());
            }
        }
        assertTrue(procedure1checked);
        assertTrue(procedure2checked);
    }

    /*
     * Inserts a contact into the T_ADM table, and tests import timestamp is set correctly from the DAO
     */
    @Test
    public void updateImportTimestamp() {
        ssiJdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr) values (?, ?)", 1234, "1111111111");
        minipasJdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr) values (?, ?)", 5678, "2222222222");

        lprdao.updateImportTime(new LPRReference(LPRDAOComposite.SSI_DB, 1234), Outcome.SUCCESS);
        lprdao.updateImportTime(new LPRReference(LPRDAOComposite.MINIPAS_DB, 5678), Outcome.SUCCESS);

        assertNotNull(ssiJdbcTemplate.queryForObject("select D_IMPORTDTO from T_ADM", Date.class));
        assertEquals(Outcome.SUCCESS.toString(),
                ssiJdbcTemplate.queryForObject("select V_STATUS from T_ADM", String.class));
    }

    /*
     * Inserts a couple of rows in the T_LOG_SYNC table, and tests if import is ready or not.
     */
    @Test
    public void checkImportReadiness() {

        ssiJdbcTemplate.update("insert into T_LOG_SYNC (v_sync_id, start_time, end_time) values (?, ?, ?)", 1,
                new Date(), new Date());
        ssiJdbcTemplate.update("insert into T_LOG_SYNC (v_sync_id, start_time, end_time) values (?, ?, ?)", 2,
                new Date(), new Date());
        ssiJdbcTemplate.update("insert into T_LOG_SYNC (v_sync_id, start_time, end_time) values (?, ?, ?)", 3,
                new Date(), null);
        minipasJdbcTemplate.update("insert into T_LOG_SYNC (v_sync_id, start_time, end_time) values (?, ?, ?)", 8000,
                new Date(), null);

        long maxSyncId = lprdao.isdatabaseReadyForImport();
        assertEquals("Databases are not ready for import", 0, maxSyncId);

        ssiJdbcTemplate.update("update T_LOG_SYNC set end_time = ? where v_sync_id = 3", new Date());
        assertEquals("Databases are not ready for import", 0, maxSyncId);

        minipasJdbcTemplate.update("update T_LOG_SYNC set end_time = ? where v_sync_id = 8000", new Date());
        maxSyncId = lprdao.isdatabaseReadyForImport();
        assertEquals("Databases are ready for import", 1, maxSyncId);

        // no transaction manager for minipas, thank you spring
        minipasJdbcTemplate.update("delete from T_LOG_SYNC where v_sync_id = 8000");
    }

    @Test
    public void checkForDeletedContacts() {
        long recordNumber = 1234567;
        String cpr = "1111111111";
        String cpr2 = "2222222222";
        try {
            ssiJdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr) values (?, ?)", new Long(recordNumber), cpr);
            ssiJdbcTemplate.update("insert into T_LOG_SYNC (v_sync_id, start_time, end_time) values (?, ?, ?)", 1,
                    new Date(), new Date());

            // check delete
            ssiJdbcTemplate
                    .update("insert into T_LOG_SYNC_HISTORY (v_sync_id, v_recnum, affected_v_recnum, c_action_type) values (1, 111, ?, 'DELETE')",
                            recordNumber);
            List<String> cprnumbersFromDeletedContacts = lprdao.getCPRnumbersFromDeletedContacts();
            assertEquals("Expect 1 CPR number", 1, cprnumbersFromDeletedContacts.size());
            assertEquals(cpr, cprnumbersFromDeletedContacts.get(0));

            // check delete_implicit
            minipasJdbcTemplate
                    .update("insert into T_ADM (v_recnum, v_cpr) values (?, ?)", new Long(recordNumber), cpr2);
            minipasJdbcTemplate.update("insert into T_LOG_SYNC (v_sync_id, start_time, end_time) values (?, ?, ?)", 8,
                    new Date(), new Date());
            minipasJdbcTemplate
                    .update("insert into T_LOG_SYNC_HISTORY (v_sync_id, v_recnum, affected_v_recnum, c_action_type) values (8, 111, ?, 'DELETE_IMPLICIT')",
                            recordNumber);
            cprnumbersFromDeletedContacts = lprdao.getCPRnumbersFromDeletedContacts();
            assertEquals("Expect 2 CPR number", 2, cprnumbersFromDeletedContacts.size());
            assertEquals(cpr, cprnumbersFromDeletedContacts.get(0));
            assertEquals(cpr2, cprnumbersFromDeletedContacts.get(1));
        } finally {
            minipasJdbcTemplate.update("delete from T_ADM where v_recnum = ?", recordNumber);
            minipasJdbcTemplate.update("delete from T_LOG_SYNC where v_sync_id = ?", 8);
            minipasJdbcTemplate.update("delete from T_LOG_SYNC_HISTORY where v_recnum = ?", recordNumber);
        }
    }
}
