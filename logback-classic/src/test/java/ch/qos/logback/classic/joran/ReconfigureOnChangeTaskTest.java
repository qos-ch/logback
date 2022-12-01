/**
 * Logback: the reliable, generic, fast and flexible logging framework. Copyright (C) 1999-2015, QOS.ch. All rights
 * reserved.
 *
 * This program and the accompanying materials are dual-licensed under either the terms of the Eclipse Public License
 * v1.0 as published by the Eclipse Foundation
 *
 * or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1 as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.joran;

import static ch.qos.logback.classic.ClassicTestConstants.JORAN_INPUT_PREFIX;
import static ch.qos.logback.classic.joran.ReconfigureOnChangeTask.DETECTED_CHANGE_IN_CONFIGURATION_FILES;
import static ch.qos.logback.classic.joran.ReconfigureOnChangeTask.FALLING_BACK_TO_SAFE_CONFIGURATION;
import static ch.qos.logback.classic.joran.ReconfigureOnChangeTask.RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ch.qos.logback.core.spi.ConfigurationEvent;
import ch.qos.logback.core.spi.ConfigurationEventListener;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.WarnStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.issue.lbclassic135.LoggingRunnable;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.FileTestUtil;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.jupiter.api.Timeout;

public class ReconfigureOnChangeTaskTest {
    final static int THREAD_COUNT = 5;

    final static int TIMEOUT = 4;
    final static int TIMEOUT_LONG = 10;

    int diff = RandomUtil.getPositiveInt();

    // the space in the file name mandated by
    // http://jira.qos.ch/browse/LOGBACK-67
    final static String SCAN1_FILE_AS_STR = JORAN_INPUT_PREFIX + "roct/scan 1.xml";

    final static String SCAN_LOGBACK_474_FILE_AS_STR = JORAN_INPUT_PREFIX + "roct/scan_logback_474.xml";

    final static String INCLUSION_SCAN_TOPLEVEL0_AS_STR = JORAN_INPUT_PREFIX + "roct/inclusion/topLevel0.xml";

    final static String INCLUSION_SCAN_TOP_BY_RESOURCE_AS_STR = JORAN_INPUT_PREFIX + "roct/inclusion/topByResource.xml";

    final static String INCLUSION_SCAN_INNER0_AS_STR = JORAN_INPUT_PREFIX + "roct/inclusion/inner0.xml";

    final static String INCLUSION_SCAN_INNER1_AS_STR = "target/test-classes/asResource/inner1.xml";

    private static final String SCAN_PERIOD_DEFAULT_FILE_AS_STR = JORAN_INPUT_PREFIX + "roct/scan_period_default.xml";

    LoggerContext loggerContext = new LoggerContext();
    Logger logger = loggerContext.getLogger(this.getClass());
    StatusChecker statusChecker = new StatusChecker(loggerContext);

    @BeforeAll
    static public void classSetup() {
        FileTestUtil.makeTestOutputDir();
    }

    @BeforeEach
    public void before() {
        loggerContext.setName("ROCTTest-context" + diff);
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

    @Test
    @Timeout(value = TIMEOUT, unit = TimeUnit.SECONDS)
    public void checkBasicLifecyle() throws JoranException, IOException, InterruptedException {
        File file = new File(SCAN1_FILE_AS_STR);
        configure(file);
        List<File> fileList = getConfigurationWatchList(loggerContext);
        assertThatListContainsFile(fileList, file);
        checkThatTaskHasRan();
        checkThatTaskCanBeStopped();
    }

    private void checkThatTaskCanBeStopped() {
        ScheduledFuture<?> future = loggerContext.getCopyOfScheduledFutures().get(0);
        loggerContext.stop();
        assertTrue(future.isCancelled());
    }

    private void checkThatTaskHasRan() throws InterruptedException {
        waitForReconfigureOnChangeTaskToRun();
    }

    List<File> getConfigurationWatchList(LoggerContext lc) {
        ConfigurationWatchList configurationWatchList = ConfigurationWatchListUtil.getConfigurationWatchList(lc);
        return configurationWatchList.getCopyOfFileWatchList();
    }

    @Test
    @Timeout(value = TIMEOUT, unit = TimeUnit.SECONDS)
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

    @Test
    @Timeout(value = TIMEOUT, unit = TimeUnit.SECONDS)
    public void scanWithResourceInclusion() throws JoranException, IOException, InterruptedException {
        File topLevelFile = new File(INCLUSION_SCAN_TOP_BY_RESOURCE_AS_STR);
        File innerFile = new File(INCLUSION_SCAN_INNER1_AS_STR);
        configure(topLevelFile);
        List<File> fileList = getConfigurationWatchList(loggerContext);
        assertThatListContainsFile(fileList, topLevelFile);
        assertThatListContainsFile(fileList, innerFile);
    }

    // See also http://jira.qos.ch/browse/LOGBACK-338
    @Test
    @Timeout(value = TIMEOUT, unit = TimeUnit.SECONDS)
    public void reconfigurationIsNotPossibleInTheAbsenceOfATopFile()
            throws IOException, JoranException, InterruptedException {

        ReconfigurationTaskRegisteredConfigEventListener listener = new ReconfigurationTaskRegisteredConfigEventListener();
        loggerContext.addConfigurationEventListener(listener);
        String configurationStr = "<configuration scan=\"true\" scanPeriod=\"50 millisecond\"><include resource=\"asResource/inner1.xml\"/></configuration>";
        configure(new ByteArrayInputStream(configurationStr.getBytes("UTF-8")));

        ConfigurationWatchList configurationWatchList = ConfigurationWatchListUtil.getConfigurationWatchList(
                loggerContext);
        assertNull(configurationWatchList);
        // assertNull(configurationWatchList.getMainURL());

        statusChecker.containsMatch(Status.WARN, "Due to missing top level");
        //StatusPrinter.print(loggerContext);

        assertFalse(listener.changeDetectorRegisteredEventOccurred);
        assertEquals(0, loggerContext.getCopyOfScheduledFutures().size());
    }

    @Test
    @Timeout(value = TIMEOUT, unit = TimeUnit.SECONDS)
    public void fallbackToSafe_FollowedByRecovery() throws IOException, JoranException, InterruptedException {
        String path = CoreTestConstants.OUTPUT_DIR_PREFIX + "reconfigureOnChangeConfig_fallbackToSafe-" + diff + ".xml";
        File topLevelFile = new File(path);
        writeToFile(topLevelFile,
                "<configuration scan=\"true\" scanPeriod=\"25 millisecond\"><root level=\"ERROR\"/></configuration> ");

        addResetResistantOnConsoleStatusListener();
        configure(topLevelFile);

        long afterFirstConfiguration = System.currentTimeMillis();
        CountDownLatch changeDetectedLatch = registerChangeDetectedListener();
        CountDownLatch configurationDoneLatch = registerNewReconfigurationDoneListener();

        String badXML = "<configuration scan=\"true\" scanPeriod=\"5 millisecond\">\n" + "  <root></configuration>";
        writeToFile(topLevelFile, badXML);
        changeDetectedLatch.await();
        configurationDoneLatch.await();
        addInfo("Woke from configurationDoneLatch.await()", this);

        statusChecker.assertContainsMatch(Status.ERROR, CoreConstants.XML_PARSING);
        statusChecker.assertContainsMatch(Status.WARN, FALLING_BACK_TO_SAFE_CONFIGURATION);
        statusChecker.assertContainsMatch(Status.INFO, RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION);

        loggerContext.getStatusManager().clear();

        addInfo("after loggerContext.getStatusManager().clear() ", this);
        CountDownLatch secondConfigEndedLatch = registerNewReconfigurationDoneListener();

        writeToFile(topLevelFile,
                "<configuration scan=\"true\" scanPeriod=\"5 millisecond\"><root level=\"ERROR\"/></configuration> ");

        secondConfigEndedLatch.await();
        try {
            statusChecker.assertIsErrorFree();
            statusChecker.containsMatch(DETECTED_CHANGE_IN_CONFIGURATION_FILES);
        } finally {
            StatusPrinter.print(loggerContext);
        }
    }

    private void addResetResistantOnConsoleStatusListener() {
        if(1==1)
            return;
        OnConsoleStatusListener ocs = new OnConsoleStatusListener();
        ocs.setContext(loggerContext);
        ocs.setResetResistant(true);
        ocs.start();
        loggerContext.getStatusManager().add(ocs);
    }

    @Test
    @Timeout(value = TIMEOUT_LONG, unit = TimeUnit.SECONDS)
    public void fallbackToSafeWithIncludedFile_FollowedByRecovery()
            throws IOException, JoranException, InterruptedException, ExecutionException {
        String topLevelFileAsStr =
                CoreTestConstants.OUTPUT_DIR_PREFIX + "reconfigureOnChangeConfig_top-" + diff + ".xml";
        String innerFileAsStr =
                CoreTestConstants.OUTPUT_DIR_PREFIX + "reconfigureOnChangeConfig_inner-" + diff + ".xml";
        File topLevelFile = new File(topLevelFileAsStr);
        writeToFile(topLevelFile,
                "<configuration xdebug=\"true\" scan=\"true\" scanPeriod=\"5 millisecond\"><include file=\""
                        + innerFileAsStr + "\"/></configuration> ");

        File innerFile = new File(innerFileAsStr);
        writeToFile(innerFile, "<included><root level=\"ERROR\"/></included> ");
        addResetResistantOnConsoleStatusListener();

        ReconfigurationTaskRegisteredConfigEventListener roctRegisteredListener = new ReconfigurationTaskRegisteredConfigEventListener();
        loggerContext.addConfigurationEventListener(roctRegisteredListener);


        configure(topLevelFile);

        ReconfigureOnChangeTask roct = roctRegisteredListener.reconfigureOnChangeTask;


        System.out.println("===================================================");

        CountDownLatch changeDetectedLatch = registerChangeDetectedListener();
        CountDownLatch configurationDoneLatch = registerNewReconfigurationDoneListener(roct);

        writeToFile(innerFile, "<included>\n<root>\n</included>");
        changeDetectedLatch.await();
        configurationDoneLatch.await();
        addInfo("Woke from configurationDoneLatch.await()", this);

        statusChecker.assertContainsMatch(Status.ERROR, CoreConstants.XML_PARSING);
        statusChecker.assertContainsMatch(Status.WARN, FALLING_BACK_TO_SAFE_CONFIGURATION);
        statusChecker.assertContainsMatch(Status.INFO, RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION);

        loggerContext.getStatusManager().clear();

        CountDownLatch secondDoneLatch = registerNewReconfigurationDoneListener();
        writeToFile(innerFile, "<included><root level=\"ERROR\"/></included> ");
        secondDoneLatch.await();

        statusChecker.assertIsErrorFree();
        statusChecker.containsMatch(DETECTED_CHANGE_IN_CONFIGURATION_FILES);

    }

    CountDownLatch registerNewReconfigurationDoneListener() {
        return registerNewReconfigurationDoneListener(null);
    }

    CountDownLatch registerNewReconfigurationDoneListener(ReconfigureOnChangeTask roct) {
        CountDownLatch latch = new CountDownLatch(1);
        ReconfigurationDoneListener reconfigurationDoneListener = new ReconfigurationDoneListener(latch, roct);
        loggerContext.addConfigurationEventListener(reconfigurationDoneListener);
        return latch;
    }


    CountDownLatch registerChangeDetectedListener() {
        CountDownLatch latch = new CountDownLatch(1);
        ChangeDetectedListener changeDetectedListener = new ChangeDetectedListener(latch);
        loggerContext.addConfigurationEventListener(changeDetectedListener);
        return latch;
    }


    class RunMethodInvokedListener implements ConfigurationEventListener {
        CountDownLatch countDownLatch;
        ReconfigureOnChangeTask reconfigureOnChangeTask;

        RunMethodInvokedListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void listen(ConfigurationEvent configurationEvent) {
            switch (configurationEvent.getEventType()) {
            case CHANGE_DETECTOR_RUNNING:
                countDownLatch.countDown();
                Object data = configurationEvent.getData();
                if (data instanceof ReconfigureOnChangeTask) {
                    reconfigureOnChangeTask = (ReconfigureOnChangeTask) data;
                }
                break;
            default:
            }
        }
    }

    private ReconfigureOnChangeTask waitForReconfigureOnChangeTaskToRun() throws InterruptedException {
        addInfo("entering waitForReconfigureOnChangeTaskToRun", this);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        RunMethodInvokedListener runMethodInvokedListener = new RunMethodInvokedListener(countDownLatch);

        loggerContext.addConfigurationEventListener(runMethodInvokedListener);
        countDownLatch.await();
        return runMethodInvokedListener.reconfigureOnChangeTask;
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

        final List<ScheduledFuture<?>> scheduledFutures = loggerContext.getCopyOfScheduledFutures();
        //StatusPrinter.print(loggerContext);
        assertFalse(scheduledFutures.isEmpty());
        statusChecker.containsMatch("No 'scanPeriod' specified. Defaulting to");

    }

    // check for deadlocks
    @Test
    @Timeout(value = 4, unit = TimeUnit.SECONDS)
    public void scan_LOGBACK_474() throws JoranException, IOException, InterruptedException {
        File file = new File(SCAN_LOGBACK_474_FILE_AS_STR);
        addResetResistantOnConsoleStatusListener();
        configure(file);

        // ReconfigureOnChangeTask roct = waitForReconfigureOnChangeTaskToRun();
        System.out.println(" ------------ creating ReconfigureOnChangeTaskHarness");

        int expectedResets = 2;
        ReconfigureOnChangeTaskHarness harness = new ReconfigureOnChangeTaskHarness(loggerContext, expectedResets);

        RunnableWithCounterAndDone[] runnableArray = buildRunnableArray(file, UpdateType.TOUCH);
        harness.execute(runnableArray);

        addInfo("scan_LOGBACK_474 end of execution ", this);
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
        // assertTrue(failMsg, expected <= effectiveResets && (expected + 2) >=
        // effectiveResets);

    }

    void addInfo(String msg, Object o) {
        loggerContext.getStatusManager().add(new InfoStatus(msg, o));
    }

    void addWarn(String msg, Object o) {
        loggerContext.getStatusManager().add(new WarnStatus(msg, o));
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
        boolean success = file.setLastModified(System.currentTimeMillis() + RandomUtil.getPositiveInt());
        if (!success) {
            addWarn("failed to setLastModified on file " + file, this);
        }
    }

    class Updater extends RunnableWithCounterAndDone {
        File configFile;
        UpdateType updateType;

        // it actually takes time for Windows to propagate file modification changes
        // values below 100 milliseconds can be problematic the same propagation
        // latency occurs in Linux but is even larger (>600 ms)
        // final static int DEFAULT_SLEEP_BETWEEN_UPDATES = 60;

        final int sleepBetweenUpdates = 100;

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
            writeToFile(configFile,
                    "<configuration scan=\"true\" scanPeriod=\"50 millisecond\">\n" + "  <root level=\"ERROR\">\n"
                            + "</configuration>");
        }

        private void malformedInnerUpdate() throws IOException {
            writeToFile(configFile, "<included>\n" + "  <root>\n" + "</included>");
        }

        void touchFile() {

            boolean result = configFile.setLastModified(System.currentTimeMillis());
            if (!result)
                addWarn(this.getClass().getName() + ".touchFile on " + configFile.toString() + " FAILED", this);
        }
    }

}
