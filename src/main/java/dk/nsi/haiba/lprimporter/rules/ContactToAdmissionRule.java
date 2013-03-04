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

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.model.haiba.Diagnose;
import dk.nsi.haiba.lprimporter.model.haiba.Indlaeggelse;
import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;
import dk.nsi.haiba.lprimporter.model.haiba.Procedure;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRDiagnose;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;

/*
 * This is the 11. rule to be applied to LPR data
 * It takes a list of contacts from a single CPR number, and processes the data with the contacts to admissions rule
 * See the solution document for details about this rule.
 */public class ContactToAdmissionRule implements LPRRule {
	
	private static Log log = new Log(Logger.getLogger(ContactToAdmissionRule.class));
	private List<Administration> contacts;
	
	@Autowired
	ConnectAdmissionsRule connectAdmissionsRule;
	

	@Override
	public LPRRule doProcessing() {

		List<Indlaeggelse> indlaeggelser = new ArrayList<Indlaeggelse>();

		// sort contacts after in date
		Collections.sort(contacts, new AdministrationInDateComparator());
		
		
		if(contacts.size() == 1) {
			indlaeggelser.add(convertContact(contacts.get(0)));
		} else {
			Indlaeggelse indlaeggelse = null;
			for (Administration contact : contacts) {
				if(indlaeggelse == null) {
					indlaeggelse = convertContact(contact);
					continue;
				} else {
					DateTime previousOut = new DateTime(indlaeggelse.getUdskrivningsDatetime());
					DateTime currentIn = new DateTime(contact.getIndlaeggelsesDatetime());
					
					// check if there is a gap between the contact and admission, if not merge them to one admission
					if(hospitalAndDepartmentAreIdentical(indlaeggelse, contact) && previousOut.isEqual(currentIn)) {
						Indlaeggelse tempIndlaeggelse = convertContact(contact);
						// preserve diagnoses and procedures, adjust the outDate and save the LPR refnumber
						indlaeggelse.getDiagnoses().addAll(tempIndlaeggelse.getDiagnoses());
						indlaeggelse.getProcedures().addAll(tempIndlaeggelse.getProcedures());
						indlaeggelse.setUdskrivningsDatetime(tempIndlaeggelse.getUdskrivningsDatetime());
						indlaeggelse.getLprReferencer().addAll(tempIndlaeggelse.getLprReferencer());
					} else {
						// Indlaeggelse and Contact doesn't connect, save the indlaeggelse
						indlaeggelser.add(indlaeggelse);

						// convert the contact to an indlaeggelse
						indlaeggelse = convertContact(contact);
					}
				}
			}
			// no more in the loop, add the last indlaeggelse to the list
			indlaeggelser.add(indlaeggelse);
		}

		connectAdmissionsRule.setAdmissions(indlaeggelser);
		return connectAdmissionsRule;
	}

	private Indlaeggelse convertContact(Administration contact) {
		log.trace("Convert contact with CPR: "+contact.getCpr());
		Indlaeggelse indlaeggelse = new Indlaeggelse();
		indlaeggelse.setAfdelingsCode(contact.getAfdelingsCode());
		indlaeggelse.setCpr(contact.getCpr());
		indlaeggelse.setSygehusCode(contact.getSygehusCode());
		indlaeggelse.setIndlaeggelsesDatetime(contact.getIndlaeggelsesDatetime());
		indlaeggelse.setUdskrivningsDatetime(contact.getUdskrivningsDatetime());
		indlaeggelse.setAktuel(contact.isCurrentPatient());
		
		// save current contact reference
		log.trace("Contact recordnumber: "+contact.getRecordNumber());
		indlaeggelse.addLPRReference(new LPRReference(contact.getRecordNumber()));
		// and merge all former contact references to the admission
		for (LPRReference ref : contact.getLprReferencer()) {
			log.trace("Contact former recordnumbers: " + ref.getLprRecordNumber());
		}
		indlaeggelse.getLprReferencer().addAll(contact.getLprReferencer());
		
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
	
	/*
	 * Utility method for checking if hospital and department is identical for an indlaeggelse and a contact
	 */
	public boolean hospitalAndDepartmentAreIdentical(Indlaeggelse admission, Administration contact) {
        if(admission.getSygehusCode() != null && !admission.getSygehusCode().equals(contact.getSygehusCode())) {
        	return false;
        } else if(admission.getSygehusCode() == null && contact.getSygehusCode() != null) {
        	return false;
        }
        if(admission.getAfdelingsCode() != null && !admission.getAfdelingsCode().equals(contact.getAfdelingsCode())) {
        	return false;
        } else if(admission.getAfdelingsCode() == null && contact.getAfdelingsCode() != null) {
        	return false;
        }
        return true;
	}

	public void setContacts(List<Administration> contacts) {
		this.contacts = contacts;
	}

}
