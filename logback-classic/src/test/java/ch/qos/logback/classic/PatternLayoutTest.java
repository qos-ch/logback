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

import static ch.qos.logback.classic.ClassicTestConstants.ISO_REGEX;
import static ch.qos.logback.classic.ClassicTestConstants.MAIN_REGEX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.pattern.ConverterTest;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.testUtil.SampleConverter;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import ch.qos.logback.core.pattern.parser.test.AbstractPatternLayoutBaseTest;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.testUtil.StringListAppender;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusPrinter;

public class PatternLayoutTest extends AbstractPatternLayoutBaseTest<ILoggingEvent> {

    private final PatternLayout pl = new PatternLayout();
    private final LoggerContext lc = new LoggerContext();
    Logger logger = lc.getLogger(ConverterTest.class);
    Logger root = lc.getLogger(Logger.ROOT_LOGGER_NAME);

    String aMessage = "Some message";

    ILoggingEvent le;

    public PatternLayoutTest() {
        final Exception ex = new Exception("Bogus exception");
        le = makeLoggingEvent(aMessage, ex);
    }

    @Before
    public void setUp() {
        pl.setContext(lc);
    }

    ILoggingEvent makeLoggingEvent(final String msg, final Exception ex) {
        return new LoggingEvent(ch.qos.logback.core.pattern.FormattingConverter.class.getName(), logger, Level.INFO, msg, ex, null);
    }

    @Override
    public ILoggingEvent getEventObject() {
        return makeLoggingEvent("Some message", null);
    }

    @Override
    public PatternLayoutBase<ILoggingEvent> getPatternLayoutBase() {
        return new PatternLayout();
    }

    @Test
    public void testOK() {
        pl.setPattern("%d %le [%t] %lo{30} - %m%n");
        pl.start();
        final String val = pl.doLayout(getEventObject());
        // 2006-02-01 22:38:06,212 INFO [main] c.q.l.pattern.ConverterTest - Some
        // message
        // 2010-12-29 19:04:26,137 INFO [pool-1-thread-47] c.q.l.c.pattern.ConverterTest - Some message
        final String regex = ISO_REGEX + " INFO " + MAIN_REGEX + " c.q.l.c.pattern.ConverterTest - Some message\\s*";

        assertTrue("val=" + val, val.matches(regex));
    }

    @Test
    public void testNoExeptionHandler() {
        pl.setPattern("%m%n");
        pl.start();
        final String val = pl.doLayout(le);
        assertTrue(val.contains("java.lang.Exception: Bogus exception"));
    }

    @Test
    public void testCompositePattern() {
        pl.setPattern("%-56(%d %lo{20}) - %m%n");
        pl.start();
        final String val = pl.doLayout(getEventObject());
        // 2008-03-18 21:55:54,250 c.q.l.c.pattern.ConverterTest - Some message
        final String regex = ISO_REGEX + " c.q.l.c.p.ConverterTest          - Some message\\s*";
        assertTrue(val.matches(regex));
    }

    @Test
    public void contextProperty() {
        pl.setPattern("%property{a}");
        pl.start();
        lc.putProperty("a", "b");

        final String val = pl.doLayout(getEventObject());
        assertEquals("b", val);
    }

    @Test
    public void testNopExeptionHandler() {
        pl.setPattern("%nopex %m%n");
        pl.start();
        final String val = pl.doLayout(le);
        assertTrue(!val.contains("java.lang.Exception: Bogus exception"));
    }

    @Test
    public void testWithParenthesis() {
        pl.setPattern("\\(%msg:%msg\\) %msg");
        pl.start();
        le = makeLoggingEvent(aMessage, null);
        final String val = pl.doLayout(le);
        assertEquals("(Some message:Some message) Some message", val);
    }

    @Test
    public void testWithLettersComingFromLog4j() {
        // Letters: p = level and c = logger
        pl.setPattern("%d %p [%t] %c{30} - %m%n");
        pl.start();
        final String val = pl.doLayout(getEventObject());
        // 2006-02-01 22:38:06,212 INFO [main] c.q.l.pattern.ConverterTest - Some
        // message
        final String regex = ClassicTestConstants.ISO_REGEX + " INFO " + MAIN_REGEX + " c.q.l.c.pattern.ConverterTest - Some message\\s*";
        assertTrue(val.matches(regex));
    }

    @Test
    public void mdcWithDefaultValue() throws ScanException {
        final String pattern = "%msg %mdc{foo} %mdc{bar:-[null]}";
        pl.setPattern(OptionHelper.substVars(pattern, lc));
        pl.start();
        MDC.put("foo", "foo");
        try {
            final String val = pl.doLayout(getEventObject());
            assertEquals("Some message foo [null]", val);
        } finally {
            MDC.remove("foo");
        }
    }

