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
package ch.qos.logback.classic.joran;

import static ch.qos.logback.classic.ClassicTestConstants.JORAN_INPUT_PREFIX;
import static ch.qos.logback.classic.joran.ReconfigureOnChangeTask.DETECTED_CHANGE_IN_CONFIGURATION_FILES;
import static ch.qos.logback.classic.joran.ReconfigureOnChangeTask.FALLING_BACK_TO_SAFE_CONFIGURATION;
import static ch.qos.logback.classic.joran.ReconfigureOnChangeTask.RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION;
import static ch.qos.logback.core.CoreConstants.RECONFIGURE_ON_CHANGE_TASK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.issue.lbclassic135.LoggingRunnable;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.contention.AbstractMultiThreadedHarness;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.FileTestUtil;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.testUtil.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;

public class ReconfigureOnChangeTaskTest {
    final static int THREAD_COUNT = 5;

    int diff = RandomUtil.getPositiveInt();

    // the space in the file name mandated by
    // http://jira.qos.ch/browse/LBCORE-119
    final static String SCAN1_FILE_AS_STR = JORAN_INPUT_PREFIX + "roct/scan 1.xml";

    final static String G_SCAN1_FILE_AS_STR = JORAN_INPUT_PREFIX + "roct/scan 1.groovy";

    final static String SCAN_LOGBACK_474_FILE_AS_STR = JORAN_INPUT_PREFIX + "roct/scan_logback_474.xml";

    final static String INCLUSION_SCAN_TOPLEVEL0_AS_STR = JORAN_INPUT_PREFIX + "roct/inclusion/topLevel0.xml";

    final static String INCLUSION_SCAN_TOP_BY_RESOURCE_AS_STR = JORAN_INPUT_PREFIX + "roct/inclusion/topByResource.xml";

    final static String INCLUSION_SCAN_INNER0_AS_STR = JORAN_INPUT_PREFIX + "roct/inclusion/inner0.xml";

    final static String INCLUSION_SCAN_INNER1_AS_STR = "target/test-classes/asResource/inner1.xml";

    private static final String SCAN_PERIOD_DEFAULT_FILE_AS_STR =  JORAN_INPUT_PREFIX + "roct/scan_period_default.xml";

    LoggerContext loggerContext = new LoggerContext();
    Logger logger = loggerContext.getLogger(this.getClass());
    StatusChecker statusChecker = new StatusChecker(loggerContext);

    @BeforeClass
    static public void classSetup() {
        FileTestUtil.makeTestOutputDir();
    }

    void configure(final File file) throws JoranException {
        final JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        jc.doConfigure(file);
    }

    void configure(final InputStream is) throws JoranException {
        final JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        jc.doConfigure(is);
    }

    //    void gConfigure(File file) throws JoranException {
    //        GafferConfigurator gc = new GafferConfigurator(loggerContext);
    //        gc.run(file);
    //    }

    @Test(timeout = 4000L)
    public void checkBasicLifecyle() throws JoranException, IOException, InterruptedException {
        final File file = new File(SCAN1_FILE_AS_STR);
        configure(file);
        final List<File> fileList = getConfigurationWatchList(loggerContext);
        assertThatListContainsFile(fileList, file);
        checkThatTaskHasRan();
        checkThatTaskCanBeStopped();
    }

    private void checkThatTaskCanBeStopped() {
        final ScheduledFuture<?> future = loggerContext.getCopyOfScheduledFutures().get(0);
        loggerContext.stop();
        assertTrue(future.isCancelled());
    }

    private void checkThatTaskHasRan() throws InterruptedException {
        waitForReconfigureOnChangeTaskToRun();
    }

    List<File> getConfigurationWatchList(final LoggerContext context) {
        final ConfigurationWatchList configurationWatchList = ConfigurationWatchListUtil.getConfigurationWatchList(loggerContext);
        return configurationWatchList.getCopyOfFileWatchList();
    }

    @Test(timeout = 4000L)
    public void scanWithFileInclusion() throws JoranException, IOException, InterruptedException {
        final File topLevelFile = new File(INCLUSION_SCAN_TOPLEVEL0_AS_STR);
        final File innerFile = new File(INCLUSION_SCAN_INNER0_AS_STR);
        configure(topLevelFile);
        final List<File> fileList = getConfigurationWatchList(loggerContext);
        assertThatListContainsFile(fileList, topLevelFile);
        assertThatListContainsFile(fileList, innerFile);
        checkThatTaskHasRan();
        checkThatTaskCanBeStopped();
    }

