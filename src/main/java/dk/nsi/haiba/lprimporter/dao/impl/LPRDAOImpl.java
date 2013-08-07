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
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import dk.nsi.haiba.lprimporter.dao.CommonDAO;
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.exception.DAOException;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.status.ImportStatus.Outcome;

public class LPRDAOImpl extends CommonDAO implements LPRDAO {

	private static Log log = new Log(Logger.getLogger(LPRDAOImpl.class));

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public boolean hasUnprocessedCPRnumbers() throws DAOException {
		String sql = null;
		if(MYSQL.equals(getDialect())) {
			sql = "SELECT V_RECNUM FROM T_ADM WHERE D_IMPORTDTO IS NULL LIMIT 1";
		} else {
			// MSSQL
			sql = "SELECT TOP 1 V_RECNUM FROM haiba_replica.T_ADM WHERE D_IMPORTDTO IS NULL";
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
			sql = "SELECT TOP "+batchsize+" v_cpr FROM haiba_replica.T_ADM WHERE D_IMPORTDTO IS NULL GROUP BY v_cpr";
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
			String sql = null;
			if(MYSQL.equals(getDialect())) {
				sql = "SELECT a.v_recnum,a.c_sgh,a.c_afd,a.c_pattype,a.v_cpr,a.d_inddto,a.d_uddto," +
			    		"k.c_kode,k.c_tilkode,k.c_kodeart,k.d_pdto,k.c_psgh,k.c_pafd,k.v_type " +
			    		"FROM T_ADM a " +
			    		"left join T_KODER k on a.v_recnum = k.v_recnum " +
			    		"WHERE a.v_cpr=?";
			} else {
				// MSSQL
				sql = "SELECT a.v_recnum,a.c_sgh,a.c_afd,a.c_pattype,a.v_cpr,a.d_inddto,a.d_uddto," +
			    		"k.c_kode,k.c_tilkode,k.c_kodeart,k.d_pdto,k.c_psgh,k.c_pafd,k.v_type " +
			    		"FROM haiba_replica.T_ADM a " +
			    		"left join haiba_replica.T_KODER k on a.v_recnum = k.v_recnum " +
			    		"WHERE a.v_cpr=?";
			}
		    lprContacts = jdbcTemplate.query(sql, new Object[]{cpr}, new LPRContactRowMapper());
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching contacts from LPR", e);
        }
	    
	    lprContacts = mergeContacts(lprContacts);
	    
		log.trace("END getContactsByCPR");
	    return lprContacts;
	}

	private List<Administration> mergeContacts(List<Administration> lprContacts) {
		
		if(lprContacts.size() == 1) {
			return lprContacts;
		}
		
		Map<Long, Administration> contacts = new HashMap<Long, Administration>();
		for (Administration adm : lprContacts) {
			Long recordNumber = new Long(adm.getRecordNumber());
			
			Administration c = contacts.get(recordNumber);
			if(c == null) {
				contacts.put(recordNumber, adm);
			} else {
				c.getLprDiagnoses().addAll(adm.getLprDiagnoses());
				c.getLprProcedures().addAll(adm.getLprProcedures());
			}
		}
		return new ArrayList<Administration>(contacts.values()) ;
	}

	@Override
	public void updateImportTime(long recordNumber, Outcome status) {
		log.trace("BEGIN updateImportTime");
		
		String sql = null;
		if(MYSQL.equals(getDialect())) {
			sql = "update T_ADM set D_IMPORTDTO = ?, V_STATUS =? WHERE V_RECNUM = ?";
		} else {
			// MSSQL
			sql = "update haiba_replica.T_ADM set D_IMPORTDTO = ?, V_STATUS =? WHERE V_RECNUM = ?";
		}

	    try {
	    	jdbcTemplate.update(sql, new Object[] {new Date(), status.toString(), new Long(recordNumber)});
        } catch (RuntimeException e) {
            throw new DAOException("Error updating import timestamp in LPR", e);
        }
		log.trace("END updateImportTime");
	}

	@Override
	public long isdatabaseReadyForImport() {
		
		try {
			String sql1 = null;
			String sql2 = null;
			if(MYSQL.equals(getDialect())) {
				sql1 = "select max(v_sync_id) from T_LOG_SYNC";
				sql2 = "select v_sync_id from T_LOG_SYNC where v_sync_id =  ? and end_time is not null";
			} else {
				// MSSQL
				sql1 = "select max(v_sync_id) from haiba_etl.T_LOG_SYNC";
				sql2 = "select v_sync_id from haiba_etl.T_LOG_SYNC where v_sync_id =  ? and end_time is not null";
			}
			
			long maxSyncId = jdbcTemplate.queryForLong(sql1);
			jdbcTemplate.queryForLong(sql2, maxSyncId);
	    	return maxSyncId;
        } catch(EmptyResultDataAccessException e) {
        	// LPR is not ready for Import
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching contacts from LPR", e);
        }
    	return 0;
	}

	@Override
	public List<String> getCPRnumbersFromDeletedContacts(long syncId) throws DAOException {
		log.trace("BEGIN getCPRnumbersFromDeletedContacts");
		String sql = null;
		if(MYSQL.equals(getDialect())) {
			sql = "select v_cpr from T_ADM where v_recnum in ("+
					"select AFFECTED_V_RECNUM from T_LOG_SYNC_HISTORY where "+
					" (C_ACTION_TYPE = 'DELETE' or C_ACTION_TYPE = 'DELETE_IMPLICIT') and v_sync_id = ?)";
		} else {
			// MSSQL
			sql = "select v_cpr from haiba_replica.T_ADM where v_recnum in ("+
					"select AFFECTED_V_RECNUM from haiba_etl.T_LOG_SYNC_HISTORY where "+
					" (C_ACTION_TYPE = 'DELETE' or C_ACTION_TYPE = 'DELETE_IMPLICIT') and v_sync_id = ?)";
		}
		
		List<String> cprNumbersWithDeletedContacts = new ArrayList<String>();
	    try {
	    	cprNumbersWithDeletedContacts = jdbcTemplate.queryForList(sql,new Object[] {new Long(syncId)}, String.class);
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching CPR numbers from deleted contacts in LPR", e);
        }
		log.trace("END getCPRnumbersFromDeletedContacts");
	    return cprNumbersWithDeletedContacts;
	}

}
