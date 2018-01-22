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

    void configure(File file) throws JoranException {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        jc.doConfigure(file);
    }

    void configure(InputStream is) throws JoranException {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        jc.doConfigure(is);
    }

//    void gConfigure(File file) throws JoranException {
//        GafferConfigurator gc = new GafferConfigurator(loggerContext);
//        gc.run(file);
//    }

    @Test(timeout = 4000L)
    public void checkBasicLifecyle() throws JoranException, IOException, InterruptedException {
        File file = new File(SCAN1_FILE_AS_STR);
        configure(file);
        List<File> fileList = getConfigurationWatchList(loggerContext);
        assertThatListContainsFile(fileList, file);
        checkThatTaskHasRan();
        checkThatTaskCanBeStopped();
    }

//    @Test(timeout = 4000L)
//    public void checkBasicLifecyleWithGaffer() throws JoranException, IOException, InterruptedException {
//        File file = new File(G_SCAN1_FILE_AS_STR);
//        gConfigure(file);
//        List<File> fileList = getConfigurationWatchList(loggerContext);
//        assertThatListContainsFile(fileList, file);
//        checkThatTaskHasRan();
//        checkThatTaskCanBeStopped();
//    }

    private void checkThatTaskCanBeStopped() {
        ScheduledFuture<?> future = loggerContext.getScheduledFutures().get(0);
        loggerContext.stop();
        assertTrue(future.isCancelled());
    }

    private void checkThatTaskHasRan() throws InterruptedException {
        waitForReconfigureOnChangeTaskToRun();
    }

    List<File> getConfigurationWatchList(LoggerContext context) {
        ConfigurationWatchList configurationWatchList = ConfigurationWatchListUtil.getConfigurationWatchList(loggerContext);
        return configurationWatchList.getCopyOfFileWatchList();
    }

    @Test(timeout = 4000L)
    public void scanWithFileInclusion() throws JoranException, IOException, InterruptedException {
        File topLevelFile = new File(INCLUSION_SCAN_TOPLEVEL0_AS_STR);
        File innerFile = new File(INCLUSION_SCAN_INNER0_AS_STR);
        configure(topLevelFile);
        List<File> fileList = getConfigurationWatchList(loggerContext);
        assertThatListContainsFile(fileList, topLevelFile);
        assertThatListContainsFile(fileList, innerFile);
        checkThatTaskHasRan();
        checkThatTaskCanBeStopped();
    }

    @Test(timeout = 4000L)
    public void scanWithResourceInclusion() throws JoranException, IOException, InterruptedException {
        File topLevelFile = new File(INCLUSION_SCAN_TOP_BY_RESOURCE_AS_STR);
        File innerFile = new File(INCLUSION_SCAN_INNER1_AS_STR);
        configure(topLevelFile);
        List<File> fileList = getConfigurationWatchList(loggerContext);
        assertThatListContainsFile(fileList, topLevelFile);
        assertThatListContainsFile(fileList, innerFile);
    }

    // See also http://jira.qos.ch/browse/LOGBACK-338
    @Test(timeout = 4000L)
    public void reconfigurationIsNotPossibleInTheAbsenceOfATopFile() throws IOException, JoranException, InterruptedException {
        String configurationStr = "<configuration scan=\"true\" scanPeriod=\"50 millisecond\"><include resource=\"asResource/inner1.xml\"/></configuration>";
        configure(new ByteArrayInputStream(configurationStr.getBytes("UTF-8")));

        ConfigurationWatchList configurationWatchList = ConfigurationWatchListUtil.getConfigurationWatchList(loggerContext);
        assertNull(configurationWatchList);
        // assertNull(configurationWatchList.getMainURL());

        statusChecker.containsMatch(Status.WARN, "Due to missing top level");
        StatusPrinter.print(loggerContext);
        ReconfigureOnChangeTask roct = getRegisteredReconfigureTask();
        assertNull(roct);
        assertEquals(0, loggerContext.getScheduledFutures().size());
    }

    @Test(timeout = 3000L)
    public void fallbackToSafe_FollowedByRecovery() throws IOException, JoranException, InterruptedException {
        String path = CoreTestConstants.OUTPUT_DIR_PREFIX + "reconfigureOnChangeConfig_fallbackToSafe-" + diff + ".xml";
        File topLevelFile = new File(path);
        writeToFile(topLevelFile, "<configuration scan=\"true\" scanPeriod=\"5 millisecond\"><root level=\"ERROR\"/></configuration> ");
        configure(topLevelFile);
        CountDownLatch changeDetectedLatch = waitForReconfigurationToBeDone(null);
        ReconfigureOnChangeTask oldRoct = getRegisteredReconfigureTask();
        assertNotNull(oldRoct);
        writeToFile(topLevelFile, "<configuration scan=\"true\" scanPeriod=\"5 millisecond\">\n" + "  <root></configuration>");
        changeDetectedLatch.await();
        statusChecker.assertContainsMatch(Status.WARN, FALLING_BACK_TO_SAFE_CONFIGURATION);
        statusChecker.assertContainsMatch(Status.INFO, RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION);

        loggerContext.getStatusManager().clear();

        CountDownLatch secondDoneLatch = waitForReconfigurationToBeDone(oldRoct);
        writeToFile(topLevelFile, "<configuration scan=\"true\" scanPeriod=\"5 millisecond\"><root level=\"ERROR\"/></configuration> ");
        secondDoneLatch.await();
        StatusPrinter.print(loggerContext);
        statusChecker.assertIsErrorFree();
        statusChecker.containsMatch(DETECTED_CHANGE_IN_CONFIGURATION_FILES);
    }

    @Test(timeout = 4000L)
    public void fallbackToSafeWithIncludedFile_FollowedByRecovery() throws IOException, JoranException, InterruptedException, ExecutionException {
        String topLevelFileAsStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "reconfigureOnChangeConfig_top-" + diff + ".xml";
        String innerFileAsStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "reconfigureOnChangeConfig_inner-" + diff + ".xml";
        File topLevelFile = new File(topLevelFileAsStr);
        writeToFile(topLevelFile, "<configuration xdebug=\"true\" scan=\"true\" scanPeriod=\"5 millisecond\"><include file=\"" + innerFileAsStr
                        + "\"/></configuration> ");

        File innerFile = new File(innerFileAsStr);
        writeToFile(innerFile, "<included><root level=\"ERROR\"/></included> ");
        configure(topLevelFile);

        CountDownLatch doneLatch = waitForReconfigurationToBeDone(null);
        ReconfigureOnChangeTask oldRoct = getRegisteredReconfigureTask();
        assertNotNull(oldRoct);
        
        writeToFile(innerFile, "<included>\n<root>\n</included>");
        doneLatch.await();

        statusChecker.assertContainsMatch(Status.WARN, FALLING_BACK_TO_SAFE_CONFIGURATION);
        statusChecker.assertContainsMatch(Status.INFO, RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION);

        loggerContext.getStatusManager().clear();

        CountDownLatch secondDoneLatch = waitForReconfigurationToBeDone(oldRoct);
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

        RunMethodInvokedListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void enteredRunMethod() {
            countDownLatch.countDown();
        }
    };

    class ChangeDetectedListener extends ReconfigureOnChangeTaskListener {
        CountDownLatch countDownLatch;

        ChangeDetectedListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void changeDetected() {
            countDownLatch.countDown();
        }
    };

    class ReconfigurationDoneListener extends ReconfigureOnChangeTaskListener {
        CountDownLatch countDownLatch;

        ReconfigurationDoneListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void doneReconfiguring() {
            countDownLatch.countDown();
        }
    };

    private ReconfigureOnChangeTask waitForReconfigureOnChangeTaskToRun() throws InterruptedException {
        ReconfigureOnChangeTask roct = null;
        while (roct == null) {
            roct = getRegisteredReconfigureTask();
            Thread.yield();
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);
        roct.addListener(new RunMethodInvokedListener(countDownLatch));
        countDownLatch.await();
        return roct;
    }

    private CountDownLatch waitForReconfigurationToBeDone(ReconfigureOnChangeTask oldTask) throws InterruptedException {
        ReconfigureOnChangeTask roct = oldTask;
        while (roct == oldTask) {
            roct = getRegisteredReconfigureTask();
            Thread.yield();
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);
        roct.addListener(new ReconfigurationDoneListener(countDownLatch));
        return countDownLatch;
    }

    private RunnableWithCounterAndDone[] buildRunnableArray(File configFile, UpdateType updateType) {
        RunnableWithCounterAndDone[] rArray = new RunnableWithCounterAndDone[THREAD_COUNT];
        rArray[0] = new Updater(configFile, updateType);
        for (int i = 1; i < THREAD_COUNT; i++) {
            rArray[i] = new LoggingRunnable(logger);
        }
        return rArray;
    }

    @Test
    public void checkReconfigureTaskScheduledWhenDefaultScanPeriodUsed() throws JoranException {
        File file = new File(SCAN_PERIOD_DEFAULT_FILE_AS_STR);
        configure(file);

        final List<ScheduledFuture<?>> scheduledFutures = loggerContext.getScheduledFutures();
        StatusPrinter.print(loggerContext);
        assertFalse(scheduledFutures.isEmpty());
        statusChecker.containsMatch("No 'scanPeriod' specified. Defaulting to");
   
    }

    // check for deadlocks
    @Test(timeout = 4000L)
    public void scan_LOGBACK_474() throws JoranException, IOException, InterruptedException {
        loggerContext.setName("scan_LOGBACK_474");
        File file = new File(SCAN_LOGBACK_474_FILE_AS_STR);
        // StatusListenerConfigHelper.addOnConsoleListenerInstance(loggerContext, new OnConsoleStatusListener());
        configure(file);

        // ReconfigureOnChangeTask roct = waitForReconfigureOnChangeTaskToRun();

        int expectedResets = 2;
        Harness harness = new Harness(expectedResets);

        RunnableWithCounterAndDone[] runnableArray = buildRunnableArray(file, UpdateType.TOUCH);
        harness.execute(runnableArray);

        loggerContext.getStatusManager().add(new InfoStatus("end of execution ", this));
        StatusPrinter.print(loggerContext);
        checkResetCount(expectedResets);
    }

    private void assertThatListContainsFile(List<File> fileList, File file) {
        // conversion to absolute file seems to work nicely
        assertTrue(fileList.contains(file.getAbsoluteFile()));
    }

    private void checkResetCount(int expected) {
        StatusChecker checker = new StatusChecker(loggerContext);
        checker.assertIsErrorFree();

        int effectiveResets = checker.matchCount(CoreConstants.RESET_MSG_PREFIX);
        assertEquals(expected, effectiveResets);

        // String failMsg = "effective=" + effectiveResets + ", expected=" + expected;
        //
        // there might be more effective resets than the expected amount
        // since the harness may be sleeping while a reset occurs
        // assertTrue(failMsg, expected <= effectiveResets && (expected + 2) >= effectiveResets);

    }

    void addInfo(String msg, Object o) {
        loggerContext.getStatusManager().add(new InfoStatus(msg, o));
    }

    enum UpdateType {
        TOUCH, MALFORMED, MALFORMED_INNER
    }

    void writeToFile(File file, String contents) throws IOException {
    	FileWriter fw = new FileWriter(file);
        fw.write(contents);
        fw.close();
        // on linux changes to last modified are not propagated if the
        // time stamp is near the previous time stamp hence the random delta
        file.setLastModified(System.currentTimeMillis()+RandomUtil.getPositiveInt());
    }

    class Harness extends AbstractMultiThreadedHarness {
        int changeCountLimit;

        Harness(int changeCount) {
            this.changeCountLimit = changeCount;
        }

        public void waitUntilEndCondition() throws InterruptedException {
            ReconfigureOnChangeTaskTest.this.addInfo("Entering " + this.getClass() + ".waitUntilEndCondition()", this);

            int changeCount = 0;
            ReconfigureOnChangeTask lastRoct = null;
            CountDownLatch countDownLatch = null;

            while (changeCount < changeCountLimit) {
                ReconfigureOnChangeTask roct = (ReconfigureOnChangeTask) loggerContext.getObject(RECONFIGURE_ON_CHANGE_TASK);
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
            ReconfigureOnChangeTaskTest.this.addInfo("*****Exiting " + this.getClass() + ".waitUntilEndCondition()", this);
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

        Updater(File configFile, UpdateType updateType) {
            this.configFile = configFile;
            this.updateType = updateType;
        }

        Updater(File configFile) {
            this(configFile, UpdateType.TOUCH);
        }

        public void run() {
            while (!isDone()) {
                try {
                    Thread.sleep(sleepBetweenUpdates);
                } catch (InterruptedException e) {
                }
                if (isDone()) {
                    ReconfigureOnChangeTaskTest.this.addInfo("Exiting Updater.run()", this);
                    return;
                }
                counter++;
                ReconfigureOnChangeTaskTest.this.addInfo("Touching [" + configFile + "]", this);
                switch (updateType) {
                case TOUCH:
                    touchFile();
                    break;
                case MALFORMED:
                    try {
                        malformedUpdate();
                    } catch (IOException e) {
                        e.printStackTrace();
                        fail("malformedUpdate failed");
                    }
                    break;
                case MALFORMED_INNER:
                    try {
                        malformedInnerUpdate();
                    } catch (IOException e) {
                        e.printStackTrace();
                        fail("malformedInnerUpdate failed");
                    }
                }
            }
            ReconfigureOnChangeTaskTest.this.addInfo("Exiting Updater.run()", this);
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
