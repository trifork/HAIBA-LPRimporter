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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import dk.nsi.haiba.lprimporter.dao.CommonDAO;
import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.exception.DAOException;
import dk.nsi.haiba.lprimporter.model.haiba.Diagnose;
import dk.nsi.haiba.lprimporter.model.haiba.Indlaeggelse;
import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;
import dk.nsi.haiba.lprimporter.model.haiba.Procedure;
import dk.nsi.haiba.lprimporter.rules.BusinessRuleError;

public class HAIBADAOImpl extends CommonDAO implements HAIBADAO {

	@Autowired
	@Qualifier("haibaJdbcTemplate")
	JdbcTemplate jdbc;

	@Override
	public void saveIndlaeggelsesForloeb(List<Indlaeggelse> indlaeggelser) throws DAOException {

		List<Long> indlaeggelserInForloeb = new ArrayList<Long>();
		
		try {
			for (Indlaeggelse indlaeggelse : indlaeggelser) {
				
				final String sql = "INSERT INTO Indlaeggelser (CPR, Sygehuskode, Afdelingskode, Indlaeggelsesdatotid, Udskrivningsdatotid, aktuel) VALUES (?,?,?,?,?,?)";				
				
				
				final Object[] args = new Object[] {
						indlaeggelse.getCpr(), 
						indlaeggelse.getSygehusCode(),
						indlaeggelse.getAfdelingsCode(),
						indlaeggelse.getIndlaeggelsesDatetime(),
						indlaeggelse.getUdskrivningsDatetime(),
						indlaeggelse.isAktuel()};
				

				long indlaeggelsesId = -1;
				if(MYSQL.equals(getDialect())) {
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
				} else if(MSSQL.equals(getDialect())) {
					jdbc.update(sql, args);
					indlaeggelsesId = jdbc.queryForLong("SELECT @@IDENTITY");
				} else {
					throw new DAOException("Unknown SQL dialect: "+ getDialect());
				}

		        indlaeggelserInForloeb.add(new Long(indlaeggelsesId));
		        
		        saveDiagnoses(indlaeggelse.getDiagnoses(), indlaeggelsesId);
		        saveProcedures(indlaeggelse.getProcedures(), indlaeggelsesId);
		        saveLPRReferences(indlaeggelse.getLprReferencer(), indlaeggelsesId);
			}
			saveForloeb(indlaeggelserInForloeb);
		} catch(DataAccessException e) {
			throw new DAOException(e.getMessage(), e);
		}

	}
	
	
	private void saveForloeb(List<Long> indlaeggelserInForloeb) {

		final String sql = "INSERT INTO Indlaeggelsesforloeb (IndlaeggelsesID) VALUES (?)";
		String sqlWithReference = "INSERT INTO Indlaeggelsesforloeb (IndlaeggelsesforloebID,IndlaeggelsesID) VALUES (?,?)";
		
		boolean first = true;
		long sequenceId = 0;
		
		for (Long indlaeggelsesId : indlaeggelserInForloeb) {
			if(first) {
				final Object[] args = new Object[] {indlaeggelsesId};

				if(MYSQL.equals(getDialect())) {
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
				} else if(MSSQL.equals(getDialect())) {
					jdbc.update(sql, args);
					sequenceId = jdbc.queryForLong("SELECT @@IDENTITY");
				} else {
					throw new DAOException("Unknown SQL dialect: "+ getDialect());
				}
				jdbc.update("update Indlaeggelsesforloeb set IndlaeggelsesforloebID=? where ID=?", sequenceId, sequenceId);
				
				first = false;
				continue;
			}
			jdbc.update(sqlWithReference, sequenceId, indlaeggelsesId);
		}
	}

	private void saveLPRReferences(List<LPRReference> lprReferencer, long indlaeggelsesId) {
		
		String sql = "INSERT INTO LPR_Reference (IndlaeggelsesID, LPR_recordnummer) VALUES (?, ?)";
		
		for (LPRReference ref : lprReferencer) {
			jdbc.update(sql, indlaeggelsesId, ref.getLprRecordNumber());
		}
	}


