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

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;

/*
 * This is the 1. rule to be applied to LPR data
 * It takes a list of contacts from a single CPR number, and processes the data with the Date/Time rule
 * See the solution document for details about this rule.
 */
public class LPRDateTimeRule implements LPRRule {
	
	private List<Administration> contacts;
	
	@Autowired
	ContactToAdmissionRule contactToAdmissionRule;

	@Override
	public LPRRule doProcessing() {

		for (Administration contact : getContacts()) {
			// AdmissionStart for the contact is default set to 0 if not applied in the database
			
			// AdmissionEnd must be set to the start of the next day, if it was set to 0
			DateTime admissionEnd = new DateTime(contact.getUdskrivningsDatetime().getTime());
			if(admissionEnd.getHourOfDay() == 0) {
				admissionEnd = admissionEnd.plusHours(24);
				contact.setUdskrivningsDatetime(admissionEnd.toDate());
			}
			
			for (LPRProcedure procedure : contact.getLprProcedures()) {
				// if procedure time is set to 0 - set it to 12 the same day 
				DateTime procedureStart = new DateTime(procedure.getProcedureDatetime().getTime());
				if(procedureStart.getHourOfDay() == 0) {
					procedureStart = procedureStart.plusHours(12);
					procedure.setProcedureDatetime(procedureStart.toDate());
				}
			}
		}
		
		contactToAdmissionRule.setContacts(contacts);
		
		return contactToAdmissionRule;
	}

	public List<Administration> getContacts() {
		return contacts;
	}

	public void setContacts(List<Administration> contacts) {
		this.contacts = contacts;
	}
}