    @Test
    public void contextNameTest() {
        pl.setPattern("%contextName");
        lc.setName("aValue");
        pl.start();
        final String val = pl.doLayout(getEventObject());
        assertEquals("aValue", val);
    }

    @Test
    public void cnTest() {
        pl.setPattern("%cn");
        lc.setName("aValue");
        pl.start();
        final String val = pl.doLayout(getEventObject());
        assertEquals("aValue", val);
    }

    @Override
    public Context getContext() {
        return lc;
    }

    void configure(final String file) throws JoranException {
        final JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(lc);
        jc.doConfigure(file);
    }

    @Test
    public void testConversionRuleSupportInPatternLayout() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "conversionRule/patternLayout0.xml");
        root.getAppender("LIST");
        final String msg = "Simon says";
        logger.debug(msg);
        final StringListAppender<ILoggingEvent> sla = (StringListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertNotNull(sla);
        assertEquals(1, sla.strList.size());
        assertEquals(SampleConverter.SAMPLE_STR + " - " + msg, sla.strList.get(0));
    }

    @Test
    public void smokeReplace() {
        pl.setPattern("%replace(a1234b){'\\d{4}', 'XXXX'}");
        pl.start();
        StatusPrinter.print(lc);
        final String val = pl.doLayout(getEventObject());
        assertEquals("aXXXXb", val);
    }

    @Test
    public void replaceNewline() throws ScanException {
        final String pattern = "%replace(A\nB){'\n', '\n\t'}";
        final String substPattern = OptionHelper.substVars(pattern, null, lc);
        assertEquals(pattern, substPattern);
        pl.setPattern(substPattern);
        pl.start();
        StatusPrinter.print(lc);
        final String val = pl.doLayout(makeLoggingEvent("", null));
        assertEquals("A\n\tB", val);
    }

    @Test
    public void replaceWithJoran() throws JoranException {
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "pattern/replace0.xml");
        StatusPrinter.print(lc);
        root.getAppender("LIST");
        final String msg = "And the number is 4111111111110000, expiring on 12/2010";
        logger.debug(msg);
        final StringListAppender<ILoggingEvent> sla = (StringListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertNotNull(sla);
        assertEquals(1, sla.strList.size());
        assertEquals("And the number is XXXX, expiring on 12/2010", sla.strList.get(0));
    }

    @Test
    public void replaceWithJoran_NEWLINE() throws JoranException {
        lc.putProperty("TAB", "\t");
        configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "pattern/replaceNewline.xml");
        StatusPrinter.print(lc);
        root.getAppender("LIST");
        final String msg = "A\nC";
        logger.debug(msg);
        final StringListAppender<ILoggingEvent> sla = (StringListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertNotNull(sla);
        assertEquals(1, sla.strList.size());
        assertEquals("A\n\tC", sla.strList.get(0));
    }

    @Test
    public void prefixConverterSmoke() {
        final String pattern = "%prefix(%logger) %message";
        pl.setPattern(pattern);
        pl.start();
        final String val = pl.doLayout(makeLoggingEvent("hello", null));
        assertEquals("logger=" + logger.getName() + " hello", val);
    }

    @Test
    public void prefixConverterWithMDC() {
        final String mdcKey = "boo";
        final String mdcVal = "moo";

        final String pattern = "%prefix(%level %logger %X{" + mdcKey + "}) %message";
        pl.setPattern(pattern);
        pl.start();
        MDC.put(mdcKey, mdcVal);
        try {
            final String val = pl.doLayout(makeLoggingEvent("hello", null));

            assertEquals("level=" + "INFO logger=" + logger.getName() + " " + mdcKey + "=" + mdcVal + " hello", val);

        } finally {
            MDC.remove(mdcKey);
        }
    }

    @Test
    public void prefixConverterWithProperty() {

        try {
            final String propertyKey = "px1953";
            final String propertyVal = "pxVal";

            System.setProperty(propertyKey, propertyVal);

            final String pattern = "%prefix(%logger %property{" + propertyKey + "}) %message";
            pl.setPattern(pattern);
            pl.start();

            final String val = pl.doLayout(makeLoggingEvent("hello", null));

            assertEquals("logger=" + logger.getName() + " " + propertyKey + "=" + propertyVal + " hello", val);

        } finally {
            System.clearProperty("px");
        }
    }

}