	private void saveProcedures(List<Procedure> procedures, long indlaeggelsesId) {
		
		String sql = "INSERT INTO Procedurer (IndlaeggelsesID, Procedurekode, Proceduretype, Tillaegsprocedurekode, Sygehuskode, Afdelingskode, Proceduredatotid) VALUES (?, ?, ?, ?, ?, ?, ?)";
		
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
		
		String sql = "INSERT INTO Diagnoser (IndlaeggelsesID, Diagnoseskode, Diagnosetype, Tillaegsdiagnose) VALUES (?, ?, ?, ?)";

		for (Diagnose d : diagnoses) {
			jdbc.update(sql, 
					indlaeggelsesId, 
					d.getDiagnoseCode(),
					d.getDiagnoseType(),
					d.getTillaegsDiagnose());
		}
	}


	@Override
	public void saveBusinessRuleError(BusinessRuleError error)
			throws DAOException {
		
		if(error == null) {
			throw new DAOException("BusinessRuleError must be set");
		}
		
		String sql = "INSERT INTO RegelFejlbeskeder (LPR_recordnummer, AfbrudtForretningsregel, Fejlbeskrivelse, Fejltidspunkt) VALUES (?, ?, ?, ?)";
		try {
			jdbc.update(sql, error.getLprReference(), error.getAbortedRuleName(), error.getDescription(), new Date()); 
		} catch(DataAccessException e) {
			throw new DAOException(e.getMessage(), e);
		}
	}


	@Override
	public String getSygehusInitials(String sygehuscode, String afdelingsCode, Date in) throws DAOException {
		
		// TODO - this can be cached
		
		String sql = null;
		if(MYSQL.equals(getDialect())) {
			sql = "SELECT V_AFDNAVN FROM T_AFDKLASSE where k_sgh=? and k_afd=? and k_fradto <= ? and d_tildto >= ?";
		} else {
			// MSSQL
			sql = "SELECT V_AFDNAVN FROM fgr.T_AFDKLASSE where k_sgh=? and k_afd=? and k_fradto <= ? and d_tildto >= ?";
		}

	    try {
	    	String name = jdbc.queryForObject(sql,new Object[]{sygehuscode, afdelingsCode, in, in}, String.class);
	    	if(name != null && name.length() > 3) {
	    		return name.substring(0, 3);
	    	} else {
	    		return name;
	    	}
        } catch(EmptyResultDataAccessException e) {
        	// no name found
        	return null;
        } catch (RuntimeException e) {
            throw new DAOException("Error Fetching initials for hospital from FGR", e);
        }
	}


	@Override
	public void prepareCPRNumberForImport(String cpr) {
		
		boolean cprExists = false;
		
		String sql = null;
		if(MYSQL.equals(getDialect())) {
			sql = "SELECT IndlaeggelsesID FROM Indlaeggelser where cpr=? LIMIT 1";
		} else {
			// MSSQL
			sql = "SELECT TOP 1 IndlaeggelsesID FROM Indlaeggelser where cpr=?";
		}

	    try {
	    	jdbc.queryForLong(sql,new Object[]{cpr});
	    	cprExists = true;
        } catch(EmptyResultDataAccessException e) {
        	// ignore - no CPR exists
        } catch (RuntimeException e) {
            throw new DAOException("Error Fetching CPR number from Indlaeggelser", e);
        }
		
	    if(cprExists) {
			// delete earlier processed data from HAIBA indlaeggelses tables.
			jdbc.update("DELETE FROM Diagnoser WHERE indlaeggelsesID IN (SELECT indlaeggelsesID FROM Indlaeggelser WHERE cpr=?)", cpr);
			jdbc.update("DELETE FROM Procedurer WHERE indlaeggelsesID IN (SELECT indlaeggelsesID FROM Indlaeggelser WHERE cpr=?)", cpr);
			jdbc.update("DELETE FROM Indlaeggelsesforloeb WHERE indlaeggelsesID IN (SELECT indlaeggelsesID FROM Indlaeggelser WHERE cpr=?)", cpr);
			jdbc.update("DELETE FROM LPR_Reference WHERE indlaeggelsesID IN (SELECT indlaeggelsesID FROM Indlaeggelser WHERE cpr=?)", cpr);
			jdbc.update("DELETE FROM Indlaeggelser WHERE cpr=?", cpr);
	    }
	    
	}
	
}
