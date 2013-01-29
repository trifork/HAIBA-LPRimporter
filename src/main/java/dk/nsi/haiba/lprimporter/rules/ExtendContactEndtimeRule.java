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

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import dk.nsi.haiba.lprimporter.exception.RuleAbortedException;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.message.MessageResolver;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;

/*
 * This is the 2. rule to be applied to LPR data
 * It takes a list of contacts from a single CPR number, and processes the data with the Extend Contact EndDate rule
 * See the solution document for details about this rule.
 */
public class ExtendContactEndtimeRule implements LPRRule {
	
	private static Log log = new Log(Logger.getLogger(ExtendContactEndtimeRule.class));
	private List<Administration> contacts;
	
	@Autowired
	RemoveIdenticalContactsRule removeIdenticalContactsRule;
	
	@Autowired
	MessageResolver resolver;

	@Override
	public LPRRule doProcessing() {
		
		for (Administration contact : contacts) {
			
			DateTime contactEndDateTime = new DateTime(contact.getUdskrivningsDatetime());
			DateTime latestProcedureDateTime = null; 
			
			for (LPRProcedure procedure : contact.getLprProcedures()) {
				
				// Get latest procedure endtime, business rule #1 checks the datetime exists.
				DateTime dt = new DateTime(procedure.getProcedureDatetime());
				if(latestProcedureDateTime == null || dt.isAfter(latestProcedureDateTime)) {
					latestProcedureDateTime = dt;
				}
			}
			
			//compare latest procedure endtime with contact endtime
			if(latestProcedureDateTime != null) {
				
				// if procedureDateTime is more than 24 hours after Contact enddatetime it is a businessrule error
				if(latestProcedureDateTime.isAfter(contactEndDateTime.plusHours(24))) {
					BusinessRuleError error = new BusinessRuleError(contact.getRecordNumber(), resolver.getMessage("rule.extend.contact.endddatetime.gap.to.long"), resolver.getMessage("rule.extend.contact.endddatetime.name"));
					throw new RuleAbortedException("Rule aborted due to BusinessRuleError", error);
				}
				
				// if procedureDateTime is after contact enddatetime, set it to procedureDateTime
				if(latestProcedureDateTime.isAfter(contactEndDateTime)) {
					log.debug("procedureDateTime is after contact enddatetime for contact ref + " + contact.getRecordNumber());
					contact.setUdskrivningsDatetime(latestProcedureDateTime.toDate());
				}
			}
		}
		
		// setup the next rule in the chain
		removeIdenticalContactsRule.setContacts(contacts);
		
		return removeIdenticalContactsRule;
	}

	public void setContacts(List<Administration> contacts) {
		this.contacts = contacts;
	}
}
