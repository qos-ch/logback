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
package ch.qos.logback.classic.sift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.MDC;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.helpers.NOPAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.sift.AppenderFactory;
import ch.qos.logback.core.sift.AppenderTracker;
import ch.qos.logback.core.spi.AbstractComponentTracker;
import ch.qos.logback.core.spi.ComponentTracker;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.testUtil.StringListAppender;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.StatusPrinter;

public class SiftingAppenderTest {

    static String SIFT_FOLDER_PREFIX = ClassicTestConstants.JORAN_INPUT_PREFIX + "sift/";

    LoggerContext loggerContext = new LoggerContext();
    Logger logger = loggerContext.getLogger(this.getClass().getName());
    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    StatusChecker statusChecker = new StatusChecker(loggerContext);
    int diff = RandomUtil.getPositiveInt();
    int now = 0;

    protected void configure(String file) throws JoranException {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        jc.doConfigure(file);
    }

    @Before
    public void setUp() {
        MDC.clear();
    }

    @After
    public void tearDown() {
        MDC.clear();
    }

    @Test
    public void unsetDefaultValueProperty() throws JoranException {
        configure(SIFT_FOLDER_PREFIX + "unsetDefaultValueProperty.xml");
        logger.debug("hello");
        SiftingAppender sa = (SiftingAppender) root.getAppender("SIFT");
        assertFalse(sa.isStarted());
    }

