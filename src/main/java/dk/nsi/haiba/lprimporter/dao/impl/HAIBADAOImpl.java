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
package dk.nsi.haiba.lprimporter.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import dk.nsi.haiba.lprimporter.dao.CommonDAO;
import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.exception.DAOException;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.model.haiba.Diagnose;
import dk.nsi.haiba.lprimporter.model.haiba.Indlaeggelse;
import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;
import dk.nsi.haiba.lprimporter.model.haiba.Procedure;
import dk.nsi.haiba.lprimporter.model.haiba.ShakRegionValues;
import dk.nsi.haiba.lprimporter.model.haiba.Statistics;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRDiagnose;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;
import dk.nsi.haiba.lprimporter.rules.BusinessRuleError;

public class HAIBADAOImpl extends CommonDAO implements HAIBADAO {
    private static Log log = new Log(Logger.getLogger(HAIBADAOImpl.class));

    @Autowired
    @Qualifier("haibaJdbcTemplate")
    JdbcTemplate jdbc;

    @Value("${jdbc.haibatableprefix:}")
    String tableprefix;

    @Value("${jdbc.fgrtableprefix:fgr.}")
    String fgrtableprefix;

    @Override
    public void saveIndlaeggelsesForloeb(List<Indlaeggelse> indlaeggelser) throws DAOException {
        List<Long> indlaeggelserInForloeb = new ArrayList<Long>();

        try {
            log.debug("* Inserting Indlaeggelsesforloeb");
            for (Indlaeggelse indlaeggelse : indlaeggelser) {

                final String sql = "INSERT INTO " + tableprefix + "Indlaeggelser (CPR, Sygehuskode, Afdelingskode, Indlaeggelsesdatotid, Udskrivningsdatotid, aktuel) VALUES (?,?,?,?,?,?)";

                final Object[] args = new Object[] { 
                        indlaeggelse.getCpr(), 
                        indlaeggelse.getSygehusCode(),
                        indlaeggelse.getAfdelingsCode(), 
                        indlaeggelse.getIndlaeggelsesDatetime(),
                        indlaeggelse.getUdskrivningsDatetime(),
                        indlaeggelse.isAktuel() ? new Integer(1) : new Integer(0) };

                long indlaeggelsesId = -1;
                if (MYSQL.equals(getDialect())) {
                    KeyHolder keyHolder = new GeneratedKeyHolder();
                    jdbc.update(new PreparedStatementCreator() {
                        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
                            for (int i = 0; i < args.length; i++) {
                                ps.setObject(i + 1, args[i]);
                            }
                            return ps;
                        }
                    }, keyHolder);
                    indlaeggelsesId = keyHolder.getKey().longValue();
                } else if (MSSQL.equals(getDialect())) {
                    jdbc.update(sql, args);
                    indlaeggelsesId = jdbc.queryForLong("SELECT @@IDENTITY");
                } else {
                    throw new DAOException("Unknown SQL dialect: " + getDialect());
                }
                indlaeggelserInForloeb.add(new Long(indlaeggelsesId));

                saveDiagnoses(indlaeggelse.getDiagnoses(), indlaeggelsesId);
                saveProcedures(indlaeggelse.getProcedures(), indlaeggelsesId);
                saveLPRReferences(indlaeggelse.getLprReferencer(), indlaeggelsesId);
            }
            saveForloeb(indlaeggelserInForloeb);
            log.debug("** Inserted Indlaeggelsesforloeb");
        } catch (DataAccessException e) {
            throw new DAOException(e.getMessage(), e);
        }

    }

    private void saveForloeb(List<Long> indlaeggelserInForloeb) {
        final String sql = "INSERT INTO " + tableprefix + "Indlaeggelsesforloeb (IndlaeggelsesID) VALUES (?)";
        String sqlWithReference = "INSERT INTO " + tableprefix + "Indlaeggelsesforloeb (IndlaeggelsesforloebID,IndlaeggelsesID) VALUES (?,?)";

        boolean first = true;
        long sequenceId = 0;

        for (Long indlaeggelsesId : indlaeggelserInForloeb) {
            if (first) {
                final Object[] args = new Object[] { indlaeggelsesId };

                if (MYSQL.equals(getDialect())) {
                    KeyHolder keyHolder = new GeneratedKeyHolder();
                    jdbc.update(new PreparedStatementCreator() {
                        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
                            for (int i = 0; i < args.length; i++) {
                                ps.setObject(i + 1, args[i]);
                            }
                            return ps;
                        }
                    }, keyHolder);
                    sequenceId = keyHolder.getKey().longValue();
                } else if (MSSQL.equals(getDialect())) {
                    jdbc.update(sql, args);
                    sequenceId = jdbc.queryForLong("SELECT @@IDENTITY");
                } else {
                    throw new DAOException("Unknown SQL dialect: " + getDialect());
                }
                jdbc.update("UPDATE " + tableprefix + "Indlaeggelsesforloeb SET IndlaeggelsesforloebID=? WHERE ID=?", sequenceId, sequenceId);

                first = false;
                continue;
            }
            jdbc.update(sqlWithReference, sequenceId, indlaeggelsesId);
        }
    }

    private void saveLPRReferences(List<LPRReference> lprReferencer, long indlaeggelsesId) {
        String sql = "INSERT INTO " + tableprefix + "LPR_Reference (IndlaeggelsesID, LPR_recordnummer, LPR_dbid) VALUES (?, ?, ?)";

        for (LPRReference ref : lprReferencer) {
            jdbc.update(sql, indlaeggelsesId, ref.getLprRecordNumber(), ref.getDbId());
        }
    }

    private void saveProcedures(List<Procedure> procedures, long indlaeggelsesId) {
        String sql = "INSERT INTO " + tableprefix + "Procedurer (IndlaeggelsesID, Procedurekode, Proceduretype, Tillaegsprocedurekode, Sygehuskode, Afdelingskode, Proceduredatotid) VALUES (?, ?, ?, ?, ?, ?, ?)";

        for (Procedure p : procedures) {
            jdbc.update(sql, 
                    indlaeggelsesId, 
                    p.getProcedureCode(), 
                    p.getProcedureType(), 
                    p.getTillaegsProcedureCode(),
                    p.getSygehusCode(), 
                    p.getAfdelingsCode(), 
                    p.getProcedureDatetime());
        }
    }

    private void saveDiagnoses(List<Diagnose> diagnoses, long indlaeggelsesId) {
        String sql = "INSERT INTO " + tableprefix + "Diagnoser (IndlaeggelsesID, Diagnoseskode, Diagnosetype, Tillaegsdiagnose) VALUES (?, ?, ?, ?)";

        for (Diagnose d : diagnoses) {
            jdbc.update(sql, indlaeggelsesId, d.getDiagnoseCode(), d.getDiagnoseType(), d.getTillaegsDiagnose());
        }
    }

    @Override
    public void saveBusinessRuleError(BusinessRuleError error) throws DAOException {
        if (error == null) {
            throw new DAOException("BusinessRuleError must be set");
        }

        String sql = "INSERT INTO " + tableprefix + "RegelFejlbeskeder (LPR_dbid, LPR_recordnummer, AfbrudtForretningsregel, Fejlbeskrivelse, Fejltidspunkt) VALUES (?, ?, ?, ?, ?)";
        try {
            jdbc.update(sql, error.getDbId(), error.getLprReference(), error.getAbortedRuleName(), error.getDescription(), new Date());
        } catch (DataAccessException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public String getSygehusInitials(String sygehuscode, String afdelingsCode, Date in) throws DAOException {
        // TODO - this can be cached
        String sql = null;
        if (MYSQL.equals(getDialect())) {
            sql = "SELECT Navn FROM klass_shak WHERE Nummer=? AND ValidFrom <= ? AND ValidTo >= ?";
        } else {
            // MSSQL
            sql = "SELECT Navn FROM " + fgrtableprefix + "klass_shak WHERE Nummer=? AND ValidFrom <= ? AND ValidTo >= ?";
        }

        try {
            String name = jdbc.queryForObject(sql, new Object[] { sygehuscode + afdelingsCode, in, in }, String.class);
            if (name != null && name.length() > 3) {
                return name.substring(0, 3);
            } else {
                return name;
            }
        } catch (EmptyResultDataAccessException e) {
            // no name found
            log.warn("No SygehusInitials found for Code:" + sygehuscode + ", department:" + afdelingsCode + " and date:" + in);
            return "";
        } catch (RuntimeException e) {
            throw new DAOException("Error Fetching initials for hospital from FGR", e);
        }
    }

    @Override
    public void prepareCPRNumberForImport(String cpr) {
        boolean cprExists = false;

        String sql = null;
        if (MYSQL.equals(getDialect())) {
            sql = "SELECT IndlaeggelsesID FROM Indlaeggelser where cpr=? LIMIT 1";
        } else {
            // MSSQL
            sql = "SELECT TOP 1 IndlaeggelsesID FROM " + tableprefix + "Indlaeggelser where cpr=?";
        }

        try {
            jdbc.queryForLong(sql, new Object[] { cpr });
            cprExists = true;
        } catch (EmptyResultDataAccessException e) {
            // ignore - no CPR exists
        } catch (RuntimeException e) {
            throw new DAOException("Error Fetching CPR number from Indlaeggelser", e);
        }

        if (cprExists) {
            // delete earlier processed data from HAIBA indlaeggelses tables.
            jdbc.update("DELETE FROM " + tableprefix + "Diagnoser WHERE indlaeggelsesID IN (SELECT indlaeggelsesID FROM Indlaeggelser WHERE cpr=?)", cpr);
            jdbc.update("DELETE FROM " + tableprefix + "Procedurer WHERE indlaeggelsesID IN (SELECT indlaeggelsesID FROM Indlaeggelser WHERE cpr=?)", cpr);
            jdbc.update("DELETE FROM " + tableprefix + "Indlaeggelsesforloeb WHERE indlaeggelsesID IN (SELECT indlaeggelsesID FROM Indlaeggelser WHERE cpr=?)", cpr);
            jdbc.update("DELETE FROM " + tableprefix + "LPR_Reference WHERE indlaeggelsesID IN (SELECT indlaeggelsesID FROM Indlaeggelser WHERE cpr=?)", cpr);
            jdbc.update("DELETE FROM " + tableprefix + "Indlaeggelser WHERE cpr=?", cpr);
            // delete ambulant contacts
            jdbc.update("DELETE FROM " + tableprefix + "AmbulantDiagnoser WHERE AmbulantKontaktId IN (SELECT ambulantKontaktId FROM AmbulantKontakt WHERE cpr=?)", cpr);
            jdbc.update("DELETE FROM " + tableprefix + "AmbulantProcedurer WHERE AmbulantKontaktId IN (SELECT ambulantKontaktId FROM AmbulantKontakt WHERE cpr=?)", cpr);
            jdbc.update("DELETE FROM " + tableprefix + "AmbulantLPR_Reference WHERE AmbulantKontaktId IN (SELECT ambulantKontaktId FROM AmbulantKontakt WHERE cpr=?)", cpr);
            jdbc.update("DELETE FROM " + tableprefix + "AmbulantKontakt WHERE cpr=?", cpr);
        }
    }

    @Override
    public List<String> getCurrentPatients() {
        String sql = "SELECT distinct cpr FROM " + tableprefix + "Indlaeggelser WHERE Aktuel = 1";
        String sql2 = "SELECT distinct cpr FROM " + tableprefix + "AmbulantKontakt WHERE Aktuel = 1";

        List<String> currentPatientCPRNumbers = new ArrayList<String>();
        try {
            currentPatientCPRNumbers = jdbc.queryForList(sql, String.class);
            currentPatientCPRNumbers.addAll(jdbc.queryForList(sql2, String.class));
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching list of current patients", e);
        }
        return currentPatientCPRNumbers;
    }

    @Override
    public void saveAmbulantIndlaeggelser(List<Administration> contacts) throws DAOException {
        try {
            log.debug("* Inserting ambulant contact");
            for (Administration contact : contacts) {

                final String sql = "INSERT INTO " + tableprefix + "AmbulantKontakt (CPR, Sygehuskode, Afdelingskode, Indlaeggelsesdatotid, Udskrivningsdatotid, aktuel) VALUES (?,?,?,?,?,?)";

                final Object[] args = new Object[] { 
                        contact.getCpr(), 
                        contact.getSygehusCode(),
                        contact.getAfdelingsCode(), 
                        contact.getIndlaeggelsesDatetime(),
                        contact.getUdskrivningsDatetime(), 
                        contact.isCurrentPatient() ? new Integer(1) : new Integer(0) };

                long ambulantContactId = -1;
                if (MYSQL.equals(getDialect())) {
                    KeyHolder keyHolder = new GeneratedKeyHolder();
                    jdbc.update(new PreparedStatementCreator() {
                        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
                            for (int i = 0; i < args.length; i++) {
                                ps.setObject(i + 1, args[i]);
                            }
                            return ps;
                        }
                    }, keyHolder);
                    ambulantContactId = keyHolder.getKey().longValue();
                } else if (MSSQL.equals(getDialect())) {
                    jdbc.update(sql, args);
                    ambulantContactId = jdbc.queryForLong("SELECT @@IDENTITY");
                } else {
                    throw new DAOException("Unknown SQL dialect: " + getDialect());
                }
                log.trace("ambulantContactId is " + ambulantContactId);

                saveAmbulantDiagnoses(contact.getLprDiagnoses(), ambulantContactId);
                saveAmbulantProcedures(contact.getLprProcedures(), ambulantContactId);
                saveAmbulantLPRReferences(contact.getLprReferencer(), ambulantContactId);
            }
            log.debug("** Inserted ambulant contact");
        } catch (DataAccessException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    private void saveAmbulantLPRReferences(List<LPRReference> lprReferencer, long ambulantContactId) {
        String sql = "INSERT INTO " + tableprefix + "AmbulantLPR_Reference (AmbulantKontaktID, LPR_recordnummer, LPR_dbid) VALUES (?, ?, ?)";

        for (LPRReference ref : lprReferencer) {
            jdbc.update(sql, ambulantContactId, ref.getLprRecordNumber(), ref.getDbId());
        }
    }

    private void saveAmbulantProcedures(List<LPRProcedure> procedures, long ambulantContactId) {
        String sql = "INSERT INTO " + tableprefix + "AmbulantProcedurer (AmbulantKontaktID, Procedurekode, Proceduretype, Tillaegsprocedurekode, Sygehuskode, Afdelingskode, Proceduredatotid) VALUES (?, ?, ?, ?, ?, ?, ?)";

        for (LPRProcedure p : procedures) {
            jdbc.update(sql, 
                    ambulantContactId, 
                    p.getProcedureCode(), 
                    p.getProcedureType(),
                    p.getTillaegsProcedureCode(), 
                    p.getSygehusCode(), 
                    p.getAfdelingsCode(), 
                    p.getProcedureDatetime());
        }
    }

    private void saveAmbulantDiagnoses(List<LPRDiagnose> diagnoses, long ambulantContactId) {
        String sql = "INSERT INTO " + tableprefix + "AmbulantDiagnoser (AmbulantKontaktID, Diagnoseskode, Diagnosetype, Tillaegsdiagnose) VALUES (?, ?, ?, ?)";

        for (LPRDiagnose d : diagnoses) {
            jdbc.update(sql, ambulantContactId, d.getDiagnoseCode(), d.getDiagnoseType(), d.getTillaegsDiagnose());
        }
    }

    @Override
    public void saveStatistics(Statistics statistics) {
        String sql = "INSERT INTO "
                + tableprefix
                + "Statistik (KoerselsDato,AntalKontakter,AntalCPRNumre,AntalKontakterFejlet,AntalCPRNumreEksporteret,AntalIndlaeggelserEksporteret,AntalForloebEksporteret,AntalAmbulanteKontakterEksporteret,AntalCPRNumreMedSlettedeKontakterBehandlet,AntalNuvaerendePatienterBehandlet,Regel1,Regel2,Regel3,Regel4,Regel5,Regel6,Regel7,Regel8,Regel9,Regel10,Regel11,Regel12,Regel13,Regel14) "
                + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        jdbc.update(sql, 
                statistics.getDate(), 
                statistics.contactCounter, 
                statistics.cprCounter,
                statistics.contactErrorCounter, 
                statistics.cprExportedCounter, 
                statistics.admissionsExportedCounter,
                statistics.admissionsSeriesExportedCounter, 
                statistics.ambulantContactsExportedCounter,
                statistics.cprNumbersWithDeletedContactsCounter, 
                statistics.currentPatientsCounter,
                statistics.rule1Counter, 
                statistics.rule2Counter, 
                statistics.rule3Counter, 
                statistics.rule4Counter,
                statistics.rule5Counter, 
                statistics.rule6Counter, 
                statistics.rule7Counter, 
                statistics.rule8Counter,
                statistics.rule9Counter, 
                statistics.rule10Counter, 
                statistics.rule11Counter, 
                statistics.rule12Counter,
                statistics.rule13Counter, 
                statistics.rule14Counter);
    }

    @Override
    public Collection<ShakRegionValues> getShakRegionValuesForSygehusNumre(Collection<String> sygehusNumre) {
        List<ShakRegionValues> returnValue = new ArrayList<ShakRegionValues>();
        for (String nummer : sygehusNumre) {
            // 3800-sygehuse has an extra sygehus extension that doesn't exist in the shak table
            String truncatedSygehusNummer = nummer.length() > 4 ? nummer.substring(0, 4) : nummer;
            RowMapper<ShakRegionValues> rowMapper = new RowMapper<ShakRegionValues>() {
                @Override
                public ShakRegionValues mapRow(ResultSet rs, int rowNum) throws SQLException {
                    ShakRegionValues returnValue = new ShakRegionValues();
                    returnValue.setEjerForhold(rs.getString("Ejerforhold"));
                    returnValue.setInstitutionsArt(rs.getString("Institutionsart"));
                    returnValue.setRegionsKode(rs.getString("Regionskode"));
                    return returnValue;
                }
            };
            try {
                ShakRegionValues shakRegionValues = jdbc.queryForObject(
                        "SELECT DISTINCT Ejerforhold,Institutionsart,Regionskode FROM klass_shak WHERE nummer = ?",
                        rowMapper, truncatedSygehusNummer);
                // but keep the original nummer here
                shakRegionValues.setNummer(nummer);
                returnValue.add(shakRegionValues);
            } catch (RuntimeException e) {
                log.error("Error fetching shakregion values from sygehus nummer " + nummer, e);
            }
        }
        return returnValue;
    }
}
