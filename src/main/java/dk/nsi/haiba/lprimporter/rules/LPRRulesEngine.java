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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.exception.RuleAbortedException;
import dk.nsi.haiba.lprimporter.message.MessageResolver;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;

/*
 * Simple RulesEngine - this could be enhanced by using Spring Integration
 * but for this P.O.C. the processflow is very simple, without contentbased routing, no external triggers e.t.c
 * The rules can be used directly with Spring Integration, just needs the channel mapping etc. But for now we keeps it simple.
 */
public class LPRRulesEngine implements RulesEngine {

	private static Logger businessRuleErrorLog = Logger.getLogger("BusinessRulesErrors");
	
	@Value("${disable.database.errorlog}")
	boolean disableDatabaseErrorLog;

	@Autowired
	LPRDateTimeRule lprDateTimeRule;
	
	@Autowired
	HAIBADAO haibaDao;
	
	@Autowired
	MessageResolver resolver;

	@Override
	public void processRuleChain(List<Administration> contacts) {
		
		// The first rule in the squence is the LprDateTimeRule, 
		// This rule returns the next rule, which carries on until either an error occurs or the end of the flow is reached
		lprDateTimeRule.setContacts(contacts);
		
		try {
			LPRRule next = lprDateTimeRule.doProcessing();
			while(next != null) {
				// Execute the next rule until the end of the flow
				next = next.doProcessing();
			}
		} catch(RuleAbortedException e) {
			// An error occured, log the exceptions attached dataobject into the business rule log (both file and database).
			BusinessRuleError be = e.getBusinessRuleError();
			
			if(!disableDatabaseErrorLog) {
				// Save Businessrule error in database
				haibaDao.saveBusinessRuleError(be);
			}
			
			businessRuleErrorLog.info(resolver.getMessage("errorlog.rule.message", new Object[] {""+be.getLprReference(), be.getAbortedRuleName(), be.getDescription()}));
		}
		
	}
	
}
