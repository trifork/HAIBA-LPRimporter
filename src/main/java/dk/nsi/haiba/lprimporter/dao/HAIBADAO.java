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

import java.util.Date;
import java.util.List;

import dk.nsi.haiba.lprimporter.exception.DAOException;
import dk.nsi.haiba.lprimporter.model.haiba.Indlaeggelse;
import dk.nsi.haiba.lprimporter.rules.BusinessRuleError;

public interface HAIBADAO {

	/**
	 * Takes a list of indlaeggelser and saves it in the database
	 * 
	 * @param A list of Indlaeggelse objects
	 * @throws DAOException
	 *             if something goes wrong in the process
	 */
	public void saveIndlaeggelsesForloeb(List<Indlaeggelse> indlaeggelser) throws DAOException;

	/**
	 * Takes a BusinessRuleError and saves it to the RegelFejlbeskedertable in the database
	 * 
	 * @param BusinessRuleError
	 * @throws DAOException
	 *             if something goes wrong in the process
	 */
	public void saveBusinessRuleError(BusinessRuleError error) throws DAOException;
	
	/**
	 * Special operation, for taking care of sygehuscodes from Region Sjælland, where they all are set to 3800
	 * The first 3 letters for the field V_AFDNAVN is returned, because they are the initials for the actual hospital.
	 * Se solutionspecification for details.
	 * 
	 * @param sygehuscode The SKS code for the hospital
	 * @param afdelingscode The SKS code for the department on the hospital
	 * @param in The contact in date, because SKS codes can change value with time.
	 * @return hospitalInitials, the 3 first letters from the field V_AFDNAVN 
	 * @throws DAOException if something goes wrong in the process
	 */
	public String getSygehusInitials(String sygehuscode, String afdelingsCode, Date in) throws DAOException;

	/**
	 * removes all data from the HAIBA indlaeggelses tables for current patient
	 * This method is used before applying businessrules
	 * 
	 * @param the current patient CPR number
	 * @throws DAOException if something goes wrong in the process
	 */
	public void prepareCPRNumberForImport(String cpr);
}