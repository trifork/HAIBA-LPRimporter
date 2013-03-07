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
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.exception.RuleAbortedException;
import dk.nsi.haiba.lprimporter.log.BusinessRuleErrorLog;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.message.MessageResolver;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;
import dk.nsi.haiba.lprimporter.status.ImportStatus.Outcome;

/*
 * This is the 2 and 3. rule to be applied to LPR data
 * It takes a list of contacts from a single CPR number, and processes the data with the Date/Time rule
 * See the solution document for details about this rule.
 */
public class LPRDateTimeRule implements LPRRule {
	
	private static Log log = new Log(Logger.getLogger(LPRDateTimeRule.class));
	private List<Administration> contacts;
	
	@Autowired
	RemoveIdenticalContactsRule removeIdenticalContactsRule;
	
	@Autowired
	MessageResolver resolver;
	
	@Autowired
	BusinessRuleErrorLog businessRuleErrorLog;
	
	@Autowired
	LPRDAO lprDao;

	@Value("${default.contact.in.hour}")
	int defaultContactInHour;
	
    @Value("${default.contact.outhours.added.inhours}")
    int defaultContactOuthoursAddedInhours;

    @Value("${default.contact.outhours}")
    private int defaultAdmissionEndHours;

    @Value("${default.contact.procedure.outhours}")
	private int defaultProcedureHours;
    
    @Value("${currentpatient.default.outdate.calculated.interval.from.indate}")
	private int currentPatientDefaultInterval;

    @Value("${currentpatient.default.outdate.hours.after.indate}")
	private int currentPatientHoursIfLessThanInterval;

    @Value("${currentpatient.default.outdate.days.after.indate}")
	private int currentPatientDaysIfGreaterThanInterval;

	@Override
	public LPRRule doProcessing() {
		
		List<Administration> adjustedContacts = new ArrayList<Administration>();
		
		for (Administration contact : contacts) {
			
			// AdmissionStartHour for the contact is default set to 0 if not applied in the database, adjust it with the default value from the propertiesfile
			DateTime admissionStart = new DateTime(contact.getIndlaeggelsesDatetime());
			if(admissionStart.getHourOfDay() == 0 && defaultContactInHour != 0) {
				admissionStart = admissionStart.plusHours(defaultContactInHour);
				contact.setIndlaeggelsesDatetime(admissionStart.toDate());
			}
			
			// AdmissionEndtime must be adjusted, if it was set to 0
			Date udskrivningsDatetime = contact.getUdskrivningsDatetime();
			if(udskrivningsDatetime != null) {
				DateTime admissionEnd = new DateTime(udskrivningsDatetime.getTime());
				if(admissionEnd.getHourOfDay() == 0) {
					// does a procedure exist on the same date, set the procedure hour as admission end hour
					for (LPRProcedure procedure : contact.getLprProcedures()) {
						DateTime procedureTime = new DateTime(procedure.getProcedureDatetime());
						if(admissionEnd.getYear() == procedureTime.getYear() &&
								admissionEnd.getMonthOfYear() == procedureTime.getMonthOfYear() &&
								admissionEnd.getDayOfMonth() == procedureTime.getDayOfMonth()) {
							admissionEnd.plusHours(procedureTime.getHourOfDay());
							break;
						}
					}
				}

				// Then if admissionEnd still is 0, check the in date time is the same day 
				if(admissionEnd.getHourOfDay() == 0) {
					if(admissionEnd.getYear() == admissionStart.getYear() &&
							admissionEnd.getMonthOfYear() == admissionStart.getMonthOfYear() &&
							admissionEnd.getDayOfMonth() == admissionStart.getDayOfMonth()) {
						// if same date, set end-datetime to in-datetime + defaultvalue
						admissionEnd = admissionEnd.plusHours(admissionStart.getHourOfDay()).plusHours(defaultContactOuthoursAddedInhours);
					}
				}
				
				// Then if admissionEnd still is 0, and the enddate is after indate set it to a configured defaultvalue 
				if(admissionEnd.getHourOfDay() == 0) {
					admissionEnd = admissionEnd.plusHours(defaultAdmissionEndHours);
				}
				
				contact.setUdskrivningsDatetime(admissionEnd.toDate());
				
				for (LPRProcedure procedure : contact.getLprProcedures()) {
					// if procedure time is set to 0 - set it to 12 the same day
					Date procedureDatetime = procedure.getProcedureDatetime(); 
					if(procedureDatetime != null) {
						DateTime procedureStart = new DateTime(procedureDatetime.getTime());
						if(procedureStart.getHourOfDay() == 0) {
							procedureStart = procedureStart.plusHours(defaultProcedureHours);
							procedure.setProcedureDatetime(procedureStart.toDate());
						}
					} else {
						BusinessRuleError error = new BusinessRuleError(contact.getRecordNumber(), resolver.getMessage("rule.datetime.proceduredate.isempty"), resolver.getMessage("rule.datetime.name"));
						throw new RuleAbortedException("Rule aborted due to BusinessRuleError", error);
					}
				}
				
			} else {
				// patient is currently at the hospital
				contact.setCurrentPatient(true);

				log.debug("Admission End datetime is null for LPR ref: "+contact.getRecordNumber()+" patient is probably not discharged from hospital yet");

				// if in-date is not more than 30 days older than now - set out-date to today at 24:00
				DateTime now = new DateTime();
				DateTime in = new DateTime(contact.getIndlaeggelsesDatetime());
				if(in.isAfter(now.minusDays(currentPatientDefaultInterval))) {
					DateTime out = in.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).plusHours(currentPatientHoursIfLessThanInterval);
					contact.setUdskrivningsDatetime(out.toDate());
				} else {
					// else set out-date to in-date + 30 days
					DateTime out = in.plusDays(currentPatientDaysIfGreaterThanInterval);
					contact.setUdskrivningsDatetime(out.toDate());
				}
			}
			
			// Rule #3
			DateTime in = new DateTime(contact.getIndlaeggelsesDatetime());
			DateTime out = new DateTime(contact.getUdskrivningsDatetime());
			if(in.isAfter(out)) {
				// log the error and ignore the contact.
				BusinessRuleError be = new BusinessRuleError(contact.getRecordNumber(), resolver.getMessage("rule.datetime.indate.isafter.outdate"), resolver.getMessage("rule.datetime.name"));
				businessRuleErrorLog.log(be);
				lprDao.updateImportTime(contact.getRecordNumber(), Outcome.FAILURE);
				continue;
			}
			
			adjustedContacts.add(contact);
		}
		
		// set this for unittesting purpose
		contacts = adjustedContacts;
		if(contacts.size() == 0) {
			// all contacts were prone to error, abort the flow
			return null;
		}
		
		// setup the next rule in the chain
		removeIdenticalContactsRule.setContacts(contacts);
		
		return removeIdenticalContactsRule;
	}

	// package scope for unittesting purpose
	List<Administration> getContacts() {
		return contacts;
	}
	
	public void setContacts(List<Administration> contacts) {
		this.contacts = contacts;
	}
}
