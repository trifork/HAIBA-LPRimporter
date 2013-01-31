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
import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;
import dk.nsi.haiba.lprimporter.model.haiba.Procedure;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRDiagnose;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;

/*
 * This is the 7. rule to be applied to LPR data
 * It takes a list of contacts from a single CPR number, and processes the data with the contacts to admissions rule
 * See the solution document for details about this rule.
 */public class ContactToAdmissionRule implements LPRRule {
	
	private List<Administration> contacts;
	
	@Autowired
	HAIBADAO haibaDao;
	
	@Autowired
	LPRDAO lprDao;

	@Override
	public LPRRule doProcessing() {

		// sort contacts after In date
		Collections.sort(contacts, new InDateComparator());
		
		
		if(contacts.size() == 1) {
			List<Indlaeggelse> indlaeggelser = new ArrayList<Indlaeggelse>();
			indlaeggelser.add(convertContact(contacts.get(0)));
			
			lprDao.updateImportTime(contacts.get(0).getRecordNumber());
			
			haibaDao.saveIndlaeggelsesForloeb(indlaeggelser);
			
		} else {
			Administration previousContact = null;
			for (Administration contact : contacts) {
				if(previousContact == null) {
					previousContact = contact;
				}
				
				List<Indlaeggelse> indlaeggelser = new ArrayList<Indlaeggelse>();
				
				Indlaeggelse indlaeggelse = null;
				DateTime previousOut = new DateTime(previousContact.getUdskrivningsDatetime());
				DateTime currentIn = new DateTime(contact.getIndlaeggelsesDatetime());
				// check if there is a gab between the contacts, if not merge them to one admission
				if(previousContact.hospitalAndDepartmentAreIdentical(contact) && previousOut.isEqual(currentIn)) {
					indlaeggelse = convertContact(previousContact);
					Indlaeggelse tempIndlaeggelse = convertContact(contact);
					// preserve diagnoses and procedures, and adjust the outDate and save the LPR refnumber
					indlaeggelse.getDiagnoses().addAll(tempIndlaeggelse.getDiagnoses());
					indlaeggelse.getProcedures().addAll(tempIndlaeggelse.getProcedures());
					indlaeggelse.setUdskrivningsDatetime(tempIndlaeggelse.getUdskrivningsDatetime());
					indlaeggelse.getLprReferencer().addAll(tempIndlaeggelse.getLprReferencer());
					// TODO check if more than 2 contacts are in the chain
				} else {
					indlaeggelse = convertContact(previousContact);
				}
				indlaeggelser.add(indlaeggelse);
				
				haibaDao.saveIndlaeggelsesForloeb(indlaeggelser);
			}
			
			for (Administration contact : contacts) {
				// Rules are complete, update LPR with the import timestamp so they are not imported again
				lprDao.updateImportTime(contact.getRecordNumber());
			}
		}

		// TODO the next rule is to apply Admissions to a series of admissions, refactor this rule so it doesn't save state to the database
		// For now, this is the last rule, terminate the rulesengine
		return null;
	}

	private Indlaeggelse convertContact(Administration contact) {
		Indlaeggelse indlaeggelse = new Indlaeggelse();
		indlaeggelse.setAfdelingsCode(contact.getAfdelingsCode());
		indlaeggelse.setCpr(contact.getCpr());
		indlaeggelse.setSygehusCode(contact.getSygehusCode());
		indlaeggelse.setIndlaeggelsesDatetime(contact.getIndlaeggelsesDatetime());
		indlaeggelse.setUdskrivningsDatetime(contact.getUdskrivningsDatetime());
		
		// TODO - an indlaeggelse can have more than one LPR ref
		indlaeggelse.addLPRReference(new LPRReference(contact.getRecordNumber()));
		
		for (LPRDiagnose lprDiagnose : contact.getLprDiagnoses()) {
			Diagnose d = new Diagnose();
			d.setDiagnoseCode(lprDiagnose.getDiagnoseCode());
			d.setDiagnoseType(lprDiagnose.getDiagnoseType());
			d.setTillaegsDiagnose(lprDiagnose.getTillaegsDiagnose());
			indlaeggelse.addDiagnose(d);
		}
		
		for (LPRProcedure lprProcedure : contact.getLprProcedures()) {
			Procedure p = new Procedure();
			p.setAfdelingsCode(lprProcedure.getAfdelingsCode());
			p.setSygehusCode(lprProcedure.getSygehusCode());
			p.setProcedureCode(lprProcedure.getProcedureCode());
			p.setProcedureType(lprProcedure.getProcedureType());
			p.setTillaegsProcedureCode(lprProcedure.getTillaegsProcedureCode());
			p.setProcedureDatetime(lprProcedure.getProcedureDatetime());
			indlaeggelse.addProcedure(p);
		}
		return indlaeggelse;
	}

	public List<Administration> getContacts() {
		return contacts;
	}

	public void setContacts(List<Administration> contacts) {
		this.contacts = contacts;
	}

}
