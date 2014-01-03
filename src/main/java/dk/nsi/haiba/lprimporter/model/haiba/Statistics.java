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
			instance.date = new Date();
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
	public Date getDate() {
		return date;
	}
	
	// A counter for the number of contacts processed
	public long contactCounter;

	// A counter for the number of CPR numbers processed
	public long cprCounter;

	// A counter for the number of contacts ending up in errors
	public long contactErrorCounter;

	// A counter for the number of CPR numbers exported (less or the the same as cprCounter)
	public long cprExportedCounter;

	// A counter for the number of admissions exported
	public long admissionsExportedCounter;

	// A counter for the number of series of admissions exported
	public long admissionsSeriesExportedCounter;

	// A counter for the number of ambulant contacts exported
	public long ambulantContactsExportedCounter;
	
	// A counter for processed CPR numbers with deleted contacts
	public long cprNumbersWithDeletedContactsCounter;

	// A counter for processed CPR numbers for current patients
	public long currentPatientsCounter;

	/*
	 * Rule numbers are defined in the solution description.
	 */
	public long rule1Counter;
	public long rule2Counter;
	public long rule3Counter;
	public long rule4Counter;
	public long rule5Counter;
	public long rule6Counter;
	public long rule7Counter;
	public long rule8Counter;
	public long rule9Counter;
	public long rule10Counter;
	public long rule11Counter;
	public long rule12Counter;
	public long rule13Counter;
	public long rule14Counter;

    @Override
    public String toString() {
        return "Statistics [date=" + date + ", contactCounter=" + contactCounter + ", cprCounter=" + cprCounter
                + ", contactErrorCounter=" + contactErrorCounter + ", cprExportedCounter=" + cprExportedCounter
                + ", admissionsExportedCounter=" + admissionsExportedCounter + ", admissionsSeriesExportedCounter="
                + admissionsSeriesExportedCounter + ", ambulantContactsExportedCounter="
                + ambulantContactsExportedCounter + ", cprNumbersWithDeletedContactsCounter="
                + cprNumbersWithDeletedContactsCounter + ", currentPatientsCounter=" + currentPatientsCounter
                + ", rule1Counter=" + rule1Counter + ", rule2Counter=" + rule2Counter + ", rule3Counter="
                + rule3Counter + ", rule4Counter=" + rule4Counter + ", rule5Counter=" + rule5Counter
                + ", rule6Counter=" + rule6Counter + ", rule7Counter=" + rule7Counter + ", rule8Counter="
                + rule8Counter + ", rule9Counter=" + rule9Counter + ", rule10Counter=" + rule10Counter
                + ", rule11Counter=" + rule11Counter + ", rule12Counter=" + rule12Counter + ", rule13Counter="
                + rule13Counter + ", rule14Counter=" + rule14Counter + "]";
    }
	
}
