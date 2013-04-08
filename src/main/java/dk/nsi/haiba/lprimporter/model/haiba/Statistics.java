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
package dk.nsi.haiba.lprimporter.model.haiba;

import java.util.Date;

public class Statistics {
	
	private static Statistics instance = null;
	
	private Statistics() {
		// private
	}
	
	public static Statistics getInstance() {
		if(instance == null) {
			instance = new Statistics();
		}
		return instance;
	}
	
	/*
	 * When the importer is finished and statistics are saved reset the instance to be ready for the next run.
	 */
	public void resetInstance() {
		instance = null;
	}
	
	
	Date date;
	
	// A counter for the number of contacts processed
	public long ContactCounter;

	// A counter for the number of CPR numbers processed
	public long cprCounter;

	// A counter for the number of contacts ending up in errors
	public long ContactErrorCounter;

	// A counter for the number of CPR numbers exported (less or the the same as cprCounter)
	public long cprExportedCounter;

	// A counter for the number of admissions exported
	public long admissionsExportedCounter;

	// A counter for the number of series of admissions exported
	public long admissionsSeriesExportedCounter;

	// A counter for the number of ambulant contacts exported
	public long ambulantContactsExportedCounter;

	/*
	 * Rule numbers are defined in the solution description.
	 */
	public long Rule1Counter;
	public long Rule2Counter;
	public long Rule3Counter;
	public long Rule4Counter;
	public long Rule5Counter;
	public long Rule6Counter;
	public long Rule7Counter;
	public long Rule8Counter;
	public long Rule9Counter;
	public long Rule10Counter;
	public long Rule11Counter;
	public long Rule12Counter;
	public long Rule13Counter;
	public long Rule14Counter;
	public long Rule15Counter;
	public long Rule16Counter;
	

	/*
	 * Saves all current counters to the database
	 */
	public void saveStatistics() {
		// TODO
	}
}