    @Test(timeout = 4000L)
    public void scanWithResourceInclusion() throws JoranException, IOException, InterruptedException {
        final File topLevelFile = new File(INCLUSION_SCAN_TOP_BY_RESOURCE_AS_STR);
        final File innerFile = new File(INCLUSION_SCAN_INNER1_AS_STR);
        configure(topLevelFile);
        final List<File> fileList = getConfigurationWatchList(loggerContext);
        assertThatListContainsFile(fileList, topLevelFile);
        assertThatListContainsFile(fileList, innerFile);
    }

    // See also http://jira.qos.ch/browse/LOGBACK-338
    @Test(timeout = 4000L)
    public void reconfigurationIsNotPossibleInTheAbsenceOfATopFile() throws IOException, JoranException, InterruptedException {
        final String configurationStr = "<configuration scan=\"true\" scanPeriod=\"50 millisecond\"><include resource=\"asResource/inner1.xml\"/></configuration>";
        configure(new ByteArrayInputStream(configurationStr.getBytes("UTF-8")));

        final ConfigurationWatchList configurationWatchList = ConfigurationWatchListUtil.getConfigurationWatchList(loggerContext);
        assertNull(configurationWatchList);
        // assertNull(configurationWatchList.getMainURL());

        statusChecker.containsMatch(Status.WARN, "Due to missing top level");
        StatusPrinter.print(loggerContext);
        final ReconfigureOnChangeTask roct = getRegisteredReconfigureTask();
        assertNull(roct);
        assertEquals(0, loggerContext.getCopyOfScheduledFutures().size());
    }

    @Test(timeout = 3000L)
    public void fallbackToSafe_FollowedByRecovery() throws IOException, JoranException, InterruptedException {
        final String path = CoreTestConstants.OUTPUT_DIR_PREFIX + "reconfigureOnChangeConfig_fallbackToSafe-" + diff + ".xml";
        final File topLevelFile = new File(path);
        writeToFile(topLevelFile, "<configuration scan=\"true\" scanPeriod=\"5 millisecond\"><root level=\"ERROR\"/></configuration> ");
        configure(topLevelFile);
        StatusPrinter.print(loggerContext);
        final CountDownLatch changeDetectedLatch = waitForReconfigurationToBeDone(null);
        final ReconfigureOnChangeTask oldRoct = getRegisteredReconfigureTask();
        assertNotNull(oldRoct);

        final String badXML = "<configuration scan=\"true\" scanPeriod=\"5 millisecond\">\n" + "  <root></configuration>";
        writeToFile(topLevelFile, badXML);
        System.out.println("Waiting for changeDetectedLatch.await()");
        changeDetectedLatch.await();
        System.out.println("Woke from changeDetectedLatch.await()");

        StatusPrinter.print(loggerContext);

        statusChecker.assertContainsMatch(Status.WARN, FALLING_BACK_TO_SAFE_CONFIGURATION);
        statusChecker.assertContainsMatch(Status.INFO, RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION);

        loggerContext.getStatusManager().clear();

        final CountDownLatch secondDoneLatch = waitForReconfigurationToBeDone(oldRoct);
        writeToFile(topLevelFile, "<configuration scan=\"true\" scanPeriod=\"5 millisecond\"><root level=\"ERROR\"/></configuration> ");
        secondDoneLatch.await();
        StatusPrinter.print(loggerContext);
        statusChecker.assertIsErrorFree();
        statusChecker.containsMatch(DETECTED_CHANGE_IN_CONFIGURATION_FILES);
    }

    @Test(timeout = 4000L)
    public void fallbackToSafeWithIncludedFile_FollowedByRecovery() throws IOException, JoranException, InterruptedException, ExecutionException {
        final String topLevelFileAsStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "reconfigureOnChangeConfig_top-" + diff + ".xml";
        final String innerFileAsStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "reconfigureOnChangeConfig_inner-" + diff + ".xml";
        final File topLevelFile = new File(topLevelFileAsStr);
        writeToFile(topLevelFile, "<configuration xdebug=\"true\" scan=\"true\" scanPeriod=\"5 millisecond\"><include file=\"" + innerFileAsStr
                        + "\"/></configuration> ");

        final File innerFile = new File(innerFileAsStr);
        writeToFile(innerFile, "<included><root level=\"ERROR\"/></included> ");
        configure(topLevelFile);

        final CountDownLatch doneLatch = waitForReconfigurationToBeDone(null);
        final ReconfigureOnChangeTask oldRoct = getRegisteredReconfigureTask();
        assertNotNull(oldRoct);

        writeToFile(innerFile, "<included>\n<root>\n</included>");
        doneLatch.await();

        statusChecker.assertContainsMatch(Status.WARN, FALLING_BACK_TO_SAFE_CONFIGURATION);
        statusChecker.assertContainsMatch(Status.INFO, RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION);

        loggerContext.getStatusManager().clear();

        final CountDownLatch secondDoneLatch = waitForReconfigurationToBeDone(oldRoct);
        writeToFile(innerFile, "<included><root level=\"ERROR\"/></included> ");
        secondDoneLatch.await();

        StatusPrinter.print(loggerContext);
        statusChecker.assertIsErrorFree();
        statusChecker.containsMatch(DETECTED_CHANGE_IN_CONFIGURATION_FILES);
    }