    @Test
    public void smoke() throws JoranException {
        configure(SIFT_FOLDER_PREFIX + "smoke.xml");
        logger.debug("smoke");
        Appender<ILoggingEvent> appender = getAppenderTracker().find("smokeDefault");
        assertNotNull(appender);
        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) appender;
        List<ILoggingEvent> eventList = listAppender.list;
        assertEquals(1, listAppender.list.size());
        assertEquals("smoke", eventList.get(0).getMessage());
    }

    private AppenderTracker<ILoggingEvent> getAppenderTracker() {
        SiftingAppender ha = (SiftingAppender) root.getAppender("SIFT");
        return ha.getAppenderTracker();
    }

    @Test
    public void zeroNesting() throws JoranException {
        configure(SIFT_FOLDER_PREFIX + "zeroNesting.xml");
        logger.debug("hello");
        logger.debug("hello");
        logger.debug("hello");
        logger.debug("hello");
        logger.debug("hello");
        
        Appender<ILoggingEvent> nopa = getAppenderTracker().find("zeroDefault");
        assertNotNull(nopa);
        assertThat(nopa).isInstanceOf(NOPAppender.class);
        StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);

        statusChecker.assertContainsMatch(ErrorStatus.ERROR, "No nested appenders found");
    }

    @Test
    public void multipleNesting() throws JoranException {
        configure(SIFT_FOLDER_PREFIX + "multipleNesting.xml");
        logger.debug("hello");
        logger.debug("hello");
        logger.debug("hello");

        Appender<ILoggingEvent> listAppender = getAppenderTracker().find("multipleDefault");
        StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);

        assertNotNull(listAppender);
        statusChecker.assertContainsMatch(ErrorStatus.ERROR, "Only and only one appender can be nested");
    }

    @Test
    public void defaultLayoutRule() throws JoranException {
        configure(SIFT_FOLDER_PREFIX + "defaultLayoutRule.xml");
        logger.debug("hello");
        SiftingAppender ha = (SiftingAppender) root.getAppender("SIFT");
        StringListAppender<ILoggingEvent> listAppender = (StringListAppender<ILoggingEvent>) ha.getAppenderTracker().find("default");

        assertNotNull(listAppender);
        List<String> strList = listAppender.strList;
        assertEquals(1, strList.size());
        assertEquals("DEBUG hello", strList.get(0));
    }

    @Test
    public void testWholeCycle() throws JoranException {
        String mdcKey = "cycle";
        configure(SIFT_FOLDER_PREFIX + "completeCycle.xml");
        MDC.put(mdcKey, "a");
        logger.debug("smoke");
        long timestamp = System.currentTimeMillis();
        SiftingAppender sa = (SiftingAppender) root.getAppender("SIFT");
        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) sa.getAppenderTracker().find("a");
        assertNotNull(listAppender);
        List<ILoggingEvent> eventList = listAppender.list;
        assertEquals(1, listAppender.list.size());
        assertEquals("smoke", eventList.get(0).getMessage());

        MDC.remove(mdcKey);
        LoggingEvent le = new LoggingEvent("x", logger, Level.INFO, "hello", null, null);
        le.setTimeStamp(timestamp + ComponentTracker.DEFAULT_TIMEOUT + 1);
        sa.doAppend(le);
        assertFalse(listAppender.isStarted());
        assertEquals(1, sa.getAppenderTracker().allKeys().size());
        assertTrue(sa.getAppenderTracker().allKeys().contains("cycleDefault"));
    }

    @Test
    public void sessionFinalizationShouldCauseLingering() throws JoranException {
        String mdcKey = "linger";
        String mdcVal = "session" + diff;
        configure(SIFT_FOLDER_PREFIX + "lingering.xml");
        MDC.put(mdcKey, mdcVal);
        logger.debug("linger 1");
        logger.debug(ClassicConstants.FINALIZE_SESSION_MARKER, "linger 2");
        long now = System.currentTimeMillis();
        SiftingAppender sa = (SiftingAppender) root.getAppender("SIFT");
        AppenderTracker<ILoggingEvent> tracker = sa.getAppenderTracker();

        assertEquals(1, tracker.allKeys().size());
        Appender<ILoggingEvent> appender = tracker.find(mdcVal);
        assertTrue(appender.isStarted());

        tracker.removeStaleComponents(now + AppenderTracker.LINGERING_TIMEOUT + 1);
        // previously lingering appenders should be closed upon timeout
        assertFalse(appender.isStarted());
        // and they should be gone
        assertEquals(0, tracker.allKeys().size());
    }

    @Test
    public void localPropertiesShouldBeVisible() throws JoranException {
        String mdcKey = "localProperty";
        String mdcVal = "" + diff;
        String msg = "localPropertiesShouldBeVisible";
        String prefix = "Y";
        configure(SIFT_FOLDER_PREFIX + "propertyPropagation.xml");
        MDC.put(mdcKey, mdcVal);
        logger.debug(msg);
        SiftingAppender sa = (SiftingAppender) root.getAppender("SIFT");
        StringListAppender<ILoggingEvent> listAppender = (StringListAppender<ILoggingEvent>) sa.getAppenderTracker().find(mdcVal);
        assertNotNull(listAppender);
        List<String> strList = listAppender.strList;
        assertEquals(1, listAppender.strList.size());
        assertEquals(prefix + msg, strList.get(0));
    }

    @Test
    public void propertyDefinedWithinSiftElementShouldBeVisible() throws JoranException {
        String mdcKey = "propertyDefinedWithinSift";
        String mdcVal = "" + diff;
        String msg = "propertyDefinedWithinSiftElementShouldBeVisible";
        String prefix = "Y";
        configure(SIFT_FOLDER_PREFIX + "propertyDefinedInSiftElement.xml");
        MDC.put(mdcKey, mdcVal);
        logger.debug(msg);
        SiftingAppender sa = (SiftingAppender) root.getAppender("SIFT");
        StringListAppender<ILoggingEvent> listAppender = (StringListAppender<ILoggingEvent>) sa.getAppenderTracker().find(mdcVal);
        assertNotNull(listAppender);
        List<String> strList = listAppender.strList;
        assertEquals(1, listAppender.strList.size());
        assertEquals(prefix + msg, strList.get(0));
    }

    @Test
    public void compositePropertyShouldCombineWithinAndWithoutSiftElement() throws JoranException {
        String mdcKey = "compositeProperty";
        String mdcVal = "" + diff;
        String msg = "compositePropertyShouldCombineWithinAndWithoutSiftElement";
        String prefix = "composite";
        configure(SIFT_FOLDER_PREFIX + "compositeProperty.xml");
        MDC.put(mdcKey, mdcVal);
        logger.debug(msg);
        SiftingAppender sa = (SiftingAppender) root.getAppender("SIFT");
        StringListAppender<ILoggingEvent> listAppender = (StringListAppender<ILoggingEvent>) sa.getAppenderTracker().find(mdcVal);
        assertNotNull(listAppender);
        List<String> strList = listAppender.strList;
        assertEquals(1, listAppender.strList.size());
        assertEquals(prefix + msg, strList.get(0));
    }

    @Test
    public void maxAppendersCountPropertyShouldBeHonored() throws JoranException {
        configure(SIFT_FOLDER_PREFIX + "maxAppenderCount.xml");
        int max = 5;
        SiftingAppender sa = (SiftingAppender) root.getAppender("SIFT");
        String mdcKey = "max";
        for (int i = 0; i <= max; i++) {
            MDC.put(mdcKey, "" + (diff + i));
            LoggingEvent event = new LoggingEvent("", logger, Level.DEBUG, "max" + i, null, null);
            event.setTimeStamp(now);
            sa.doAppend(event);
            now += AbstractComponentTracker.WAIT_BETWEEN_SUCCESSIVE_REMOVAL_ITERATIONS;
        }
        AppenderTracker<ILoggingEvent> tracker = sa.getAppenderTracker();
        assertEquals(max, tracker.allKeys().size());
        assertNull(tracker.find("" + (diff + 0)));
        for (int i = 1; i <= max; i++) {
            assertNotNull(tracker.find("" + (diff + i)));
        }
    }

    @Test
    public void timeoutPropertyShouldBeHonored() throws JoranException, InterruptedException {
        configure(SIFT_FOLDER_PREFIX + "timeout.xml");
        long timeout = 30 * 1000;
        SiftingAppender sa = (SiftingAppender) root.getAppender("SIFT");

        LoggingEvent event = new LoggingEvent("", logger, Level.DEBUG, "timeout", null, null);
        event.setTimeStamp(now);
        sa.doAppend(event);

        AppenderTracker<ILoggingEvent> tracker = sa.getAppenderTracker();

        assertEquals(1, tracker.getComponentCount());

        now += timeout + 1;
        tracker.removeStaleComponents(now);
        assertEquals(0, tracker.getComponentCount());
        statusChecker.assertIsErrorFree();
    }

    
    // LOGBACK-1127
    @Ignore
    @Test 
    public void programmicSiftingAppender() {
        
        SiftingAppender connectorAppender = new SiftingAppender();
        connectorAppender.setContext(loggerContext);
        connectorAppender.setName("SIFTING_APPENDER");
                        
        MDCBasedDiscriminator discriminator = new MDCBasedDiscriminator();
        discriminator.setKey("SKEY");
        discriminator.setDefaultValue("DEF_KEY");
        discriminator.start();
        connectorAppender.setDiscriminator(discriminator);

        connectorAppender.setAppenderFactory(new AppenderFactory<ILoggingEvent>() {

                @Override
                public Appender<ILoggingEvent> buildAppender(Context context, String discriminatingValue) throws JoranException {

                        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<ILoggingEvent>();
                        appender.setName("ROLLING_APPENDER_"+discriminatingValue);
                        appender.setContext(context);
                        appender.setFile("/var/logs/active_"+discriminatingValue+".log");

                        TimeBasedRollingPolicy<ILoggingEvent> policy = new TimeBasedRollingPolicy<ILoggingEvent>();
                        policy.setContext(context);
                        policy.setMaxHistory(365);
                        policy.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX+"/logback1127/"+discriminatingValue+"_%d{yyyy_MM_dd}_%i.log");
                        policy.setParent(appender);
                        policy.setCleanHistoryOnStart(true);

                        SizeAndTimeBasedFNATP<ILoggingEvent> innerpolicy = new SizeAndTimeBasedFNATP<ILoggingEvent>();
                        innerpolicy.setContext(context);
                        innerpolicy.setMaxFileSize(FileSize.valueOf("5KB"));
                        innerpolicy.setTimeBasedRollingPolicy(policy);

                        policy.setTimeBasedFileNamingAndTriggeringPolicy(innerpolicy);
                        policy.start();
                        appender.setRollingPolicy(policy);

                        PatternLayoutEncoder pl = new PatternLayoutEncoder();
                        pl.setContext(context);
                        pl.setPattern("%d{yyyy/MM/dd'T'HH:mm:ss} %-5level - %msg\n");

                        pl.start();
                        appender.setEncoder(pl);

                        appender.start();
                        return appender;
                }
        });
        connectorAppender.start();
                
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger("org.test");
        logger.addAppender(connectorAppender);
        logger.setLevel(Level.DEBUG);
        logger.setAdditive(false);

        MDC.put("SKEY", "K1");
        logger.info("bla1");
        MDC.clear();
                
        MDC.put("SKEY", "K2");
        logger.info("bla2");
        MDC.clear();

        StatusPrinter.print(loggerContext);
        
    }
}
