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

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.message.MessageResolver;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;

/*
 * This is the 6. rule to be applied to LPR data
 * It takes a list of contacts from a single CPR number, and processes the data with the Connect contacts rule
 * See the solution document for details about this rule.
 */
public class ConnectContactsRule implements LPRRule {
	
	private static Log log = new Log(Logger.getLogger(ConnectContactsRule.class));
	private List<Administration> contacts;
	
	@Autowired
	ContactToAdmissionRule contactToAdmissionRule;
	
	@Autowired
	MessageResolver resolver;
	
	@Value("${hours.between.contacts.same.hospital}")
	private int sameHospitalDifference;
	
	@Value("${hours.between.contacts.different.hospital}")
	private int differentHospitalDifference;

	@Override
	public LPRRule doProcessing() {

		// Overlapping contacts has been sorted out now, so sort the contacts by in date
		Collections.sort(contacts, new AdministrationInDateComparator());
		
		
		if(contacts.size() > 1) {
			Administration previousContact = null;
			for (Administration contact : contacts) {
				if(previousContact == null) {
					previousContact = contact;
					continue;
				}
				
				DateTime previousOut = new DateTime(previousContact.getUdskrivningsDatetime());
				DateTime in = new DateTime(contact.getIndlaeggelsesDatetime());

				// if same hospital
				if(previousContact.getSygehusCode().equals(contact.getSygehusCode())) {
					// check if gap is <= "sameHospitalDifference" in hours
					if(Minutes.minutesBetween(previousOut, in).isGreaterThan(Minutes.minutes(0)) && 
							Minutes.minutesBetween(previousOut, in).isLessThan(Minutes.minutes(sameHospitalDifference*60 +1))) {
						
						previousContact.setUdskrivningsDatetime(contact.getIndlaeggelsesDatetime());
					}
				} else {
					// else if different hospital

					// check if gap is <= "differentHospitalDifference" in hours
					if(Minutes.minutesBetween(previousOut, in).isGreaterThan(Minutes.minutes(0)) && 
							Minutes.minutesBetween(previousOut, in).isLessThan(Minutes.minutes(differentHospitalDifference*60 +1))) {
						
						previousContact.setUdskrivningsDatetime(contact.getIndlaeggelsesDatetime());
					}
				}
				// set previous to current, for the next iteration
				previousContact = contact;
			}
		} else {
			log.trace("Only one contact in list");
		}
		
		// setup the next rule in the chain
		contactToAdmissionRule.setContacts(contacts);
		
		return contactToAdmissionRule;
	}

	public void setContacts(List<Administration> contacts) {
		this.contacts = contacts;
	}
	
	/*
	 * Package scope for unittesting purpose
	 */
	List<Administration> getContacts() {
		return contacts;
	}
}
