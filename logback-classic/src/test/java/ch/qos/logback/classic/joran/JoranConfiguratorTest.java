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

import static ch.qos.logback.core.joran.sanity.AppenderWithinAppenderSanityChecker.NESTED_APPENDERS_WARNING;
import static ch.qos.logback.core.model.processor.ImplicitModelHandler.IGNORING_UNKNOWN_PROP;
import static ch.qos.logback.core.model.processor.ShutdownHookModelHandler.RENAME_WARNING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.event.KeyValuePair;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jul.JULHelper;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.turbo.DebugUsersTurboFilter;
import ch.qos.logback.classic.turbo.NOPTurboFilter;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.joran.action.ParamAction;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.pattern.parser.Parser;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.spi.ErrorCodes;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.testUtil.StringListAppender;
import ch.qos.logback.core.util.CachingDateFormatter;
import ch.qos.logback.core.util.StatusPrinter;

public class JoranConfiguratorTest {

    LoggerContext loggerContext = new LoggerContext();
    Logger logger = loggerContext.getLogger(this.getClass().getName());
    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    StatusChecker checker = new StatusChecker(loggerContext);
    int diff = RandomUtil.getPositiveInt();

    void configure(String file) throws JoranException {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        loggerContext.putProperty("diff", "" + diff);
        jc.doConfigure(file);

    }

