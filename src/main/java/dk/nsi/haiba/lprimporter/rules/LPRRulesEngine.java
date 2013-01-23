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

import org.springframework.beans.factory.annotation.Autowired;

import dk.nsi.haiba.lprimporter.exception.RuleAbortedException;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;

/*
 * Simple RulesEngine - this could be enhanced by using Spring Integration
 * but for this P.O.C. the processflow is very simple, without contentbased routing, no external triggers e.t.c
 * The rules can be used directly with Spring Integration, just needs the channel mapping etc. But for now we keeps it simple.
 */
public class LPRRulesEngine implements RulesEngine {

	@Autowired
	LPRDateTimeRule lprDateTimeRule;
	
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
			// An Error occured, log the exceptions attached dataobject into the business rule log.
			
			// TODO - Log the error in the application error log
			e.printStackTrace();
		}
		
	}
	
}
