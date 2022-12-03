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
package ch.qos.logback.classic;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.turbo.MDCFilter;
import ch.qos.logback.classic.turbo.MarkerFilter;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TurboFilteringInLoggerTest {

    static final String BLUE = "BLUE";
    LoggerContext loggerContext;
    Logger logger;
    Marker blueMarker = MarkerFactory.getMarker(BLUE);

    int diff = RandomUtil.getPositiveInt();
    String key = "tfiolKey" + diff;
    String value = "val" + diff;

    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    MDCFilter mdcFilter = new MDCFilter();
    @BeforeEach
    public void setUp() throws Exception {
        loggerContext = new LoggerContext();
        loggerContext.setName("test");
        loggerContext.start();
        logger = loggerContext.getLogger(TurboFilteringInLoggerTest.class);

        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ERROR);
        listAppender.start();
        root.addAppender(listAppender);

    }

    private void addMDCFilter() {

        mdcFilter.setOnMatch("ACCEPT");
        mdcFilter.setOnMismatch("DENY");
        mdcFilter.setMDCKey(key);
        mdcFilter.setValue(value);
        mdcFilter.start();
        loggerContext.addTurboFilter(mdcFilter);
    }
    private void addYesFilter() {
        YesFilter filter = new YesFilter();
        filter.start();
        loggerContext.addTurboFilter(filter);
    }

    private void addNoFilter() {
        NoFilter filter = new NoFilter();
        filter.start();
        loggerContext.addTurboFilter(filter);
    }

    private void addAcceptBLUEFilter() {
        MarkerFilter filter = new MarkerFilter();
        filter.setMarker(BLUE);
        filter.setOnMatch("ACCEPT");
        filter.start();
        loggerContext.addTurboFilter(filter);
    }

    private void addDenyBLUEFilter() {
        MarkerFilter filter = new MarkerFilter();
        filter.setMarker(BLUE);
        filter.setOnMatch("DENY");
        filter.start();
        loggerContext.addTurboFilter(filter);
    }

    @Test
    public void testIsDebugEnabledWithYesFilter() {
        addYesFilter();
        logger.setLevel(Level.INFO);
        assertTrue(logger.isDebugEnabled());
    }

    @Test
    public void testIsInfoEnabledWithYesFilter() {
        addYesFilter();
        logger.setLevel(Level.WARN);
        assertTrue(logger.isInfoEnabled());
    }

    @Test
    public void testIsWarnEnabledWithYesFilter() {
        addYesFilter();
        logger.setLevel(Level.ERROR);
        assertTrue(logger.isWarnEnabled());
    }

    @Test
    public void testIsErrorEnabledWithYesFilter() {
        addYesFilter();
        logger.setLevel(Level.OFF);
        assertTrue(logger.isErrorEnabled());
    }

    @Test
    public void testIsEnabledForWithYesFilter() {
        addYesFilter();
        logger.setLevel(Level.ERROR);
        assertTrue(logger.isEnabledFor(Level.INFO));
    }

    @Test
    public void testIsEnabledForWithNoFilter() {
        addNoFilter();
        logger.setLevel(Level.DEBUG);
        assertFalse(logger.isEnabledFor(Level.INFO));
    }

    @Test
    public void testIsDebugEnabledWithNoFilter() {
        addNoFilter();
        logger.setLevel(Level.DEBUG);
        assertFalse(logger.isDebugEnabled());
    }

    @Test
    public void testIsInfoEnabledWithNoFilter() {
        addNoFilter();
        logger.setLevel(Level.DEBUG);
        assertFalse(logger.isInfoEnabled());
    }

    @Test
    public void testIsWarnEnabledWithNoFilter() {
        addNoFilter();
        logger.setLevel(Level.DEBUG);
        assertFalse(logger.isWarnEnabled());
    }

    @Test
    public void testIsErrorEnabledWithNoFilter() {
        addNoFilter();
        logger.setLevel(Level.DEBUG);
        assertFalse(logger.isErrorEnabled());
    }

    @Test
    public void testIsErrorEnabledWithAcceptBlueFilter() {
        addAcceptBLUEFilter();
        logger.setLevel(Level.ERROR);
        assertTrue(logger.isDebugEnabled(blueMarker));
    }

    @Test
    public void testIsErrorEnabledWithDenyBlueFilter() {
        addDenyBLUEFilter();
        logger.setLevel(Level.ALL);
        assertFalse(logger.isDebugEnabled(blueMarker));
    }

    @Test
    public void testLoggingContextReset() {
        addYesFilter();
        assertNotNull(loggerContext.getTurboFilterList().get(0));
        loggerContext.reset();
        assertEquals(0, loggerContext.getTurboFilterList().size());
    }

    @Test
    public void fluentAPI() {
        addMDCFilter();
        Logger logger = loggerContext.getLogger(this.getClass());
        logger.atDebug().log("hello 1");
        assertEquals(0, listAppender.list.size());
        MDC.put(key, value);
        logger.atDebug().log("hello 2");
        assertEquals(1, listAppender.list.size());
    }
}

class YesFilter extends TurboFilter {
    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        return FilterReply.ACCEPT;
    }
}

class NoFilter extends TurboFilter {
    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        return FilterReply.DENY;
    }
}