    private ReconfigureOnChangeTask getRegisteredReconfigureTask() {
        return (ReconfigureOnChangeTask) loggerContext.getObject(RECONFIGURE_ON_CHANGE_TASK);
    }

    class RunMethodInvokedListener extends ReconfigureOnChangeTaskListener {
        CountDownLatch countDownLatch;

        RunMethodInvokedListener(final CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void enteredRunMethod() {
            countDownLatch.countDown();
        }
    }

    class ChangeDetectedListener extends ReconfigureOnChangeTaskListener {
        CountDownLatch countDownLatch;

        ChangeDetectedListener(final CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void changeDetected() {
            countDownLatch.countDown();
        }
    }

    class ReconfigurationDoneListener extends ReconfigureOnChangeTaskListener {
        CountDownLatch countDownLatch;

        ReconfigurationDoneListener(final CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void doneReconfiguring() {
            System.out.println("ReconfigurationDoneListener now invoking countDownLatch.countDown()");
            countDownLatch.countDown();
        }
    }

    private ReconfigureOnChangeTask waitForReconfigureOnChangeTaskToRun() throws InterruptedException {
        ReconfigureOnChangeTask roct = null;
        while (roct == null) {
            roct = getRegisteredReconfigureTask();
            Thread.yield();
        }

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        roct.addListener(new RunMethodInvokedListener(countDownLatch));
        countDownLatch.await();
        return roct;
    }

    private CountDownLatch waitForReconfigurationToBeDone(final ReconfigureOnChangeTask oldTask) throws InterruptedException {
        ReconfigureOnChangeTask roct = oldTask;
        while (roct == oldTask) {
            roct = getRegisteredReconfigureTask();
            Thread.yield();
        }

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        roct.addListener(new ReconfigurationDoneListener(countDownLatch));
        return countDownLatch;
    }

    private RunnableWithCounterAndDone[] buildRunnableArray(final File configFile, final UpdateType updateType) {
        final RunnableWithCounterAndDone[] rArray = new RunnableWithCounterAndDone[THREAD_COUNT];
        rArray[0] = new Updater(configFile, updateType);
        for (int i = 1; i < THREAD_COUNT; i++) {
            rArray[i] = new LoggingRunnable(logger);
        }
        return rArray;
    }

    @Test
    public void checkReconfigureTaskScheduledWhenDefaultScanPeriodUsed() throws JoranException {
        final File file = new File(SCAN_PERIOD_DEFAULT_FILE_AS_STR);
        configure(file);

        final List<ScheduledFuture<?>> scheduledFutures = loggerContext.getCopyOfScheduledFutures();
        StatusPrinter.print(loggerContext);
        assertFalse(scheduledFutures.isEmpty());
        statusChecker.containsMatch("No 'scanPeriod' specified. Defaulting to");

    }

    // check for deadlocks
    @Test(timeout = 4000L)
    public void scan_LOGBACK_474() throws JoranException, IOException, InterruptedException {
        loggerContext.setName("scan_LOGBACK_474");
        final File file = new File(SCAN_LOGBACK_474_FILE_AS_STR);
        // StatusListenerConfigHelper.addOnConsoleListenerInstance(loggerContext, new OnConsoleStatusListener());
        configure(file);

        // ReconfigureOnChangeTask roct = waitForReconfigureOnChangeTaskToRun();

        final int expectedResets = 2;
        final Harness harness = new Harness(expectedResets);

        final RunnableWithCounterAndDone[] runnableArray = buildRunnableArray(file, UpdateType.TOUCH);
        harness.execute(runnableArray);

        loggerContext.getStatusManager().add(new InfoStatus("end of execution ", this));
        StatusPrinter.print(loggerContext);
        checkResetCount(expectedResets);
    }

