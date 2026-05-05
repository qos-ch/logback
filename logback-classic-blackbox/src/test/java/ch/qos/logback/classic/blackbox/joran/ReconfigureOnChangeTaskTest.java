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
package ch.qos.logback.classic.blackbox.joran;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.blackbox.issue.lbclassic135.LoggingRunnable;
import ch.qos.logback.classic.joran.*;
import ch.qos.logback.classic.model.processor.ConfigurationModelHandlerFull;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.testUtil.RunnableWithCounterAndDone;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.spi.ConfigurationEvent;
import ch.qos.logback.core.spi.ConfigurationEventListener;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.FileTestUtil;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.StatusPrinter2;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static ch.qos.logback.classic.blackbox.BlackboxClassicTestConstants.JORAN_INPUT_PREFIX;
import static ch.qos.logback.classic.joran.ReconfigureOnChangeTask.*;
import static org.junit.jupiter.api.Assertions.*;

public class ReconfigureOnChangeTaskTest extends ReconfigureTaskTestSupport {
    final static int THREAD_COUNT = 5;

    final static int TIMEOUT = 4;
    final static int TIMEOUT_LONG = 10;

    enum ConfigurationDoneType {
        PARTIAL,
        FULL
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    void awaitChangeAndConfiguration(ConfigurationDoneType type, ThrowingRunnable trigger) throws Exception {
        CountDownLatch changeDetected = registerChangeDetectedListener();
        CountDownLatch configurationDone;
        if (type == ConfigurationDoneType.PARTIAL) {
            configurationDone = registerPartialConfigurationEndedSuccessfullyEventListener();
        } else {
            configurationDone = registerNewReconfigurationDoneSuccessfullyListener();
        }

        trigger.run();

        changeDetected.await();
        configurationDone.await();
    }

    // the space in the file name mandated by
    // http://jira.qos.ch/browse/LOGBACK-67
    final static String SCAN1_FILE_AS_STR = JORAN_INPUT_PREFIX + "roct/scan 1.xml";

    final static String SCAN_LOGBACK_474_FILE_AS_STR = JORAN_INPUT_PREFIX + "roct/scan_logback_474.xml";

    final static String INCLUSION_SCAN_TOPLEVEL0_AS_STR = JORAN_INPUT_PREFIX + "roct/inclusion/topLevel0.xml";

    final static String INCLUSION_SCAN_TOP_BY_RESOURCE_AS_STR = JORAN_INPUT_PREFIX + "roct/inclusion/topByResource.xml";

    final static String INCLUSION_SCAN_INNER0_AS_STR = JORAN_INPUT_PREFIX + "roct/inclusion/inner0.xml";

    final static String INCLUSION_SCAN_INNER1_AS_STR = "target/test-classes/asResource/inner1.xml";

    private static final String SCAN_PERIOD_DEFAULT_FILE_AS_STR = JORAN_INPUT_PREFIX + "roct/scan_period_default.xml";

    private static final String TOP_FILE_WITH_INCLUSION = "misc/topWithFileInclusion.xml";

    Logger logger = loggerContext.getLogger(this.getClass());
    StatusChecker statusChecker = new StatusChecker(loggerContext);
    StatusPrinter2 statusPrinter2 = new StatusPrinter2();


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

