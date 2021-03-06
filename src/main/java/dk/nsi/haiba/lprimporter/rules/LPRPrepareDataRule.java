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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.log.BusinessRuleErrorLog;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.message.MessageResolver;
import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;
import dk.nsi.haiba.lprimporter.model.haiba.Statistics;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.status.ImportStatus.Outcome;

/*
 * This is the 1. and 3. rule to be applied to LPR data (2. rule is not really a rule, just a demand)
 * It takes a list of contacts from a single CPR number, and processes the data with the PrepareData rule
 * See the solution document for details about this rule.
 */
public class LPRPrepareDataRule implements LPRRule {
	
	private static Log log = new Log(Logger.getLogger(LPRPrepareDataRule.class));
	private List<Administration> contacts;
	
	@Autowired
	LPRDateTimeRule lprDateTimeRule;
	
	@Autowired
	MessageResolver resolver;
	
	@Autowired
	BusinessRuleErrorLog businessRuleErrorLog;
	
	@Autowired
	HAIBADAO haibaDao;

	@Autowired
    @Qualifier(value="compositeLPRDAO")
	LPRDAO lprDao;

	@Override
	public LPRRule doProcessing(Statistics statistics) {
		
		List<Administration> preparedContacts = new ArrayList<Administration>();
		
		for (Administration contact : contacts) {
			
			if(contact.getRecordNumber() == null) {
				// log and ignore this contact
				logErrorContact(contact.getLprReference(), resolver.getMessage("rule.preparedata.recordnumber.isempty"), statistics);
				continue;
			}
			if(contact.getCpr() == null || contact.getCpr().length() == 0) {
				// log and ignore this contact
				logErrorContact(contact.getLprReference(), resolver.getMessage("rule.preparedata.cprnumber.isempty"), statistics);
				continue;
			}
			if(contact.getSygehusCode() == null || contact.getSygehusCode().length() == 0) {
				// log and ignore this contact
				logErrorContact(contact.getLprReference(), resolver.getMessage("rule.preparedata.sygehuscode.isempty"), statistics);
				continue;
			}
			if(contact.getAfdelingsCode() == null || contact.getAfdelingsCode().length() == 0) {
				// log and ignore this contact
				logErrorContact(contact.getLprReference(), resolver.getMessage("rule.preparedata.afdelingscode.isempty"), statistics);
				continue;
			}
			if(contact.getIndlaeggelsesDatetime() == null) {
				// log and ignore this contact
				logErrorContact(contact.getLprReference(), resolver.getMessage("rule.preparedata.indate.isempty"), statistics);
				continue;
			}
			
			// Fetch sygehus codes from FGR if code is 3800
			if(contact.getSygehusCode().equals("3800")) {
				// Increment counter for rule #3
				statistics.rule3Counter += 1;
				String sygehusInitials = haibaDao.getSygehusInitials(contact.getSygehusCode(), contact.getAfdelingsCode(), contact.getIndlaeggelsesDatetime());
				log.debug("Sygehusinitials from FGR: ["+sygehusInitials+"]");
				contact.setSygehusCode(contact.getSygehusCode()+sygehusInitials);
			}
			
			preparedContacts.add(contact);
		}
		// to ensure unittest can get the prepared contacts
		contacts = preparedContacts;
		if(contacts.size() == 0) {
			log.debug("all contacts were prone to error, abort the rules flow");
			return null;
		}
		
		// setup the next rule in the chain
		lprDateTimeRule.setContacts(preparedContacts);
		
		return lprDateTimeRule;
	}
	
	
	private void logErrorContact(LPRReference lprReference, String message, Statistics statistics) {
		// Increment counter for rule #1
		statistics.rule1Counter += 1;

		BusinessRuleError be = new BusinessRuleError(lprReference.getDbId(), lprReference.getLprRecordNumber(), message, resolver.getMessage("rule.preparedata.name"));
		businessRuleErrorLog.log(be);
		// Increment count for contacts with errors
		statistics.contactErrorCounter += 1;
		lprDao.updateImportTime(lprReference, Outcome.FAILURE);
	}

	// package scope for unittesting purpose
	List<Administration> getContacts() {
		return contacts;
	}

	public void setContacts(List<Administration> contacts) {
		this.contacts = contacts;
	}
}