    private void assertThatListContainsFile(final List<File> fileList, final File file) {
        // conversion to absolute file seems to work nicely
        assertTrue(fileList.contains(file.getAbsoluteFile()));
    }

    private void checkResetCount(final int expected) {
        final StatusChecker checker = new StatusChecker(loggerContext);
        checker.assertIsErrorFree();

        final int effectiveResets = checker.matchCount(CoreConstants.RESET_MSG_PREFIX);
        assertEquals(expected, effectiveResets);

        // String failMsg = "effective=" + effectiveResets + ", expected=" + expected;
        //
        // there might be more effective resets than the expected amount
        // since the harness may be sleeping while a reset occurs
        // assertTrue(failMsg, expected <= effectiveResets && (expected + 2) >= effectiveResets);

    }

    void addInfo(final String msg, final Object o) {
        loggerContext.getStatusManager().add(new InfoStatus(msg, o));
    }

    enum UpdateType {
        TOUCH, MALFORMED, MALFORMED_INNER
    }

    void writeToFile(final File file, final String contents) throws IOException {
        final FileWriter fw = new FileWriter(file);
        fw.write(contents);
        fw.close();
        // on linux changes to last modified are not propagated if the
        // time stamp is near the previous time stamp hence the random delta
        file.setLastModified(System.currentTimeMillis()+RandomUtil.getPositiveInt());
    }

    class Harness extends AbstractMultiThreadedHarness {
        int changeCountLimit;

        Harness(final int changeCount) {
            changeCountLimit = changeCount;
        }

        @Override
        public void waitUntilEndCondition() throws InterruptedException {
            addInfo("Entering " + this.getClass() + ".waitUntilEndCondition()", this);

            int changeCount = 0;
            ReconfigureOnChangeTask lastRoct = null;
            CountDownLatch countDownLatch = null;

            while (changeCount < changeCountLimit) {
                final ReconfigureOnChangeTask roct = (ReconfigureOnChangeTask) loggerContext.getObject(RECONFIGURE_ON_CHANGE_TASK);
                if (lastRoct != roct && roct != null) {
                    lastRoct = roct;
                    countDownLatch = new CountDownLatch(1);
                    roct.addListener(new ChangeDetectedListener(countDownLatch));
                } else if (countDownLatch != null) {
                    countDownLatch.await();
                    countDownLatch = null;
                    changeCount++;
                }
                Thread.yield();
            }
            addInfo("*****Exiting " + this.getClass() + ".waitUntilEndCondition()", this);
        }

    }

    class Updater extends RunnableWithCounterAndDone {
        File configFile;
        UpdateType updateType;

        // it actually takes time for Windows to propagate file modification changes
        // values below 100 milliseconds can be problematic the same propagation
        // latency occurs in Linux but is even larger (>600 ms)
        // final static int DEFAULT_SLEEP_BETWEEN_UPDATES = 60;

        int sleepBetweenUpdates = 100;

        Updater(final File configFile, final UpdateType updateType) {
            this.configFile = configFile;
            this.updateType = updateType;
        }

        Updater(final File configFile) {
            this(configFile, UpdateType.TOUCH);
        }

        @Override
        public void run() {
            while (!isDone()) {
                try {
                    Thread.sleep(sleepBetweenUpdates);
                } catch (final InterruptedException e) {
                }
                if (isDone()) {
                    addInfo("Exiting Updater.run()", this);
                    return;
                }
                counter++;
                addInfo("Touching [" + configFile + "]", this);
                switch (updateType) {
                case TOUCH:
                    touchFile();
                    break;
                case MALFORMED:
                    try {
                        malformedUpdate();
                    } catch (final IOException e) {
                        e.printStackTrace();
                        fail("malformedUpdate failed");
                    }
                    break;
                case MALFORMED_INNER:
                    try {
                        malformedInnerUpdate();
                    } catch (final IOException e) {
                        e.printStackTrace();
                        fail("malformedInnerUpdate failed");
                    }
                }
            }
            addInfo("Exiting Updater.run()", this);
        }

        private void malformedUpdate() throws IOException {
            writeToFile(configFile, "<configuration scan=\"true\" scanPeriod=\"50 millisecond\">\n" + "  <root level=\"ERROR\">\n" + "</configuration>");
        }

        private void malformedInnerUpdate() throws IOException {
            writeToFile(configFile, "<included>\n" + "  <root>\n" + "</included>");
        }

        void touchFile() {
            configFile.setLastModified(System.currentTimeMillis());
        }
    }

}