    @Test
    public void simpleList() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "simpleList.xml");
        Logger logger = loggerContext.getLogger(this.getClass().getName());
        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertNotNull(listAppender);
        assertEquals(0, listAppender.list.size());
        String msg = "hello world";
        logger.debug(msg);
        assertEquals(1, listAppender.list.size());
        ILoggingEvent le = (ILoggingEvent) listAppender.list.get(0);
        assertEquals(msg, le.getMessage());
    }


    @Test
    public void asyncWithMultipleAppendersInRoot() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "async/logback_1614.xml");
        Logger logger = loggerContext.getLogger(this.getClass().getName());
        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        AsyncAppender asyncAppender = (AsyncAppender) root.getAppender("ASYNC");
        assertNotNull(asyncAppender);
        ConsoleAppender<ILoggingEvent> console = (ConsoleAppender<ILoggingEvent>) root.getAppender("CONSOLE");
        assertNotNull(console);
        assertTrue(console.isStarted());
        //assertEquals(0, listAppender.list.size());
        String msg = "hello world";
        logger.warn(msg);
    }
    
    @Test
    public void simpleListWithImports() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "simpleListWithImports.xml");
        Logger logger = loggerContext.getLogger(this.getClass().getName());
        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertNotNull(listAppender);
        assertEquals(0, listAppender.list.size());
        String msg = "hello world";
        logger.debug(msg);
        assertEquals(1, listAppender.list.size());
        ILoggingEvent le = (ILoggingEvent) listAppender.list.get(0);
        assertEquals(msg, le.getMessage());
    }

    @Test
    public void level() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "simpleLevel.xml");
        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertEquals(0, listAppender.list.size());
        String msg = "hello world";
        logger.debug(msg);
        assertEquals(0, listAppender.list.size());
    }

    @Test
    public void additivity() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "additivity.xml");
        Logger logger = loggerContext.getLogger("additivityTest");
        assertFalse(logger.isAdditive());
    }

    @Test
    public void rootLoggerLevelSettingBySystemProperty() throws JoranException {
        String propertyName = "logback.level";

        System.setProperty(propertyName, "INFO");
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "rootLevelByProperty.xml");
        // StatusPrinter.print(loggerContext);
        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertEquals(0, listAppender.list.size());
        String msg = "hello world";
        logger.debug(msg);
        assertEquals(0, listAppender.list.size());
        System.clearProperty(propertyName);
    }

    @Test
    public void loggerLevelSettingBySystemProperty() throws JoranException {
        String propertyName = "logback.level";
        System.setProperty(propertyName, "DEBUG");
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "loggerLevelByProperty.xml");
        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertEquals(0, listAppender.list.size());
        String msg = "hello world";
        logger.debug(msg);
        assertEquals(1, listAppender.list.size());
        System.clearProperty(propertyName);
    }

    @Test
    public void appenderRefSettingBySystemProperty() throws JoranException {
        final String propertyName = "logback.appenderRef";
        System.setProperty(propertyName, "A");
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "appenderRefByProperty.xml");
        final Logger logger = loggerContext.getLogger("ch.qos.logback.classic.joran");
        final ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) logger.getAppender("A");
        assertEquals(0, listAppender.list.size());
        final String msg = "hello world";
        logger.info(msg);

        assertEquals(1, listAppender.list.size());
        System.clearProperty(propertyName);
    }

    @Test
    public void statusListener() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "statusListener.xml");
        //StatusPrinter.print(loggerContext);
        checker.assertIsErrorFree();
        checker.assertContainsMatch(Status.WARN,
                "Please use \"level\" attribute within <logger> or <root> elements instead.");
    }

    @Test
    public void statusListenerWithImports() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "statusListenerWithImports.xml");
        //StatusPrinter.print(loggerContext);
        checker.assertIsErrorFree();
        checker.assertContainsMatch(Status.WARN,
                "Please use \"level\" attribute within <logger> or <root> elements instead.");
    }

    @Test
    public void contextRename() throws JoranException {
        loggerContext.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "contextRename.xml");
        assertEquals("wombat", loggerContext.getName());
    }

    @Test
    public void missingConfigurationElement() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "ossfuzz/noConfig.xml");
        
        String msg1 = "Exception in body\\(\\) method for action \\["+ParamAction.class.getName()+"\\]";
        checker.assertContainsMatch(Status.ERROR, msg1);
        
        String msg2 = "current model is null. Is <configuration> element missing?";
        checker.assertContainsException(ActionException.class, msg2 );
    }

    @Test
    public void ignoreUnknownProperty() throws JoranException {
        
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "ossfuzz/unknownProperty.xml");
        String msg = IGNORING_UNKNOWN_PROP+" \\[a\\] in \\[ch.qos.logback.classic.LoggerContext\\]";
        checker.assertContainsMatch(Status.WARN, msg);
    }
    
    // https://bugs.chromium.org/p/oss-fuzz/issues/detail?id=46995
    @Test
    public void complexCollectionWihhNoKnownClass() throws JoranException {
        
       configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "ossfuzz/nestedComplexWithNoKnownClass.xml");
       String msg = "Could not find an appropriate class for property \\[listener\\]";
       checker.assertContainsMatch(Status.ERROR, msg);
    }
    
    @Test
    public void turboFilter() throws JoranException {
        // Although this test uses turbo filters, it only checks
        // that Joran can see the xml element and create
        // and place the relevant object correctly.
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "turbo.xml");

        TurboFilter filter = loggerContext.getTurboFilterList().get(0);
        assertTrue(filter instanceof NOPTurboFilter);
    }

    @Test
    public void testTurboFilterWithStringList() throws JoranException {
        // Although this test uses turbo filters, it only checks
        // that Joran can see <user> elements, and behave correctly
        // that is call the addUser method and pass the correct values
        // to that method.
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "turbo2.xml");

        // StatusPrinter.print(loggerContext.getStatusManager());

        TurboFilter filter = loggerContext.getTurboFilterList().get(0);
        assertTrue(filter instanceof DebugUsersTurboFilter);
        DebugUsersTurboFilter dutf = (DebugUsersTurboFilter) filter;
        assertEquals(2, dutf.getUsers().size());
    }

    @Test
    public void testLevelFilter() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "levelFilter.xml");

        // StatusPrinter.print(loggerContext);

        logger.warn("hello");
        logger.error("to be ignored");

        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) root.getAppender("LIST");

        assertNotNull(listAppender);
        assertEquals(1, listAppender.list.size());
        ILoggingEvent back = listAppender.list.get(0);
        assertEquals(Level.WARN, back.getLevel());
        assertEquals("hello", back.getMessage());
    }



    @Test
    public void testTurboDynamicThreshold() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "turboDynamicThreshold.xml");

        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertEquals(0, listAppender.list.size());

        // this one should be denied
        MDC.put("userId", "user1");
        logger.debug("hello user1");
        // this one should log
        MDC.put("userId", "user2");
        logger.debug("hello user2");

        assertEquals(1, listAppender.list.size());
        ILoggingEvent le = (ILoggingEvent) listAppender.list.get(0);
        assertEquals("hello user2", le.getMessage());
    }

    @Test
    public void testTurboDynamicThreshold2() throws JoranException {

        try {
            configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "turboDynamicThreshold2.xml");
        } finally {
            // StatusPrinter.print(loggerContext);
        }
        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertEquals(0, listAppender.list.size());

        // this one should log
        MDC.put("userId", "user1");
        logger.debug("hello user1");
        // this one should log
        MDC.put("userId", "user2");
        logger.debug("hello user2");
        // this one should fail
        MDC.put("userId", "user3");
        logger.debug("hello user3");

        assertEquals(2, listAppender.list.size());
        ILoggingEvent le = (ILoggingEvent) listAppender.list.get(0);
        assertEquals("hello user1", le.getMessage());
        le = (ILoggingEvent) listAppender.list.get(1);
        assertEquals("hello user2", le.getMessage());
    }

    @Test
    public void timestamp() throws JoranException, IOException, InterruptedException {

        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "timestamp-context.xml";
        configure(configFileAsStr);

        String r = loggerContext.getProperty("testTimestamp");
        assertNotNull(r);
        CachingDateFormatter sdf = new CachingDateFormatter("yyyy-MM");
        String expected = sdf.format(System.currentTimeMillis());
        assertEquals(expected, r, "expected \"" + expected + "\" but got " + r);
    }

    @Test
    public void timestampLocal() throws JoranException, IOException, InterruptedException {

        String sysProp = "ch.qos.logback.classic.joran.JoranConfiguratorTest.timestampLocal";
        System.setProperty(sysProp, "");

        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "timestamp-local.xml";
        configure(configFileAsStr);

        // It's hard to test the local variable has been set, as it's not
        // visible from here. But instead we test that it's not set in the
        // context. And check that a system property has been replaced with the
        // contents of the local variable

        String r = loggerContext.getProperty("testTimestamp");
        assertNull(r);

        String expected = "today is " + new SimpleDateFormat("yyyy-MM").format(new Date());
        String sysPropValue = System.getProperty(sysProp);
        assertEquals(expected, sysPropValue);
    }

    @Test
    public void encoderCharset() throws JoranException, IOException, InterruptedException {

        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "encoderCharset.xml";
        configure(configFileAsStr);

        ConsoleAppender<ILoggingEvent> consoleAppender = (ConsoleAppender<ILoggingEvent>) root.getAppender("CONSOLE");
        assertNotNull(consoleAppender);
        LayoutWrappingEncoder<ILoggingEvent> encoder = (LayoutWrappingEncoder<ILoggingEvent>) consoleAppender
                .getEncoder();

        assertEquals("UTF-8", encoder.getCharset().displayName());

        checker.assertIsErrorFree();
    }

    void verifyJULLevel(String loggerName, Level expectedLevel) {
        java.util.logging.Logger julLogger = JULHelper.asJULLogger(loggerName);
        java.util.logging.Level julLevel = julLogger.getLevel();

        if (expectedLevel == null) {
            assertNull(julLevel);
        } else {
            assertEquals(JULHelper.asJULLevel(expectedLevel), julLevel);
        }

    }

    @Test
    public void levelChangePropagator0() throws JoranException, IOException, InterruptedException {
        String loggerName = "changePropagator0" + diff;
        java.util.logging.Logger.getLogger(loggerName).setLevel(java.util.logging.Level.INFO);
        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "/jul/levelChangePropagator0.xml";
        configure(configFileAsStr);

        checker.assertIsErrorFree();
        verifyJULLevel(loggerName, null);
        verifyJULLevel("a.b.c." + diff, Level.WARN);
        verifyJULLevel(Logger.ROOT_LOGGER_NAME, Level.TRACE);
    }

    @Test
    public void levelChangePropagator1() throws JoranException, IOException, InterruptedException {
        String loggerName = "changePropagator1" + diff;
        java.util.logging.Logger logger1 = java.util.logging.Logger.getLogger(loggerName);
        logger1.setLevel(java.util.logging.Level.INFO);
        verifyJULLevel(loggerName, Level.INFO);
        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "/jul/levelChangePropagator1.xml";
        configure(configFileAsStr);

        checker.assertIsErrorFree();
        verifyJULLevel(loggerName, Level.INFO); //
        verifyJULLevel("a.b.c." + diff, Level.WARN);
        verifyJULLevel(Logger.ROOT_LOGGER_NAME, Level.TRACE);
    }

    @Disabled // because slow
    @Test
    public void onConsoleRetro() throws JoranException, IOException, InterruptedException {
        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "/onConsoleRetro.xml";
        configure(configFileAsStr);
        Thread.sleep(400);

        loggerContext.reset();
        configure(configFileAsStr);
    }

    @Test
    public void unreferencedAppenderShouldNotTriggerUnknownPropertyMessages() throws JoranException {
        String configFileAsStr = ClassicTestConstants.ISSUES_PREFIX + "/logback1572.xml";
        configure(configFileAsStr);
        checker.assertContainsMatch(Status.WARN,
                "Appender named \\[EMAIL\\] not referenced. Skipping further processing.");
        checker.assertNoMatch(IGNORING_UNKNOWN_PROP+" \\[evaluator\\]");
    }

    @Test
    public void LOGBACK_111() throws JoranException {
        String configFileAsStr = ClassicTestConstants.ISSUES_PREFIX + "lbcore193.xml";
        configure(configFileAsStr);
        checker.assertContainsException(ScanException.class);
        checker.assertContainsMatch(Status.ERROR, "Expecting RIGHT_PARENTHESIS token but got null");
        checker.assertContainsMatch(Status.ERROR, "See also " + Parser.MISSING_RIGHT_PARENTHESIS);
    }

    @Test
    public void properties() throws JoranException {
        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "properties.xml";
        assertNull(System.getProperty("sys"));

        configure(configFileAsStr);
        assertNotNull(loggerContext.getProperty(CoreConstants.HOSTNAME_KEY));
        assertNull(loggerContext.getProperty("transientKey1"));
        assertNull(loggerContext.getProperty("transientKey2"));
        assertEquals("node0", loggerContext.getProperty("nodeId"));
        assertEquals("tem", System.getProperty("sys"));
        assertNotNull(loggerContext.getProperty("path"));
        checker.assertIsErrorFree();
    }

    @Test
    public void hostnameProperty() throws JoranException {
        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "hostnameProperty.xml";
        configure(configFileAsStr);
        assertEquals("A", loggerContext.getProperty(CoreConstants.HOSTNAME_KEY));
    }

    // see also http://jira.qos.ch/browse/LOGBACK-134
    @Test
    public void sysProps() throws JoranException {
        System.setProperty("k.lbcore254", ClassicTestConstants.ISSUES_PREFIX + "lbcore254");
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        configurator.doConfigure(ClassicTestConstants.ISSUES_PREFIX + "lbcore254.xml");

        checker.assertIsErrorFree();
    }

    @Test
    public void propsWithMissingRightCurlyBrace() throws JoranException {
        System.setProperty("abc", "not important");
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        configurator.doConfigure(ClassicTestConstants.JORAN_INPUT_PREFIX + "propsMissingRightCurlyBrace.xml");
        checker.assertContainsMatch(Status.ERROR, "Problem while parsing");
    }

    @Test
    public void packageDataDisabledByConfigAttribute() throws JoranException {
        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "packagingDataDisabled.xml";
        configure(configFileAsStr);
        assertFalse(loggerContext.isPackagingDataEnabled());
    }

    @Test
    public void packageDataEnabledByConfigAttribute() throws JoranException {
        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "packagingDataEnabled.xml";
        try {
            configure(configFileAsStr);
        } finally {
            // StatusPrinter.print(loggerContext);
        }
        assertTrue(loggerContext.isPackagingDataEnabled());
    }

    @Test
    public void valueOfConvention() throws JoranException {
        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "valueOfConvention.xml";
        configure(configFileAsStr);
        checker.assertIsWarningOrErrorFree();
    }

    @Test
    public void shutdownHookTest() throws JoranException {
        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "issues/logback_1162.xml";
        loggerContext.putProperty("output_dir", ClassicTestConstants.OUTPUT_DIR_PREFIX + "logback_issue_1162/");
        configure(configFileAsStr);
        Thread thread = (Thread) loggerContext.getObject(CoreConstants.SHUTDOWN_HOOK_THREAD);
        assertNotNull(thread);
    }


    @Test
    public void nestedAppendersDisallowed() throws JoranException {
        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "issues/logback_1674.xml";
        configure(configFileAsStr);
        checker.assertContainsMatch(Status.WARN, NESTED_APPENDERS_WARNING);
        checker.assertContainsMatch(Status.WARN,"Appender at line ");
    }

    @Test
    public void shutdownHookWithDelayParameter() throws JoranException {
        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "issues/logback_1672.xml";
        configure(configFileAsStr);

        Thread thread = (Thread) loggerContext.getObject(CoreConstants.SHUTDOWN_HOOK_THREAD);
        assertNotNull(thread);
        checker.assertNoMatch(IGNORING_UNKNOWN_PROP);
    }

    @Test
    public void migrateShutdownHookClassName() throws JoranException {
        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "issues/logback_1678_shutdown.xml";
        configure(configFileAsStr);

        Thread thread = (Thread) loggerContext.getObject(CoreConstants.SHUTDOWN_HOOK_THREAD);
        assertNotNull(thread);
        checker.assertContainsMatch(RENAME_WARNING);
    }

    @Test
    public void appenderRefBeforeAppenderTest() throws JoranException {
        String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "appenderRefBeforeAppender.xml";
        configure(configFileAsStr);
        Logger logger = loggerContext.getLogger(this.getClass().getName());
        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertNotNull(listAppender);
        assertEquals(0, listAppender.list.size());
        String msg = "hello world";
        logger.debug(msg);
        assertEquals(1, listAppender.list.size());
        ILoggingEvent le = (ILoggingEvent) listAppender.list.get(0);
        assertEquals(msg, le.getMessage());
        checker.assertIsErrorFree();
    }

    @Test
    public void unreferencedAppendersShouldBeSkipped() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "unreferencedAppender1.xml");

        final ListAppender<ILoggingEvent> listAppenderA = (ListAppender<ILoggingEvent>) root.getAppender("A");
        assertNotNull(listAppenderA);
        checker.assertContainsMatch(Status.WARN, "Appender named \\[B\\] not referenced. Skipping further processing.");
    }

    @Test
    public void asynAppenderListFirst() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "asyncAppender_list_first.xml");

        final AsyncAppender asyncAppender = (AsyncAppender) root.getAppender("ASYNC");
        assertNotNull(asyncAppender);
        assertTrue(asyncAppender.isStarted());
    }

    @Test
    public void asynAppenderListAfter() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "asyncAppender_list_after.xml");

        final AsyncAppender asyncAppender = (AsyncAppender) root.getAppender("ASYNC");
        assertNotNull(asyncAppender);
        assertTrue(asyncAppender.isStarted());
    }

    // https://jira.qos.ch/browse/LOGBACK-1570
    @Test
    public void missingPropertyErrorHandling() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "missingProperty.xml");

        final ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertNotNull(listAppender);
        assertTrue(listAppender.isStarted());
        checker.assertContainsMatch(Status.WARN,
                IGNORING_UNKNOWN_PROP+" \\[inexistent\\] in \\[ch.qos.logback.core.read.ListAppender\\]");
    }

    @Test
    public void sequenceNumberGenerator() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "sequenceNumberGenerator.xml");
        final ListAppender<ILoggingEvent> listAppender= (ListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertNotNull(listAppender);

        logger.atDebug().setMessage("hello").log();
        logger.atDebug().setMessage("world").log();

        ILoggingEvent le0 = listAppender.list.get(0);
        ILoggingEvent le1 = listAppender.list.get(1);

        long se0 = le0.getSequenceNumber();
        long se1 = le1.getSequenceNumber();
        assertEquals(1, se1 - se0);
    }

    @Test
    public void sequenceNumberGenerator_missingClass() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "sequenceNumberGenerator-missingClass.xml");
        //StatusPrinter.print(loggerContext);
        final ListAppender<ILoggingEvent> listAppender= (ListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertNotNull(listAppender);
        checker.assertContainsMatch(Status.ERROR, "Missing attribute \\[class\\] in element \\[sequenceNumberGenerator\\]");
    }

        @Test
    public void kvp() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "pattern/kvp.xml");

        String msg = "hello kvp";

        KeyValuePair kvp1 = new KeyValuePair("k" + diff, "v" + diff);
        KeyValuePair kvp2 = new KeyValuePair("k" + (diff + 1), "v" + (diff + 1));
        KeyValuePair kvpNullKey = new KeyValuePair(null, "v" + (diff + 2));
        KeyValuePair kvpNullValue = new KeyValuePair("k" + (diff + 3), null);

        logger.atDebug().addKeyValue(kvp1.key, kvp1.value).log(msg);
        logger.atDebug().addKeyValue(kvp2.key, kvp2.value).log(msg);
        logger.atDebug().addKeyValue(kvpNullKey.key, kvpNullKey.value).log(msg);
        logger.atDebug().addKeyValue(kvpNullValue.key, kvpNullValue.value).log(msg);

        StringListAppender<ILoggingEvent> slAppender = (StringListAppender<ILoggingEvent>) loggerContext
                .getLogger("root").getAppender("LIST");
        assertNotNull(slAppender);
        assertEquals(4, slAppender.strList.size());
        assertTrue(slAppender.strList.get(0).contains(kvp1.key + "=\"" + kvp1.value + "\" " + msg));
        assertTrue(slAppender.strList.get(1).contains(kvp2.key + "=\"" + kvp2.value + "\" " + msg));
        assertTrue(slAppender.strList.get(2).contains("null=\"" + kvpNullKey.value + "\" " + msg));
        assertTrue(slAppender.strList.get(3).contains(kvpNullValue.key + "=\"null\" " + msg));
    }
 
    
    // https://bugs.chromium.org/p/oss-fuzz/issues/detail?id=46697
    @Test
    public void ossFuzz_46697() throws JoranException  {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "ossfuzz/fuzz-46697.xml");
         
        checker.assertContainsMatch(Status.ERROR, ErrorCodes.EMPTY_MODEL_STACK);
    }

    // https://bugs.chromium.org/p/oss-fuzz/issues/detail?id=47093
    // In previous versions of the code, we honored String literal
    // escape sequences for the 'value' attribute named 'value'. After
    // analysis this was deemed superfluous.
    @Test
    public void ossFuzz_47093() throws JoranException  {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "ossfuzz/fuzz-47093.xml");
        assertEquals("a\\t", loggerContext.getProperty("fuzz-47093-a"));
        assertEquals("a\\\\", loggerContext.getProperty("fuzz-47093-b"));
    }

    @Test
    public void ossFuzz_41117() throws JoranException  {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "ossfuzz/fuzz-47117.xml");
        checker.assertContainsMatch(Status.ERROR, ErrorCodes.ROOT_LEVEL_CANNOT_BE_SET_TO_NULL);
        checker.assertErrorCount(2);
        //StatusPrinter.print(loggerContext);
    }

    @Test
    public void ossFuzz_41117_bis() throws JoranException  {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "ossfuzz/fuzz-47117-bis.xml");
        checker.assertContainsMatch(Status.ERROR, ErrorCodes.ROOT_LEVEL_CANNOT_BE_SET_TO_NULL);
    }

    @Test
    public void ossFuzz_41117_bis2() throws JoranException  {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "ossfuzz/fuzz-47117-bis2.xml");
        checker.assertContainsMatch(Status.ERROR, ErrorCodes.ROOT_LEVEL_CANNOT_BE_SET_TO_NULL);
    }

    @Test
    public void ossFuzz_47293() throws JoranException  {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "ossfuzz/fuzz-47293.xml");
        checker.assertContainsMatch(Status.ERROR, ErrorCodes.MISSING_IF_EMPTY_MODEL_STACK);
    }


    @Test
    public void dateConverterWithLocale() throws JoranException  {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "dateWithLocale.xml");
        checker.assertContainsMatch(Status.INFO, "Setting zoneId to \"Australia/Perth\"");
        checker.assertContainsMatch(Status.INFO, "Setting locale to \"en_AU\"");
        //StatusPrinter.print(loggerContext);
    }



    // reproduction requires placing a binary properties file. Probably not worth the effort.
//    @Test
//    public void ossFuzz_47249() throws JoranException  {
//        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "ossfuzz/fuzz-47249.xml");
//         StatusPrinter.print(loggerContext);
//    }

//	@Test
//	public void doTest() throws JoranException {
//		int LIMIT = 0;
//		boolean oss = true;
//		for (int i = 0; i < LIMIT; i++) {
//			innerDoT(oss);
//		}
//		long start = System.currentTimeMillis();
//		innerDoT(oss);
//		long diff = System.currentTimeMillis() - start;
//		double average = (1.0d * diff);
//		System.out.println("Average time " + average + " ms. By serialization " + oss);
//
//	}

//	private void innerDoT(boolean oss) throws JoranException {
//		JoranConfigurator jc = new JoranConfigurator();
//		jc.setContext(loggerContext);
//		if (oss) {
//			System.out.println("jc.doT");
//			jc.doT();
//		} else {
//			System.out.println("jc.doConfigure");
//			jc.doConfigure(ClassicTestConstants.JORAN_INPUT_PREFIX + "twoAppenders.xml");
//		}
//	}

}