    void configureAsResource(String filename) throws JoranException {
        URL url = Loader.getResource(filename, this.getClass().getClassLoader());
        assertNotNull(url);
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        jc.doConfigure(url);
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

    @Timeout(value = TIMEOUT, unit = TimeUnit.SECONDS)
    @Test
    public void propertiesConfigurationTest() throws Exception {
        String loggerName = "abc";
        String propertiesFileStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "roct-" + diff + ".properties";
        File propertiesFile = new File(propertiesFileStr);
        String configurationStr = "<configuration debug=\"true\" scan=\"true\" scanPeriod=\"10 millisecond\"><propertiesConfigurator file=\"" + propertiesFileStr + "\"/></configuration>";
        writeToFile(propertiesFile, PropertiesConfigurator.LOGBACK_LOGGER_PREFIX + loggerName + "=INFO");
        configure(asBAIS(configurationStr));
        Logger abcLogger = loggerContext.getLogger(loggerName);
        assertEquals(Level.INFO, abcLogger.getLevel());

        awaitChangeAndConfiguration(ConfigurationDoneType.PARTIAL, () -> 
            writeToFile(propertiesFile, PropertiesConfigurator.LOGBACK_LOGGER_PREFIX + loggerName + "=WARN")
        );
        assertEquals(Level.WARN, abcLogger.getLevel());

        awaitChangeAndConfiguration(ConfigurationDoneType.PARTIAL, () -> 
            writeToFile(propertiesFile, PropertiesConfigurator.LOGBACK_LOGGER_PREFIX + loggerName + "=ERROR")
        );
        assertEquals(Level.ERROR, abcLogger.getLevel());

    }

    @Disabled
    @Test
    void propertiesFromHTTPS() throws InterruptedException, UnsupportedEncodingException, JoranException {
        String loggerName = "com.bazinga";
        String propertiesURLStr = "https://www.qos.ch/foo.properties";
        Logger aLogger = loggerContext.getLogger(loggerName);
        String configurationStr = "<configuration debug=\"true\" scan=\"true\" scanPeriod=\"10 millisecond\"><propertiesConfigurator url=\"" + propertiesURLStr + "\"/></configuration>";

        configure(asBAIS(configurationStr));
        assertEquals(Level.WARN, aLogger.getLevel());
        System.out.println("first phase OK");
        CountDownLatch changeDetectedLatch0 = registerChangeDetectedListener();
        CountDownLatch configurationDoneLatch0 = registerPartialConfigurationEndedSuccessfullyEventListener();

        changeDetectedLatch0.await();
        System.out.println("after changeDetectedLatch0.await();");
        configurationDoneLatch0.await();
        assertEquals(Level.ERROR, aLogger.getLevel());
    }

    // See also http://jira.qos.ch/browse/LOGBACK-338
    @Test
    @Timeout(value = TIMEOUT, unit = TimeUnit.SECONDS)
    public void reconfigurationIsNotPossibleInTheAbsenceOfATopFile() throws IOException, JoranException, InterruptedException {

        ReconfigurationTaskRegisteredConfigEventListener listener = new ReconfigurationTaskRegisteredConfigEventListener();
        loggerContext.addConfigurationEventListener(listener);
        String configurationStr = "<configuration scan=\"true\" scanPeriod=\"50 millisecond\"><include resource=\"asResource/inner1.xml\"/></configuration>";
        configure(asBAIS(configurationStr));

        ConfigurationWatchList configurationWatchList = ConfigurationWatchListUtil.getConfigurationWatchList(loggerContext);

        assertNotNull(configurationWatchList);
        assertFalse(ConfigurationWatchListUtil.watchPredicateFulfilled(loggerContext));
        statusChecker.containsMatch(Status.WARN, ConfigurationModelHandlerFull.FAILED_WATCH_PREDICATE_MESSAGE_1);

        assertFalse(listener.changeDetectorRegisteredEventOccurred);
        assertEquals(0, loggerContext.getCopyOfScheduledFutures().size());
    }

    @Test
    @Timeout(value = TIMEOUT, unit = TimeUnit.SECONDS)
    public void fallbackToSafe_FollowedByRecovery() throws Exception {
        addInfo("Start fallbackToSafe_FollowedByRecovery", this);
        String path = CoreTestConstants.OUTPUT_DIR_PREFIX + "reconfigureOnChangeConfig_fallbackToSafe-" + diff + ".xml";
        File topLevelFile = new File(path);
        writeToFile(topLevelFile, "<configuration scan=\"true\" scanPeriod=\"25 millisecond\"><root level=\"ERROR\"/></configuration> ");

        addResetResistantOnConsoleStatusListener();
        configure(topLevelFile);

        awaitChangeAndConfiguration(ConfigurationDoneType.FULL, () -> 
            writeToFile(topLevelFile, "<configuration scan=\"true\" scanPeriod=\"5 millisecond\">\n  <root></configuration>")
        );
        addInfo("Woke from configurationDoneLatch.await()", this);

        statusChecker.assertContainsMatch(Status.ERROR, CoreConstants.XML_PARSING);
        statusChecker.assertContainsMatch(Status.WARN, FALLING_BACK_TO_SAFE_CONFIGURATION);
        statusChecker.assertContainsMatch(Status.INFO, RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION);

        statusPrinter2.print(loggerContext);

        loggerContext.getStatusManager().clear();

        awaitChangeAndConfiguration(ConfigurationDoneType.FULL, () -> 
            writeToFile(topLevelFile, "<configuration scan=\"true\" scanPeriod=\"5 millisecond\"><root level=\"ERROR\"/></configuration> ")
        );

        statusChecker.assertIsErrorFree();
        statusChecker.containsMatch(DETECTED_CHANGE_IN_CONFIGURATION_FILES);
    }

    private void addResetResistantOnConsoleStatusListener() {
        // enable when debugging
        if (1 == 1)
            return;
        OnConsoleStatusListener ocs = new OnConsoleStatusListener();
        ocs.setContext(loggerContext);
        ocs.setResetResistant(true);
        ocs.start();
        loggerContext.getStatusManager().add(ocs);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    public void scanWithIncludedFileCreatedLater() throws Exception {

        try {
            ReconfigurationTaskRegisteredConfigEventListener roctRegisteredListener = new ReconfigurationTaskRegisteredConfigEventListener();
            loggerContext.addConfigurationEventListener(roctRegisteredListener);
            addResetResistantOnConsoleStatusListener();
            String innerFileAsStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "scanWithIncludedFileCreatedLater-" + diff + ".xml";
            System.setProperty("fileCreatedLater", innerFileAsStr);
            configureAsResource(TOP_FILE_WITH_INCLUSION);

            if(isSurefire()) {
                statusChecker.assertContainsMatch("URL \\[.*\\] is not of type file");
            }

            File innerFile = new File(innerFileAsStr);

            List<File> fileList = getConfigurationWatchList(loggerContext);
            assertThatListContainsFile(fileList, innerFile);

            awaitChangeAndConfiguration(ConfigurationDoneType.FULL, () -> 
                writeToFile(innerFile, "<included><root level=\"ERROR\"/></included> ")
            );

            //statusPrinter2.print(loggerContext);
            Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            assertEquals(Level.ERROR, root.getLevel());

            //System.getProperties().forEach((k,v)->System.out.println(k+"="+v));
        } finally {
            System.getProperties().remove("fileCreatedLater");
        }
    }


    @Test
    @Timeout(value = TIMEOUT, unit = TimeUnit.SECONDS)
    public void scanWithIncludedPropertiesFileCreatedLater() throws Exception {
        try {
            ReconfigurationTaskRegisteredConfigEventListener roctRegisteredListener = new ReconfigurationTaskRegisteredConfigEventListener();
            loggerContext.addConfigurationEventListener(roctRegisteredListener);
            addResetResistantOnConsoleStatusListener();
            String propertiesFileAsStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "scanWithIncludedPropertiesFileCreatedLater-" + diff + ".properties";
            System.setProperty("propertiesFileCreatedLater", propertiesFileAsStr);
            String configurationStr = "<configuration scan=\"true\" scanPeriod=\"5 millisecond\"><propertiesConfigurator file=\"${propertiesFileCreatedLater}\"/></configuration>";
            configure(asBAIS(configurationStr));

            File propertiesFile = new File(propertiesFileAsStr);

            List<File> fileList = getConfigurationWatchList(loggerContext);
            assertThatListContainsFile(fileList, propertiesFile);

            awaitChangeAndConfiguration(ConfigurationDoneType.PARTIAL, () -> 
                writeToFile(propertiesFile, "logback.logger.com.test=INFO")
            );

            // Verify the property was loaded
            Logger testLogger = loggerContext.getLogger("com.test");
            assertEquals(Level.INFO, testLogger.getLevel());

            // Now test that a change to the existing file is detected
            loggerContext.getStatusManager().clear();

            awaitChangeAndConfiguration(ConfigurationDoneType.PARTIAL, () -> 
                writeToFile(propertiesFile, "logback.logger.com.test=WARN")
            );

            assertEquals(Level.WARN, testLogger.getLevel());

        } finally {
            System.getProperties().remove("propertiesFileCreatedLater");
        }
    }

    @Test
    @Timeout(value = TIMEOUT_LONG, unit = TimeUnit.SECONDS)
    public void fallbackToSafeWithIncludedFile_FollowedByRecovery() throws Exception {
        String topLevelFileAsStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "reconfigureOnChangeConfig_top-" + diff + ".xml";
        String innerFileAsStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "reconfigureOnChangeConfig_inner-" + diff + ".xml";
        File topLevelFile = new File(topLevelFileAsStr);
        writeToFile(topLevelFile,
                "<configuration xdebug=\"true\" scan=\"true\" scanPeriod=\"5 millisecond\"><include file=\"" + innerFileAsStr + "\"/></configuration> ");

        File innerFile = new File(innerFileAsStr);
        writeToFile(innerFile, "<included><root level=\"ERROR\"/></included> ");
        addResetResistantOnConsoleStatusListener();

        configure(topLevelFile);

        awaitChangeAndConfiguration(ConfigurationDoneType.FULL, () -> 
            writeToFile(innerFile, "<included>\n<root>\n</included>")
        );
        addInfo("Woke from configurationDoneLatch.await()", this);

        statusChecker.assertContainsMatch(Status.ERROR, CoreConstants.XML_PARSING);
        statusChecker.assertContainsMatch(Status.WARN, FALLING_BACK_TO_SAFE_CONFIGURATION);
        statusChecker.assertContainsMatch(Status.INFO, RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION);

        statusPrinter2.print(loggerContext);

        loggerContext.getStatusManager().clear();

        awaitChangeAndConfiguration(ConfigurationDoneType.FULL, () -> 
            writeToFile(innerFile, "<included><root level=\"ERROR\"/></included> ")
        );

        statusChecker.assertIsErrorFree();
        statusChecker.containsMatch(DETECTED_CHANGE_IN_CONFIGURATION_FILES);

    }

    CountDownLatch registerNewReconfigurationDoneSuccessfullyListener() {
        CountDownLatch latch = new CountDownLatch(1);
        ReconfigurationDoneListener reconfigurationDoneListener = new ReconfigurationDoneListener(latch);
        loggerContext.addConfigurationEventListener(reconfigurationDoneListener);
        return latch;
    }

    boolean isSurefire() {
        if(System.getProperty("surefire.test.class.path") != null) {
            return true;
        }
        if(System.getProperty("surefire.real.class.path") != null) {
            return true;
        }
        return false;
    }

    static class RunMethodInvokedListener implements ConfigurationEventListener {
        CountDownLatch countDownLatch;
        ReconfigureOnChangeTask reconfigureOnChangeTask;

        RunMethodInvokedListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void listen(ConfigurationEvent configurationEvent) {
            if (configurationEvent.getEventType() == ConfigurationEvent.EventType.CHANGE_DETECTOR_RUNNING) {
                countDownLatch.countDown();
                Object data = configurationEvent.getData();
                if (data instanceof ReconfigureOnChangeTask) {
                    reconfigureOnChangeTask = (ReconfigureOnChangeTask) data;
                }
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
        rArray[0] = new UpdaterRunnable(this, configFile, updateType);
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

}
