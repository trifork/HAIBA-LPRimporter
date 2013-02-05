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
package dk.nsi.haiba.lprimporter.rules;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.model.haiba.Indlaeggelse;
import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;

/*
 * This is the 8. rule to be applied to LPR data
 * It takes a list of admissions from a single CPR number, and processes the data with the connect admissions rule
 * See the solution document for details about this rule.
 */public class ConnectAdmissionsRule implements LPRRule {
	
	private List<Indlaeggelse> admissions;
	
	@Autowired
	HAIBADAO haibaDao;
	
	@Autowired
	LPRDAO lprDao;

	@Override
	public LPRRule doProcessing() {

		// Rules are complete, update LPR with the import timestamp so they are not imported again

		// TODO - this is a stub
		for (Indlaeggelse admission : admissions) {
			for (LPRReference lprRef : admission.getLprReferencer()) {
				lprDao.updateImportTime(lprRef.getLprRecordNumber());
			}
		}
		haibaDao.saveIndlaeggelsesForloeb(admissions);
		
		return null;
	}

	/*
	 * Package scope for unittesting purpose
	 */
	List<Indlaeggelse> getAdmissions() {
		return admissions;
	}

	public void setAdmissions(List<Indlaeggelse> admissions) {
		this.admissions = admissions;
	}

}
