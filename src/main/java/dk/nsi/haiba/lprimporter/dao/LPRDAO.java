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
import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.status.ImportStatus.Outcome;

public interface LPRDAO {
	/**
	 * Fetches a list of CPRnumbers where contacts are deleted, they need to be recalculated
	 * 
	 * @param syncId - the current synchronisationid to lookup deleted contacts.
	 * @return A list of CPRnumbers as String
	 * @throws DAOException if something goes wrong in the process
	 */
	public List<String> getCPRnumbersFromDeletedContacts(long syncId) throws DAOException;

	/**
	 * Fetches a list of CPRnumbers where contacts are deleted, they need to be recalculated, assuming the latest sync id
	 * 
	 * @return A list of CPRnumbers as String
	 * @throws DAOException if something goes wrong in the process
	 */
	public List<String> getCPRnumbersFromDeletedContacts() throws DAOException;

	/**
	 * Fetches a list of CPRnumbers that have not been processed (Where the Import date is empty)
	 * 
	 * @param batchsize
	 * @return A list of CPRnumbers as String
	 * @throws DAOException if something goes wrong in the process
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
	 * Updates the import timestamp in the T_ADM table for the given reference
	 * 
	 * @param  The refernce from the LPR Contact
	 * @param  The Outcome of the Import (Success Or Failure)
	 * @throws DAOException if something goes wrong in the process
	 */
	public void updateImportTime(LPRReference lprReference, Outcome outcome);

	
	/**
	 * Checks if there are any unprocessed cprnumbers in the T_ADM table
	 * 
	 * @throws DAOException if something goes wrong in the process
	 */
	public boolean hasUnprocessedCPRnumbers();
	
	/**
	 * Checks if the LPR database is ready for import by checking the end_time for the last v_sync_id in the T_LOG_SYNC table
	 * 
	 * @return the latest v_sync_id
	 * @throws DAOException if something goes wrong in the process
	 */
	public long isdatabaseReadyForImport();
}