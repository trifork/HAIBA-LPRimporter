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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.model.haiba.Diagnose;
import dk.nsi.haiba.lprimporter.model.haiba.Indlaeggelse;
import dk.nsi.haiba.lprimporter.model.haiba.IndlaeggelsesForloeb;
import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;
import dk.nsi.haiba.lprimporter.model.haiba.Procedure;

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

		if(admissions.size() == 1) {
			// only 1 connection
			saveConnectedAdmissions(admissions);
			return null; // end rules processing
		} 
		
		// Sort admissions by in date
		Collections.sort(admissions, new IndlaeggelseInDateComparator());

		// first check if admissions are connected and on the same hospital
		List<Indlaeggelse> connectedAdmissionsFromTheSameHospital = new ArrayList<Indlaeggelse>(); 
		Indlaeggelse previousAdmission = null;
		for (Indlaeggelse admission : admissions) {
			if(previousAdmission == null) {
				connectedAdmissionsFromTheSameHospital.add(admission);
				previousAdmission = admission;
				continue;
			}
			
			DateTime previousOut = new DateTime(previousAdmission.getUdskrivningsDatetime());
			DateTime currentIn = new DateTime(admission.getIndlaeggelsesDatetime());
			
			if(previousAdmission.getSygehusCode().equals(admission.getSygehusCode()) && previousOut.isEqual(currentIn)) {
				// add admission to the connected list
				connectedAdmissionsFromTheSameHospital.add(admission);
			} else {
				// admission is not from the same hospital, or there is a gap between previousOut and currentIn
				// so save the connected list and start a new one.
				saveConnectedAdmissions(connectedAdmissionsFromTheSameHospital);
				connectedAdmissionsFromTheSameHospital.clear();
				connectedAdmissionsFromTheSameHospital.add(admission);
			}
		}
		// loop has ended, save the list containing the last admission(s) from the loop
		saveConnectedAdmissions(connectedAdmissionsFromTheSameHospital);
		
		
		// Then just check if admissions are connected and add a new IndlaeggelsesForloeb
		List<Indlaeggelse> connectedAdmissions = new ArrayList<Indlaeggelse>(); 
		previousAdmission = null;
		for (Indlaeggelse admission : admissions) {
			if(previousAdmission == null) {
				connectedAdmissions.add(admission);
				previousAdmission = admission;
				continue;
			}
			
			DateTime previousOut = new DateTime(previousAdmission.getUdskrivningsDatetime());
			DateTime currentIn = new DateTime(admission.getIndlaeggelsesDatetime());
			
			if(previousOut.isEqual(currentIn)) {
				// add admission to the connected list
				connectedAdmissions.add(admission);
			} else {
				// admission is not from the same hospital, or there is a gap between previousOut and currentIn
				// so save the connected list and start a new one.
				saveConnectedAdmissions(connectedAdmissions);
				connectedAdmissions.clear();
				connectedAdmissions.add(admission);
			}
		}
		// loop has ended, save the list containing the last admission(s) from the loop
		saveConnectedAdmissions(connectedAdmissions);
		
		return null;
	}

	private void saveConnectedAdmissions(List<Indlaeggelse> admissions) {
		for (Indlaeggelse admission : admissions) {
			// Rules are complete, update LPR with the import timestamp so they are not imported again
			for (LPRReference lprRef : admission.getLprReferencer()) {
				lprDao.updateImportTime(lprRef.getLprRecordNumber());
			}
		}
		removeDuplicateProceduresDiagnoses(admissions);
		haibaDao.saveIndlaeggelsesForloeb(admissions);
	}

	private void removeDuplicateProceduresDiagnoses(List<Indlaeggelse> admissions) {
		for (Indlaeggelse admission : admissions) {
			// remove duplicate diagnoses
			List<Diagnose> d = new ArrayList<Diagnose>();
			for (Diagnose diagosis : admission.getDiagnoses()) {
				if(!d.contains(diagosis)) {
					d.add(diagosis);
				}
			}
			admission.setDiagnoses(d);
			
			// remove duplicate procedures
			List<Procedure> p = new ArrayList<Procedure>();
			for (Procedure procedure : admission.getProcedures()) {
				if(!p.contains(procedure)) {
					p.add(procedure);
				}
			}
			admission.setProcedures(p);
		}
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
