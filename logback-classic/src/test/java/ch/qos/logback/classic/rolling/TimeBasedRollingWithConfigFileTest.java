/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.rolling;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.testUtil.ParentScaffoldingForRollingTests;
import ch.qos.logback.core.rolling.testUtil.ScaffoldingForRollingTests;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeBasedRollingWithConfigFileTest extends ScaffoldingForRollingTests {

    LoggerContext loggerContext = new LoggerContext();
    LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();
    StatusChecker statusChecker = new StatusChecker(loggerContext);
    Logger logger = loggerContext.getLogger(this.getClass());
    int fileSize = 0;
    int fileIndexCounter = -1;
    int sizeThreshold;

    @BeforeEach
    @Override
    public void setUp() {
        loggerContext.setName("test");
        loggerContext.setMDCAdapter(logbackMDCAdapter);
        super.setUp();
        loggerContext.putProperty("randomOutputDir", randomOutputDir);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    void loadConfig(String confifFile) throws JoranException {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        jc.doConfigure(confifFile);
        currentTime = System.currentTimeMillis();
        recomputeRolloverThreshold(currentTime);
    }

    @Test
    public void basic() throws Exception {
        String testId = "basic";
        loggerContext.putProperty("testId", testId);
        loadConfig(ClassicTestConstants.JORAN_INPUT_PREFIX + "rolling/" + testId + ".xml");
        statusChecker.assertIsErrorFree();

        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        expectedFilenameList.add(randomOutputDir + "z" + testId);

        RollingFileAppender<ILoggingEvent> rfa = (RollingFileAppender<ILoggingEvent>) root.getAppender("ROLLING");

        TimeBasedRollingPolicy<ILoggingEvent> tprp = (TimeBasedRollingPolicy<ILoggingEvent>) rfa.getTriggeringPolicy();
        TimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent> tbnatp = tprp.getTimeBasedFileNamingAndTriggeringPolicy();

        String prefix = "Hello---";
        int runLength = 4;
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
        String testId = "depratedSizeAndTimeBasedFNATPWarning";
        loggerContext.putProperty("testId", testId);
        loadConfig(ClassicTestConstants.JORAN_INPUT_PREFIX + "rolling/" + testId + ".xml");
        StatusPrinter.print(loggerContext);
        statusChecker.assertContainsMatch(Status.WARN, CoreConstants.SIZE_AND_TIME_BASED_FNATP_IS_DEPRECATED);
    }

    @Test
    public void timeAndSize() throws Exception {
        String testId = "timeAndSize";
        loggerContext.putProperty("testId", testId);
        String prefix = "Hello-----";

        // the number of times the log file will be written to before time based
        // roll-over occurs
        int approxWritesPerPeriod = 64;
        sizeThreshold = prefix.length() * approxWritesPerPeriod;
        loggerContext.putProperty("sizeThreshold", "" + sizeThreshold);
        System.out.println("timeAndSize.sizeThreshold="+sizeThreshold);
        loadConfig(ClassicTestConstants.JORAN_INPUT_PREFIX + "rolling/" + testId + ".xml");

        StatusPrinter.print(loggerContext);
        // Test http://jira.qos.ch/browse/LOGBACK-1236
        statusChecker.assertNoMatch(CoreConstants.SIZE_AND_TIME_BASED_FNATP_IS_DEPRECATED);

        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        expectedFilenameList.add(randomOutputDir + "z" + testId);

        RollingFileAppender<ILoggingEvent> rfa = (RollingFileAppender<ILoggingEvent>) root.getAppender("ROLLING");

        statusChecker.assertIsErrorFree();

        TimeBasedRollingPolicy<ILoggingEvent> tprp = (TimeBasedRollingPolicy<ILoggingEvent>) rfa.getTriggeringPolicy();
        TimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent> tbnatp = tprp.getTimeBasedFileNamingAndTriggeringPolicy();

        int timeIncrement = 1000 / approxWritesPerPeriod;
        int targetPeriodCount = 3;
        int runLength = approxWritesPerPeriod * targetPeriodCount;
        for (int i = 0; i < runLength; i++) {
            String msg = prefix + i;
            logger.debug(msg);
            addExpectedFileNamedIfItsTime(testId, msg, false);
            incCurrentTime(timeIncrement);
            tbnatp.setCurrentTime(currentTime);
        }

        sortedContentCheck(randomOutputDir, runLength, prefix);
        int eCount = existenceCount(expectedFilenameList);
        // for various reasons, it is extremely difficult to have the files
        // match exactly the expected archive files. Thus, we aim for
        // an approximate match
        assertTrue(eCount >= targetPeriodCount || eCount >= expectedFilenameList.size() / 2,
                "existenceCount=" + eCount + ", expectedFilenameList.size=" + expectedFilenameList.size());
    }

    @Test
    public void timeAndSizeWithoutIntegerToken() throws Exception {
        String testId = "timeAndSizeWithoutIntegerToken";
        loadConfig(ClassicTestConstants.JORAN_INPUT_PREFIX + "rolling/" + testId + ".xml");
        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        expectedFilenameList.add(randomOutputDir + "z" + testId);
        RollingFileAppender<ILoggingEvent> rfa = (RollingFileAppender<ILoggingEvent>) root.getAppender("ROLLING");
        StatusPrinter.print(loggerContext);

        statusChecker.assertContainsMatch("Missing integer token");
        assertFalse(rfa.isStarted());
    }

    // see also LOGBACK-1176
    @Test
    public void timeAndSizeWithoutMaxFileSize() throws Exception {
        String testId = "timeAndSizeWithoutMaxFileSize";
        loadConfig(ClassicTestConstants.JORAN_INPUT_PREFIX + "rolling/" + testId + ".xml");
        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        // expectedFilenameList.add(randomOutputDir + "z" + testId);
        RollingFileAppender<ILoggingEvent> rfa = (RollingFileAppender<ILoggingEvent>) root.getAppender("ROLLING");

        // statusChecker.assertContainsMatch("Missing integer token");
        assertFalse(rfa.isStarted());
        StatusPrinter.print(loggerContext);
    }

    @Test
    public void totalSizeCapSmallerThanMaxFileSize() throws Exception {
        String testId = "totalSizeCapSmallerThanMaxFileSize";
        loggerContext.putProperty("testId", testId);
        loadConfig(ClassicTestConstants.JORAN_INPUT_PREFIX + "rolling/" + testId + ".xml");
        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        // expectedFilenameList.add(randomOutputDir + "z" + testId);
        RollingFileAppender<ILoggingEvent> rfa = (RollingFileAppender<ILoggingEvent>) root.getAppender("ROLLING");

        // totalSizeCap of [10 Bytes] is much smaller than maxFileSize [250 Bytes] which is non-sensical, even taking compression into account.
        statusChecker.assertContainsMatch(Status.WARN,
                "totalSizeCap of \\[\\d* \\w*\\] is much smaller than maxFileSize \\[\\d* \\w*\\] which is non-sensical, even taking compression into account.");
        assertTrue(rfa.isStarted());

    }

    void addExpectedFileNamedIfItsTime(String testId, String msg, boolean gzExtension) {
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
        if ((fileIndexCounter < 1) && fileSize > sizeThreshold) {
            addExpectedFileName(testId, getDateOfPreviousPeriodsStart(), ++fileIndexCounter, gzExtension);
            fileSize = -1;
            return;
        }
    }

    void addExpectedFileName(String testId, Date date, int fileIndexCounter, boolean gzExtension) {

        String fn = randomOutputDir + testId + "-" + SDF.format(date) + "." + fileIndexCounter;
        if (gzExtension) {
            fn += ".gz";
        }
        System.out.println("Adding " + fn);
        expectedFilenameList.add(fn);
    }

    @Override
    protected void addExpectedFileNamedIfItsTime_ByDate(String outputDir, String testId, boolean gzExtension) {
        if (passThresholdTime(nextRolloverThreshold)) {
            addExpectedFileName_ByDate(outputDir, testId, getDateOfPreviousPeriodsStart(), gzExtension);
            recomputeRolloverThreshold(currentTime);
        }
    }
}
