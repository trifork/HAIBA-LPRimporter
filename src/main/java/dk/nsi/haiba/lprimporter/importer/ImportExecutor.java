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
package dk.nsi.haiba.lprimporter.importer;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.rules.RulesEngine;

/*
 * Scheduled job, responsible for fetching new data from LPR, then send it to the RulesEngine for further processing
 */
public class ImportExecutor {
	
	@Autowired
	LPRDAO lprdao;

	@Autowired
	RulesEngine rulesEngine;
	
	@Scheduled(fixedDelay = 10000)
	public void run() {
		System.out.println("Running Importer: " + new Date().toString());
		doProcess();
	}

	
	/*
	 * Seperated into its own method for testing purpose, because testing a scheduled method isn't good
	 */
	public void doProcess() {
		// Fetch new records from LPR contact table
		// TODO select this in batches
		try {
			List<String> unprocessedCPRnumbers = lprdao.getUnprocessedCPRnumbers();
			if(unprocessedCPRnumbers.size() != 0) {
				for (String cpr : unprocessedCPRnumbers) {
					List<Administration> contactsByCPR = lprdao.getContactsByCPR(cpr);
					// Process the LPR data according to the defined business rules
					rulesEngine.processRuleChain(contactsByCPR);
				}
			}
		} catch(Exception e) {
			// TODO Log this
			e.printStackTrace();
		}
	}
}
