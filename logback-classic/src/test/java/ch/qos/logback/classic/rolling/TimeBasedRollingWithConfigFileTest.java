/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.rolling;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.testUtil.ScaffoldingForRollingTests;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;

public class TimeBasedRollingWithConfigFileTest extends ScaffoldingForRollingTests {

	LoggerContext lc = new LoggerContext();
	StatusChecker statusChecker = new StatusChecker(lc);
	Logger logger = lc.getLogger(this.getClass());
	int fileSize = 0;
	int fileIndexCounter = -1;
	int sizeThreshold;

	@Before
	@Override
	public void setUp() {
		lc.setName("test");
		super.setUp();
		lc.putProperty("randomOutputDir", randomOutputDir);
	}

	@After
	public void tearDown() throws Exception {
	}

	void loadConfig(final String confifFile) throws JoranException {
		final JoranConfigurator jc = new JoranConfigurator();
		jc.setContext(lc);
		jc.doConfigure(confifFile);
		currentTime = System.currentTimeMillis();
		recomputeRolloverThreshold(currentTime);
	}

	@Test
	public void basic() throws Exception {
		final String testId = "basic";
		lc.putProperty("testId", testId);
		loadConfig(ClassicTestConstants.JORAN_INPUT_PREFIX + "rolling/" + testId + ".xml");
		statusChecker.assertIsErrorFree();

		final Logger root = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

		expectedFilenameList.add(randomOutputDir + "z" + testId);

		final RollingFileAppender<ILoggingEvent> rfa = (RollingFileAppender<ILoggingEvent>) root.getAppender("ROLLING");

		final TimeBasedRollingPolicy<ILoggingEvent> tprp = (TimeBasedRollingPolicy<ILoggingEvent>) rfa.getTriggeringPolicy();
		final TimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent> tbnatp = tprp.getTimeBasedFileNamingAndTriggeringPolicy();

		final String prefix = "Hello---";
		final int runLength = 4;
		for (int i = 0; i < runLength; i++) {
			logger.debug(prefix + i);
			addExpectedFileNamedIfItsTime_ByDate(randomOutputDir, testId, false);
			incCurrentTime(500);
			tbnatp.setCurrentTime(currentTime);
		}

		existenceCheck(expectedFilenameList);
		sortedContentCheck(randomOutputDir, runLength, prefix);
	}

	@Test
	public void depratedSizeAndTimeBasedFNATPWarning() throws Exception {
		final String testId = "depratedSizeAndTimeBasedFNATPWarning";
		lc.putProperty("testId", testId);
		loadConfig(ClassicTestConstants.JORAN_INPUT_PREFIX + "rolling/" + testId + ".xml");
		StatusPrinter.print(lc);
		statusChecker.assertContainsMatch(Status.WARN, CoreConstants.SIZE_AND_TIME_BASED_FNATP_IS_DEPRECATED);
	}

	@Test
	public void timeAndSize() throws Exception {
		final String testId = "timeAndSize";
		lc.putProperty("testId", testId);
		final String prefix = "Hello-----";

		// the number of times the log file will be written to before time based
		// roll-over occurs
		final int approxWritesPerPeriod = 64;
		sizeThreshold = prefix.length() * approxWritesPerPeriod;
		lc.putProperty("sizeThreshold", "" + sizeThreshold);
		loadConfig(ClassicTestConstants.JORAN_INPUT_PREFIX + "rolling/" + testId + ".xml");

		StatusPrinter.print(lc);
		// Test http://jira.qos.ch/browse/LOGBACK-1236
		statusChecker.assertNoMatch(CoreConstants.SIZE_AND_TIME_BASED_FNATP_IS_DEPRECATED);

		final Logger root = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

		expectedFilenameList.add(randomOutputDir + "z" + testId);

		final RollingFileAppender<ILoggingEvent> rfa = (RollingFileAppender<ILoggingEvent>) root.getAppender("ROLLING");

		statusChecker.assertIsErrorFree();

		final TimeBasedRollingPolicy<ILoggingEvent> tprp = (TimeBasedRollingPolicy<ILoggingEvent>) rfa.getTriggeringPolicy();
		final TimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent> tbnatp = tprp.getTimeBasedFileNamingAndTriggeringPolicy();

		final int timeIncrement = 1000 / approxWritesPerPeriod;
		final int runLength = approxWritesPerPeriod * 3;
		for (int i = 0; i < runLength; i++) {
			final String msg = prefix + i;
			logger.debug(msg);
			addExpectedFileNamedIfItsTime(testId, msg, false);
			incCurrentTime(timeIncrement);
			tbnatp.setCurrentTime(currentTime);
		}

		sortedContentCheck(randomOutputDir, runLength, prefix);
		final int eCount = existenceCount(expectedFilenameList);
		// for various reasons, it is extremely difficult to have the files
		// match exactly the expected archive files. Thus, we aim for
		// an approximate match
		assertTrue("exitenceCount=" + eCount + ", expectedFilenameList.size=" + expectedFilenameList.size(),
				eCount >= 4 && eCount > expectedFilenameList.size() / 2);
	}

