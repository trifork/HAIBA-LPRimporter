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
import java.util.List;

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

public class ContactToAdmissionRule implements LPRRule {
	
	private List<Administration> contacts;
	
	@Autowired
	HAIBADAO haibaDao;
	
	@Autowired
	LPRDAO lprDao;

	@Override
	public LPRRule doProcessing() {
		
		// TODO - this is a simple stub by now, it converts each contact to a List of "Indlaeggelse", this is to be replaced in a later sprint
		
		for (Administration contact : contacts) {
			List<Indlaeggelse> indlaeggelser = new ArrayList<Indlaeggelse>();

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
			
			indlaeggelser.add(indlaeggelse);


			haibaDao.saveIndlaeggelsesForloeb(indlaeggelser);
			
			//TODO - Update of imported LPR references must be implemented correctly - this is just a stub by now
			lprDao.updateImportTime(contact.getRecordNumber());
		}

		
		
		
		// This is the last rule
		return null;
	}

	public List<Administration> getContacts() {
		return contacts;
	}

	public void setContacts(List<Administration> contacts) {
		this.contacts = contacts;
	}

}
