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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import dk.nsi.haiba.lprimporter.exception.RuleAbortedException;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.message.MessageResolver;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;

/*
 * This is the 4. and 5. rule to be applied to LPR data
 * It takes a list of contacts from a single CPR number, and processes the data with the Overlapping contacts rule
 * See the solution document for details about this rule.
 */
public class OverlappingContactsRule implements LPRRule {
	
	private static Log log = new Log(Logger.getLogger(OverlappingContactsRule.class));
	private List<Administration> contacts;
	
	@Autowired
	ConnectContactsRule connectContactsRule;
	
	@Autowired
	MessageResolver resolver;

	@Override
	public LPRRule doProcessing() {
		
		// If hospital and department on two contacts are identical, check for overlapping timestamps
		List<Administration> processedContacts = new ArrayList<Administration>();
		
		Map<String, List<Administration>> sortedContacts = new HashMap<String, List<Administration>>(); 
		
		// Sort contacts for the same hospital and department
		for (Administration contact : contacts) {
			String key = contact.getSygehusCode()+contact.getAfdelingsCode();
			
			if(!sortedContacts.containsKey(key)) {
				List<Administration> list = new ArrayList<Administration>();
				list.add(contact);
				sortedContacts.put(key, list); 
			} else {
				List<Administration> list = sortedContacts.get(key);
				list.add(contact);
			}
		}
		
		for (List<Administration> contactList : sortedContacts.values()) {

			if(contactList.size() == 1) {
				// only 1 contact for the same hospital and department - so no overlapping
				processedContacts.addAll(contactList);
				continue;
			}
			
			// sort list after inDate
			Collections.sort(contactList, new InDateComparator());
			
			Administration previousContact = null;
			for (Administration contact : contactList) {
				if(previousContact == null) {
					previousContact = contact;
					continue;
				}
				
				DateTime previousIn = new DateTime(previousContact.getIndlaeggelsesDatetime());
				DateTime previousOut = null;
				if(previousContact.getUdskrivningsDatetime() != null) {
					previousOut = new DateTime(previousContact.getUdskrivningsDatetime());
				} 
				DateTime in = new DateTime(contact.getIndlaeggelsesDatetime());
				
				if(previousOut == null) {
					BusinessRuleError be = new BusinessRuleError(previousContact.getRecordNumber(), resolver.getMessage("rule.overlapping.contact.no.endddatetime"), resolver.getMessage("rule.overlapping.contact.name"));
					throw new RuleAbortedException("Business rule aborted", be);
				}
				
				if((in.isAfter(previousIn)||in.isEqual(previousIn)) && (in.isBefore(previousOut) || in.isEqual(previousOut))) {
					// contact is overlapping
					List<Administration> splittedContacts = splitContacts(previousContact, contact);
					processedContacts.addAll(splittedContacts);
					
					// TODO if a new contact is created, previous is the one with the newest outdate
					previousContact = contact;
				} else {
					processedContacts.add(previousContact);
					previousContact = contact;
				}

			}
		}
		contacts = processedContacts;
		
		// setup the next rule in the chain
		connectContactsRule.setContacts(contacts);
		
		return connectContactsRule;
	}

	private List<Administration> splitContacts(Administration previous, Administration current) {
		List<Administration> splittedContacts = new ArrayList<Administration>();
		
		DateTime previousIn = new DateTime(previous.getIndlaeggelsesDatetime());
		DateTime previousOut = new DateTime(previous.getUdskrivningsDatetime());
		DateTime in = new DateTime(current.getIndlaeggelsesDatetime());
		DateTime out = new DateTime(current.getUdskrivningsDatetime());

		if(in.isEqual(previousIn) && out.isEqual(previousOut)) {
			BusinessRuleError be = new BusinessRuleError(previous.getRecordNumber(), resolver.getMessage("rule.overlapping.contact.in.and.outdate.are.identical", new Object[] {""+current.getRecordNumber()}), resolver.getMessage("rule.overlapping.contact.name"));
			throw new RuleAbortedException("Business rule aborted", be);
		}
		
		if(previousIn.isEqual(in)) {
			// split on outTime, where current is the first
			previous.setIndlaeggelsesDatetime(current.getUdskrivningsDatetime());
		} else if(previousIn.isBefore(in) && previousOut.isAfter(out)) {
			// create new contact from out to previousout
			Administration newContact = new Administration();
			newContact.setCpr(previous.getCpr());
			newContact.setSygehusCode(previous.getSygehusCode());
			newContact.setAfdelingsCode(previous.getAfdelingsCode());
			newContact.setRecordNumber(previous.getRecordNumber());
			newContact.setLprDiagnoses(previous.getLprDiagnoses());
			newContact.setLprProcedures(previous.getLprProcedures());
			// set in to current out and out to previous out
			newContact.setIndlaeggelsesDatetime(current.getUdskrivningsDatetime());
			newContact.setUdskrivningsDatetime(previous.getUdskrivningsDatetime());
			splittedContacts.add(newContact);
			// Then set previous out to current in
			previous.setUdskrivningsDatetime(current.getIndlaeggelsesDatetime());
		} else if(out.equals(previousOut) || out.isAfter(previousOut) ) {
			// set previous out to current in
			previous.setUdskrivningsDatetime(current.getIndlaeggelsesDatetime());
		}
		
		splittedContacts.add(previous);
		splittedContacts.add(current);
		return splittedContacts;
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