	@Test
	public void timeAndSizeWithoutIntegerToken() throws Exception {
		final String testId = "timeAndSizeWithoutIntegerToken";
		loadConfig(ClassicTestConstants.JORAN_INPUT_PREFIX + "rolling/" + testId + ".xml");
		final Logger root = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		expectedFilenameList.add(randomOutputDir + "z" + testId);
		final RollingFileAppender<ILoggingEvent> rfa = (RollingFileAppender<ILoggingEvent>) root.getAppender("ROLLING");
		StatusPrinter.print(lc);

		statusChecker.assertContainsMatch("Missing integer token");
		assertFalse(rfa.isStarted());
	}


	// see also LOGBACK-1176
	@Test
	public void timeAndSizeWithoutMaxFileSize() throws Exception {
		final String testId = "timeAndSizeWithoutMaxFileSize";
		loadConfig(ClassicTestConstants.JORAN_INPUT_PREFIX + "rolling/" + testId + ".xml");
		final Logger root = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		//expectedFilenameList.add(randomOutputDir + "z" + testId);
		final RollingFileAppender<ILoggingEvent> rfa = (RollingFileAppender<ILoggingEvent>) root.getAppender("ROLLING");


		//statusChecker.assertContainsMatch("Missing integer token");
		assertFalse(rfa.isStarted());
		StatusPrinter.print(lc);
	}

	@Test
	public void totalSizeCapSmallerThanMaxFileSize() throws Exception {
		final String testId = "totalSizeCapSmallerThanMaxFileSize";
		lc.putProperty("testId", testId);
		loadConfig(ClassicTestConstants.JORAN_INPUT_PREFIX + "rolling/" + testId + ".xml");
		final Logger root = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		//expectedFilenameList.add(randomOutputDir + "z" + testId);
		final RollingFileAppender<ILoggingEvent> rfa = (RollingFileAppender<ILoggingEvent>) root.getAppender("ROLLING");

		statusChecker.assertContainsMatch("totalSizeCap of \\[\\d* \\w*\\] is smaller than maxFileSize \\[\\d* \\w*\\] which is non-sensical");
		assertFalse(rfa.isStarted());

	}

	void addExpectedFileNamedIfItsTime(final String testId, final String msg, final boolean gzExtension) {
		fileSize += msg.getBytes().length;

		if (passThresholdTime(nextRolloverThreshold)) {
			fileIndexCounter = 0;
			fileSize = 0;
			addExpectedFileName(testId, getDateOfPreviousPeriodsStart(), fileIndexCounter, gzExtension);
			recomputeRolloverThreshold(currentTime);
			return;
		}

		// windows can delay file size changes, so we only allow for
		// fileIndexCounter 0 and 1
		if (fileIndexCounter < 1 && fileSize > sizeThreshold) {
			addExpectedFileName(testId, getDateOfPreviousPeriodsStart(), ++fileIndexCounter, gzExtension);
			fileSize = -1;
		}
	}

	void addExpectedFileName(final String testId, final Date date, final int fileIndexCounter, final boolean gzExtension) {

		String fn = randomOutputDir + testId + "-" + SDF.format(date) + "." + fileIndexCounter;
		System.out.println("Adding " + fn);
		if (gzExtension) {
			fn += ".gz";
		}
		expectedFilenameList.add(fn);
	}

	@Override
	protected void addExpectedFileNamedIfItsTime_ByDate(final String outputDir, final String testId, final boolean gzExtension) {
		if (passThresholdTime(nextRolloverThreshold)) {
			addExpectedFileName_ByDate(outputDir, testId, getDateOfPreviousPeriodsStart(), gzExtension);
			recomputeRolloverThreshold(currentTime);
		}
	}
}
