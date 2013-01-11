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
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.exception.DAOException;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRDiagnose;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;

public class LPRDAOImpl implements LPRDAO {

	@Autowired
	JdbcTemplate jdbcTemplate;

	// TODO - select SQL from the chosen dialect

	@Override
	public List<String> getUnprocessedCPRnumbers() throws DAOException {
		List<String> unprocessedCPRNumbers = new ArrayList<String>();
	    try {
	    	unprocessedCPRNumbers = jdbcTemplate.queryForList("SELECT v_cpr FROM T_ADM WHERE D_IMPORTDTO IS NULL GROUP BY v_cpr", String.class);
		    return unprocessedCPRNumbers;
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching contacts from LPR", e);
        }
	}

	@Override
	public List<Administration> getContactsByCPR(String cpr) throws DAOException {
		List<Administration> lprContacts = new ArrayList<Administration>();
	    try {
		    lprContacts = jdbcTemplate.query("SELECT * FROM T_ADM WHERE v_cpr=?", new Object[]{cpr}, new LPRContactRowMapper());
		    return lprContacts;
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching contacts from LPR", e);
        }
	}

	@Override
	public List<LPRDiagnose> getDiagnosesByRecordnummer(long recordnummer) throws DAOException {
		List<LPRDiagnose> lprDiagnoses = new ArrayList<LPRDiagnose>();
	    try {
	    	lprDiagnoses = jdbcTemplate.query("SELECT * FROM T_DIAG WHERE v_recnum=?", new Object[]{recordnummer}, new LPRDiagnosisRowMapper());
		    return lprDiagnoses;
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching diagnoses from LPR", e);
        }
	}

	@Override
	public List<LPRProcedure> getProceduresByRecordnummer(long recordnummer) throws DAOException {
		List<LPRProcedure> lprProcedures = new ArrayList<LPRProcedure>();
	    try {
	    	lprProcedures = jdbcTemplate.query("SELECT * FROM T_PROCEDURER WHERE v_recnum=?", new Object[]{recordnummer}, new LPRProcedureRowMapper());
		    return lprProcedures;
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching diagnoses from LPR", e);
        }
	}

}
