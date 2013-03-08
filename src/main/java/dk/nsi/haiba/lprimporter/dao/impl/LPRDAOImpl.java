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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import dk.nsi.haiba.lprimporter.dao.CommonDAO;
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.exception.DAOException;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRDiagnose;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;
import dk.nsi.haiba.lprimporter.status.ImportStatus.Outcome;

public class LPRDAOImpl extends CommonDAO implements LPRDAO {

	private static Log log = new Log(Logger.getLogger(LPRDAOImpl.class));

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public boolean hasUnprocessedCPRnumbers() throws DAOException {
		String sql = null;
		if(MYSQL.equals(getDialect())) {
			sql = "SELECT K_RECNUM FROM T_ADM WHERE D_IMPORTDTO IS NULL LIMIT 1";
		} else {
			// MSSQL
			sql = "SELECT TOP 1 K_RECNUM FROM T_ADM WHERE D_IMPORTDTO IS NULL";
		}
		
	    try {
	    	jdbcTemplate.queryForLong(sql);
		    return true;
        } catch(EmptyResultDataAccessException e) {
        	// no unprocessed cprnumbers were found
        	return false;
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching contacts from LPR", e);
        }
	}
	
	@Override
	public List<String> getCPRnumberBatch(int batchsize) throws DAOException {
		log.trace("BEGIN getCPRnumberBatch");
		String sql = null;
		if(MYSQL.equals(getDialect())) {
			sql = "SELECT v_cpr FROM T_ADM WHERE D_IMPORTDTO IS NULL GROUP BY v_cpr LIMIT "+batchsize;
		} else {
			// MSSQL
			sql = "SELECT TOP "+batchsize+" v_cpr FROM T_ADM WHERE D_IMPORTDTO IS NULL GROUP BY v_cpr";
		}
		
		List<String> unprocessedCPRNumbers = new ArrayList<String>();
	    try {
	    	unprocessedCPRNumbers = jdbcTemplate.queryForList(sql, String.class);
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching contacts from LPR", e);
        }
		log.trace("END getCPRnumberBatch");
	    return unprocessedCPRNumbers;
	}

	@Override
	public List<Administration> getContactsByCPR(String cpr) throws DAOException {
		log.trace("BEGIN getContactsByCPR");
		List<Administration> lprContacts = new ArrayList<Administration>();
	    try {
		    lprContacts = jdbcTemplate.query("SELECT k_recnum,c_sgh,c_afd,c_pattype,v_cpr,d_inddto,d_uddto,v_indtime,v_udtime FROM T_ADM WHERE v_cpr=?", new Object[]{cpr}, new LPRContactRowMapper());
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching contacts from LPR", e);
        }
	    
	    // This is an optimization for avoiding lots of queries to the Procedure and Diagnosis table 
	    // now there is only one to each table for each CPR number
	    // the Diagnoses and Procedures are then sorted out in code which is faster in case there are more contacts with diagnoses and procedures.
	    
	    // Create "IN" string to the SQL queries
	    StringBuilder recordNumberInString = new StringBuilder("(");
	    boolean first = true;
	    for (Administration contact : lprContacts) {
	    	if(first) {
	    		recordNumberInString.append(contact.getRecordNumber());
	    		first = false;
	    		continue;
	    	}
    		recordNumberInString.append(",");
    		recordNumberInString.append(contact.getRecordNumber());
		}
		recordNumberInString.append(")");
		
		// save all contacts in a map to find them as easy as possible
		Map<Long, Administration> contacts = new HashMap<Long, Administration>();
		for (Administration administration : lprContacts) {
			contacts.put(administration.getRecordNumber(), administration);
		}
		
		// add each procedure to the corresponding contact
	    List<LPRProcedure> procedures = getProceduresByRecordnumber(recordNumberInString.toString());
	    for (LPRProcedure lprProcedure : procedures) {
	    	long recordNumber = lprProcedure.getRecordNumber();
	    	contacts.get(recordNumber).addLprProcedure(lprProcedure);
		}
	    
		// add each diagnosis to the corresponding contact
	    List<LPRDiagnose> diagnoses = getDiagnosesByRecordnumber(recordNumberInString.toString());
	    for (LPRDiagnose lprDiagnose : diagnoses) {
	    	long recordNumber = lprDiagnose.getRecordNumber();
	    	contacts.get(recordNumber).addLprDiagnose(lprDiagnose);
		}
	    
		log.trace("END getContactsByCPR");
	    return lprContacts;
	}

	@Override
	public List<LPRDiagnose> getDiagnosesByRecordnumber(String recordNumbers) throws DAOException {
		log.trace("BEGIN getDiagnosesByRecordnummer");
		List<LPRDiagnose> lprDiagnoses = new ArrayList<LPRDiagnose>();
	    try {
	    	lprDiagnoses = jdbcTemplate.query("SELECT v_recnum,c_diag,c_tildiag,c_diagtype FROM T_DIAG WHERE v_recnum in "+ recordNumbers, new LPRDiagnosisRowMapper());
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching diagnoses from LPR", e);
        }
		log.trace("END getDiagnosesByRecordnummer");
	    return lprDiagnoses;
	}

	@Override
	public List<LPRProcedure> getProceduresByRecordnumber(String recordNumbers) throws DAOException {
		log.trace("BEGIN getProceduresByRecordnummer");
		List<LPRProcedure> lprProcedures = new ArrayList<LPRProcedure>();
	    try {
	    	lprProcedures = jdbcTemplate.query("SELECT v_recnum,c_opr,c_tilopr,c_oprart,d_odto,v_otime,c_osgh,c_oafd FROM T_PROCEDURER WHERE v_recnum in "+ recordNumbers, new LPRProcedureRowMapper());
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching diagnoses from LPR", e);
        }
		log.trace("END getProceduresByRecordnummer");
	    return lprProcedures;
	}

	@Override
	public void updateImportTime(long recordNumber, Outcome status) {
		log.trace("BEGIN updateImportTime");
		String sql = "update T_ADM set D_IMPORTDTO = ?, V_STATUS =? WHERE K_RECNUM = ?";

	    try {
	    	jdbcTemplate.update(sql, new Object[] {new Date(), status.toString(), new Long(recordNumber)});
        } catch (RuntimeException e) {
            throw new DAOException("Error updating import timestamp in LPR", e);
        }
		log.trace("END updateImportTime");
	}

}
