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
package dk.nsi.haiba.lprimporter.integrationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import dk.nsi.haiba.lprimporter.status.ImportStatus;
import dk.nsi.haiba.lprimporter.status.ImportStatusRepository;
import dk.nsi.haiba.lprimporter.status.TimeSource;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional("haibaTransactionManager")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ImportStatusRepositoryJdbcImplIT {

	@Configuration
	@PropertySource("classpath:test.properties")
	@Import(LPRIntegrationTestConfiguration.class)
	static class ContextConfiguration {
		@Bean
		public TimeSource timeSource() {
			return new ProgrammableTimeSource();
		}
	}

	@Autowired
	private ImportStatusRepository statusRepo;

	@Autowired
	@Qualifier("haibaJdbcTemplate")
	private JdbcTemplate haibaJdbcTemplate;

	@Autowired
	private ProgrammableTimeSource timeSource;

	@Test
	public void returnsNoStatusWhenTableIsEmpty() {
		assertNull(statusRepo.getLatestStatus());
	}

	@Before
	public void resetTime() {
		timeSource.now = new DateTime();
	}

	@Test
	public void returnsOpenStatusWhenOnlyOneOpenStatusInDb() {
		DateTime startTime = new DateTime().withMillisOfSecond(0);
		statusRepo.importStartedAt(startTime);

		ImportStatus latestStatus = statusRepo.getLatestStatus();
		assertNotNull(latestStatus);
		assertEquals(startTime, latestStatus.getStartTime());
	}

	@Test
	public void callingEndedAtWithAnEmptyDatabaseDoesNothing() {
		// this can happen in the ParserExecutor, if some exception occurs
		// before we reach the call to importStartedAt
		statusRepo.importEndedWithFailure(new DateTime(), "ErrorMessage");
		assertNull(statusRepo.getLatestStatus());
	}

	@Test
	public void returnsClosedStatusWhenOnlyOneStatusInDb() {
		ImportStatus expectedStatus = insertStatusInDb(ImportStatus.Outcome.SUCCESS);

		assertEquals(expectedStatus, statusRepo.getLatestStatus());
	}

	@Test
	public void returnsSuccesStatusFromDb() {
		insertStatusInDb(ImportStatus.Outcome.SUCCESS);
		assertEquals(ImportStatus.Outcome.SUCCESS, statusRepo.getLatestStatus()
				.getOutcome());
	}

	@Test
	public void returnsErrorStatusFromDb() {
		insertStatusInDb(ImportStatus.Outcome.FAILURE);
		assertEquals(ImportStatus.Outcome.FAILURE, statusRepo.getLatestStatus()
				.getOutcome());
	}

	@Test
	public void openStatusHasNoOutcome() {
		DateTime startTime = new DateTime();
		statusRepo.importStartedAt(startTime);
		assertNull(statusRepo.getLatestStatus().getOutcome());
	}

	@Test
	public void returnsLatestStatusWhenTwoClosedStatusesExistsInDb()
			throws InterruptedException {
		insertStatusInDb(ImportStatus.Outcome.SUCCESS);
		Thread.sleep(1000); // to avoid the next status having the exact same
							// startTime as the one just inserted
		ImportStatus expectedStatus = insertStatusInDb(ImportStatus.Outcome.FAILURE);

		ImportStatus latestStatus = statusRepo.getLatestStatus();
		assertEquals(expectedStatus, latestStatus);
	}

	@Test
	public void whenTwoOpenStatusesExistsInDbEndingOnlyUpdatesTheLatest() throws InterruptedException {
		DateTime startTimeOldest = new DateTime().withMillisOfSecond(0);
		statusRepo.importStartedAt(startTimeOldest);
		// The reason for this not being closed would be some kind of program
		// error or outage
		Thread.sleep(1000);

		DateTime startTimeNewest = new DateTime().withMillisOfSecond(0);
		statusRepo.importStartedAt(startTimeNewest);

		Thread.sleep(1000);
		statusRepo.importEndedWithFailure(new DateTime().withMillisOfSecond(0), "ErrorMessage");

		// check that the newest was closed
		ImportStatus dbStatus = statusRepo.getLatestStatus();
		assertEquals(startTimeNewest, dbStatus.getStartTime());
		assertNotNull(dbStatus.getEndTime());

		// check that some open status exists (which we can then conclude must
		// be the oldest of the two test statuses)
		assertEquals(1, haibaJdbcTemplate.queryForInt("SELECT COUNT(*) from ImporterStatus WHERE EndTime IS NULL"));
	}

	@Test
	public void jobIsNotOverdueWhenItHasNotRun() {
		assertFalse(statusRepo.isOverdue());
	}

	@Test
	public void jobIsNotOverdueWhenItHasJustRunWithSucces() {
		insertStatusInDb(ImportStatus.Outcome.SUCCESS);
		assertFalse(statusRepo.isOverdue());
	}

	@Test
	public void jobIsNotOverdueWhenItHasJustRunWithError() {
		insertStatusInDb(ImportStatus.Outcome.FAILURE);
		assertFalse(statusRepo.isOverdue());
	}

	@Test
	public void jobIsOverdueWhenItRanMoreDaysAgoThanTheLimit() {
		insertStatusInDb(ImportStatus.Outcome.FAILURE);
		timeSource.now = (new DateTime()).plusDays(2);
		assertTrue(statusRepo.isOverdue());
	}

	@Test
	public void jobIsNotOverdueOneSecondBeforeTheDeadline() {
		insertStatusInDb(ImportStatus.Outcome.FAILURE);
		timeSource.now = (new DateTime()).plusDays(1).minusSeconds(1);
		assertFalse(statusRepo.isOverdue());
	}

	@Test
	public void jobIsOverdueOneSecondAfterTheDeadline() {
		insertStatusInDb(ImportStatus.Outcome.FAILURE);
		timeSource.now = (new DateTime()).plusDays(1).plusSeconds(1);
		assertTrue(statusRepo.isOverdue());
	}

	private ImportStatus insertStatusInDb(ImportStatus.Outcome outcome) {
		DateTime startTime = new DateTime().withMillisOfSecond(0);
		statusRepo.importStartedAt(startTime);
		DateTime endTime = new DateTime().withMillisOfSecond(0);

		if (outcome == ImportStatus.Outcome.SUCCESS) {
			statusRepo.importEndedWithSuccess(endTime);
		} else {
			statusRepo.importEndedWithFailure(endTime, "ErrorMessage");
		}

		ImportStatus expectedStatus = new ImportStatus();
		expectedStatus.setStartTime(startTime);
		expectedStatus.setEndTime(endTime);
		expectedStatus.setOutcome(outcome);
		return expectedStatus;
	}
}