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
package dk.nsi.haiba.lprimporter.dao;

import java.util.List;

import dk.nsi.haiba.lprimporter.exception.DAOException;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRDiagnose;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;
import dk.nsi.haiba.lprimporter.status.ImportStatus.Outcome;

public interface LPRDAO {

	/**
	 * Fetches a list of CPRnumbers that have not been processed (Where the Import date is empty)
	 * 
	 * @param batchsize
	 * @return A list of CPRnumbers as String
	 * @throws DAOException
	 *             if something goes wrong in the process
	 */
	public List<String> getCPRnumberBatch(int batchsize) throws DAOException;

	/**
	 * Fetches a list of {@link Administration} given the CPR number
	 * Fetches all contacts for the CPR number, as they all have to be re-processed every time there is a change
	 * 
	 * @param CPR
	 *            The CPR number of the LPR Contact
	 * @return {@link Administration} A list of contacts for a given CPR number
	 * @throws DAOException
	 *             if something goes wrong in the process
	 */
	public List<Administration> getContactsByCPR(String CPR) throws DAOException;

	/**
	 * Fetches a list of {@link LPRDiagnose} given the recordnummer number
	 * 
	 * @param 
	 *            The recordnummer from the LPR Contact
	 * @return {@link LPRDiagnose} A list of diagnoses for a given CPR number
	 * @throws DAOException
	 *             if something goes wrong in the process
	 */
	public List<LPRDiagnose> getDiagnosesByRecordnummer(long recordnummer) throws DAOException;

	/**
	 * Fetches a list of {@link LPRProcedure} given the recordnummer number
	 * 
	 * @param  The recordnummer from the LPR Contact
	 * @return {@link LPRProcedure} A list of procedures for a given CPR number
	 * @throws DAOException
	 *             if something goes wrong in the process
	 */
	public List<LPRProcedure> getProceduresByRecordnummer(long recordnummer) throws DAOException;

	
	/**
	 * Updates the import timstamp in the T_ADM table for the given recordnummer number
	 * 
	 * @param  The recordnummer from the LPR Contact
	 * @param  The Outcome of the Import (Success Or Failure)
	 * @throws DAOException if something goes wrong in the process
	 */
	public void updateImportTime(long recordNumber, Outcome outcome);

	
	/**
	 * Checks if there are any unprocessed cprnumbers in the T_ADM table
	 * 
	 * @throws DAOException if something goes wrong in the process
	 */
	public boolean hasUnprocessedCPRnumbers();
	
}