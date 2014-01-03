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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.log.BusinessRuleErrorLog;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.message.MessageResolver;
import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;
import dk.nsi.haiba.lprimporter.model.haiba.Statistics;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.status.ImportStatus.Outcome;

/*
 * This is the 9. and 10. rule to be applied to LPR data
 * It takes a list of contacts from a single CPR number, and processes the data with the Overlapping contacts rule
 * See the solution document for details about this rule.
 */
public class ContactsWithSameStartDateRule implements LPRRule {
	
	private static Log log = new Log(Logger.getLogger(ContactsWithSameStartDateRule.class));
	private List<Administration> contacts;
	
	@Autowired
	OverlappingContactsRule overlappingContactsRule;
	
	@Autowired
	MessageResolver resolver;
	
	@Autowired
	BusinessRuleErrorLog businessRuleErrorLog;
	
	@Autowired
    @Qualifier(value="compositeLPRDAO")
	LPRDAO lprDao;

	@Override
	public LPRRule doProcessing(Statistics statistics) {
		
		List<Administration> processedContacts = new ArrayList<Administration>();
		
		// sort list after inDate
		Collections.sort(contacts, new AdministrationInDateComparator());

		Set<Administration> contactsWithErrors = new HashSet<Administration>(); 
		if(contacts.size() == 1) {
			// only 1 contact for the same hospital and department - so no overlapping
			processedContacts.addAll(contacts);
		} else {
			Administration previousContact = null;
			
			for (Administration contact : contacts) {
				if(previousContact == null) {
					previousContact = contact;
					continue;
				}
				
				DateTime previousIn = new DateTime(previousContact.getIndlaeggelsesDatetime());
				DateTime in = new DateTime(contact.getIndlaeggelsesDatetime());
				if(in.isEqual(previousIn)) {
					log.debug("indates are equal for contacts: "+ previousContact.getRecordNumber() + " and: "+contact.getRecordNumber());
					// if out-datetime is equal but sygehus or afdeling is different it is an error
					DateTime previousOut = new DateTime(previousContact.getUdskrivningsDatetime());
					DateTime out = new DateTime(contact.getUdskrivningsDatetime());
					if(previousOut.isEqual(out) &&
							(!previousContact.getSygehusCode().equals(contact.getSygehusCode()) ||
							!previousContact.getAfdelingsCode().equals(contact.getAfdelingsCode()))) {
						// increment counter for rule #9
						statistics.rule9Counter += 1;
						log.debug("in and outdates are equal but department or hospital differs for contacts: "+ previousContact.getRecordNumber() + " and: "+contact.getRecordNumber());
						// error, ignore the contacts, 
						LPRReference ref = previousContact.getLprReference();
                        BusinessRuleError be = new BusinessRuleError(ref.getDbId(), ref.getLprRecordNumber(), resolver.getMessage("rule.contactswithsamestartdate.different.hospitalordepartment", new Object[] {"["+contact.getRecordNumber()+"]"}), resolver.getMessage("rule.contactswithsamestartdate.name"));
						businessRuleErrorLog.log(be);
						
						// mark contacts and their earlier refs. as failed
						contactsWithErrors.add(previousContact);
						contactsWithErrors.add(contact);

						previousContact = contact;
						continue;
					} else if(previousContact.getSygehusCode().equals(contact.getSygehusCode()) && 
							previousContact.getAfdelingsCode().equals(contact.getAfdelingsCode())){
						// increment counter for rule #10
						statistics.rule10Counter += 1;

						Administration preservedContact = null;
						Administration deletedContact = null;
						if(out.isAfter(previousOut)) {
							preservedContact = contact;
							deletedContact = previousContact;
						} else {
							preservedContact = previousContact;
							deletedContact = contact;
						}
						preservedContact.getLprDiagnoses().addAll(deletedContact.getLprDiagnoses());
						preservedContact.getLprProcedures().addAll(deletedContact.getLprProcedures());
						preservedContact.addLPRReference(deletedContact.getLprReference());
						preservedContact.getLprReferencer().addAll(deletedContact.getLprReferencer());
						processedContacts.add(preservedContact);
						previousContact = preservedContact;
						log.debug("preserve contact: "+ preservedContact.getRecordNumber() + " and discard contact: "+deletedContact.getRecordNumber());
					} else {
						log.debug("indates are equal for contacts: "+ previousContact.getRecordNumber() + " and: "+contact.getRecordNumber() + "But department or hospital is different");
						processedContacts.add(previousContact);
						previousContact = contact;
					}
				} else {
					log.debug("indates are NOT equal for contacts: "+ previousContact.getRecordNumber() + " and: "+contact.getRecordNumber());
					processedContacts.add(previousContact);
					previousContact = contact;
				}
			}
			// add the last one - if duplicate it is sorted out later
			processedContacts.add(previousContact);
			
		}
		
		if(contactsWithErrors.size() > 0) {
			// delete all contacts with errors from processedcontacts
			for (Administration administration : contactsWithErrors) {
				// Increment count for contacts with errors
				statistics.contactErrorCounter += 1;
				lprDao.updateImportTime(administration.getLprReference(), Outcome.FAILURE);
				for (LPRReference earlierRef : administration.getLprReferencer()) {
					statistics.contactErrorCounter += 1;
					lprDao.updateImportTime(earlierRef, Outcome.FAILURE);
				}
				processedContacts.remove(administration);
			}
		}
		
		contacts = processedContacts;
		if(contacts.size() == 0) {
			// all contacts were prone to error, abort the flow
			return null;
		}

		// remove duplicate contacts
		Map<String, Administration> items = new HashMap<String,Administration>();
		for (Administration item : contacts) {
			if (items.values().contains(item)) {
				// ignore duplicate items, but ensure all lpr refs are saved
				Administration administration = items.get("Item"+item.hashCode());
				if(administration.getRecordNumber() != item.getRecordNumber()) {
					administration.addLPRReference(item.getLprReference());
					administration.getLprReferencer().addAll(item.getLprReferencer());
				}
			} else {
				items.put("Item"+item.hashCode(),item);
			}
		}
		contacts = new ArrayList<Administration>(items.values());
		
		// setup the next rule in the chain
		overlappingContactsRule.setContacts(contacts);
		
		return overlappingContactsRule;
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
