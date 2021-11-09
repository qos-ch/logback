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
package ch.qos.logback.classic.pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.net.SyslogConstants;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.pattern.FormatInfo;


public class ConverterTest {

    LoggerContext lc = new LoggerContext();
    Logger logger = lc.getLogger(ConverterTest.class);
    LoggingEvent le;
    List<String> optionList = new ArrayList<>();

    // The LoggingEvent is massaged with an FCQN of FormattingConverter. This
    // forces the returned caller information to match the caller stack for this
    // this particular test.
    LoggingEvent makeLoggingEvent(final Exception ex) {
        return new LoggingEvent(ch.qos.logback.core.pattern.FormattingConverter.class.getName(), logger, Level.INFO, "Some message", ex, null);
    }

    Exception getException(final String msg, final Exception cause) {
        return new Exception(msg, cause);
    }

    @Before
    public void setUp() throws Exception {
        final Exception rootEx = getException("Innermost", null);
        final Exception nestedEx = getException("Nested", rootEx);

        final Exception ex = new Exception("Bogus exception", nestedEx);

        le = makeLoggingEvent(ex);
    }

    @Test
    public void testLineOfCaller() {
        {
            final DynamicConverter<ILoggingEvent> converter = new LineOfCallerConverter();
            final StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            // the number below should be the line number of the previous line
            assertEquals("75", buf.toString());
        }
    }

    @Test
    public void testLevel() {
        {
            final DynamicConverter<ILoggingEvent> converter = new LevelConverter();
            final StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            assertEquals("INFO", buf.toString());
        }
        {
            final DynamicConverter<ILoggingEvent> converter = new LevelConverter();
            converter.setFormattingInfo(new FormatInfo(1, 1, true, false));
            final StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            assertEquals("I", buf.toString());
        }
    }

    @Test
    public void testThread() {
        final DynamicConverter<ILoggingEvent> converter = new ThreadConverter();
        final StringBuilder buf = new StringBuilder();
        converter.write(buf, le);
        System.out.println(buf.toString());
        final String regex = ClassicTestConstants.NAKED_MAIN_REGEX;
        assertTrue(buf.toString().matches(regex));
    }

    @Test
    public void testMessage() {
        final DynamicConverter<ILoggingEvent> converter = new MessageConverter();
        final StringBuilder buf = new StringBuilder();
        converter.write(buf, le);
        assertEquals("Some message", buf.toString());
    }

    @Test
    public void testLineSeparator() {
        final DynamicConverter<ILoggingEvent> converter = new LineSeparatorConverter();
        final StringBuilder buf = new StringBuilder();
        converter.write(buf, le);
        assertEquals(CoreConstants.LINE_SEPARATOR, buf.toString());
    }

    @Test
    public void testException() {
        {
            final DynamicConverter<ILoggingEvent> converter = new ThrowableProxyConverter();
            final StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
        }

        {
            final DynamicConverter<ILoggingEvent> converter = new ThrowableProxyConverter();
            optionList.add("3");
            converter.setOptionList(optionList);
            final StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
        }
    }

    @Test
    public void testLogger() {
        {
            final ClassicConverter converter = new LoggerConverter();
            final StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            assertEquals(this.getClass().getName(), buf.toString());
        }

        {
            final ClassicConverter converter = new LoggerConverter();
            optionList.add("20");
            converter.setOptionList(optionList);
            converter.start();
            final StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            assertEquals("c.q.l.c.p.ConverterTest", buf.toString());
        }

        {
            final DynamicConverter<ILoggingEvent> converter = new LoggerConverter();
            optionList.clear();
            optionList.add("0");
            converter.setOptionList(optionList);
            converter.start();
            final StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            assertEquals("ConverterTest", buf.toString());
        }
    }

    @Test
    public void testVeryLongLoggerName() {
        final ClassicConverter converter = new LoggerConverter();
        optionList.add("5");
        converter.setOptionList(optionList);
        converter.start();
        final StringBuilder buf = new StringBuilder();

        final char c = 'a';
        final int extraParts = 3;
        final int totalParts = ClassicConstants.MAX_DOTS + extraParts;
        final StringBuilder loggerNameBuf = new StringBuilder();
        final StringBuilder witness = new StringBuilder();

        for (int i = 0; i < totalParts; i++) {
            loggerNameBuf.append(c).append(c).append(c);
            witness.append(c);
            loggerNameBuf.append('.');
            witness.append('.');
        }
        loggerNameBuf.append("zzzzzz");
        witness.append("zzzzzz");

        le.setLoggerName(loggerNameBuf.toString());
        converter.write(buf, le);
        assertEquals(witness.toString(), buf.toString());
    }

    @Test
    public void testClass() {
        final DynamicConverter<ILoggingEvent> converter = new ClassOfCallerConverter();
        final StringBuilder buf = new StringBuilder();
        converter.write(buf, le);
        assertEquals(this.getClass().getName(), buf.toString());
    }

    @Test
    public void testMethodOfCaller() {
        final DynamicConverter<ILoggingEvent> converter = new MethodOfCallerConverter();
        final StringBuilder buf = new StringBuilder();
        converter.write(buf, le);
        assertEquals("testMethodOfCaller", buf.toString());
    }

    @Test
    public void testFileOfCaller() {
        final DynamicConverter<ILoggingEvent> converter = new FileOfCallerConverter();
        final StringBuilder buf = new StringBuilder();
        converter.write(buf, le);
        assertEquals("ConverterTest.java", buf.toString());
    }

    @Test
    public void testCallerData() {
        {
            final DynamicConverter<ILoggingEvent> converter = new CallerDataConverter();
            converter.start();

            final StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            if (buf.length() < 10) {
                fail("buf is too short");
            }
        }

        {
            final DynamicConverter<ILoggingEvent> converter = new CallerDataConverter();
            optionList.add("2");
            optionList.add("XXX");
            converter.setOptionList(optionList);
            converter.start();

            final StringBuilder buf = new StringBuilder();
            final LoggingEvent event = makeLoggingEvent(null);
            event.addMarker(MarkerFactory.getMarker("XXX"));
            converter.write(buf, event);
            if (buf.length() < 10) {
                fail("buf is too short");
            }
        }

        {
            final DynamicConverter<ILoggingEvent> converter = new CallerDataConverter();
            optionList.clear();
            optionList.add("2");
            optionList.add("XXX");
            optionList.add("*");
            converter.setOptionList(optionList);
            converter.start();

            final StringBuilder buf = new StringBuilder();
            final LoggingEvent event = makeLoggingEvent(null);
            event.addMarker(MarkerFactory.getMarker("YYY"));
            converter.write(buf, event);
            if (buf.length() < 10) {
                fail("buf is too short");
            }
        }
        {
            final DynamicConverter<ILoggingEvent> converter = new CallerDataConverter();
            optionList.clear();
            optionList.add("2");
            optionList.add("XXX");
            optionList.add("+");
            converter.setOptionList(optionList);
            converter.start();

            final StringBuilder buf = new StringBuilder();
            final LoggingEvent event = makeLoggingEvent(null);
            event.addMarker(MarkerFactory.getMarker("YYY"));
            converter.write(buf, event);
            if (buf.length() < 10) {
                fail("buf is too short");
            }
        }

        {
            final DynamicConverter<ILoggingEvent> converter = new CallerDataConverter();
            optionList.clear();
            optionList.add("2");
            optionList.add("XXX");
            optionList.add("*");
            converter.setOptionList(optionList);
            converter.start();

            final StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            if (buf.length() < 10) {
                fail("buf is too short");
            }
            // System.out.println(buf);
        }

        {
            final DynamicConverter<ILoggingEvent> converter = new CallerDataConverter();
            optionList.clear();
            optionList.add("4..5");
            converter.setOptionList(optionList);
            converter.start();

            final StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            assertTrue("buf is too short", buf.length() >= 10);

            final String expectedRegex = "Caller\\+4\t at (java.base\\/)?java.lang.reflect.Method.invoke.*$";
            final String actual = buf.toString();
            assertTrue("actual: "+actual, Pattern.compile(expectedRegex).matcher(actual).find());

        }
    }

    @Test
    public void testRelativeTime() throws Exception {
        final DynamicConverter<ILoggingEvent> converter = new RelativeTimeConverter();
        final StringBuilder buf0 = new StringBuilder();
        final StringBuilder buf1 = new StringBuilder();
        final long timestamp = System.currentTimeMillis();
        final LoggingEvent e0 = makeLoggingEvent(null);
        e0.setTimeStamp(timestamp);
        final LoggingEvent e1 = makeLoggingEvent(null);
        e1.setTimeStamp(timestamp);
        converter.write(buf0, e0);
        converter.write(buf1, e1);
        assertEquals(buf0.toString(), buf1.toString());
    }

    @Test
    public void testSyslogStart() throws Exception {
        final DynamicConverter<ILoggingEvent> converter = new SyslogStartConverter();
        optionList.clear();
        optionList.add("MAIL");
        converter.setOptionList(optionList);
        converter.start();

        final ILoggingEvent event = makeLoggingEvent(null);

        final StringBuilder buf = new StringBuilder();
        converter.write(buf, event);

        final String expected = "<" + (SyslogConstants.LOG_MAIL + SyslogConstants.INFO_SEVERITY) + ">";
        assertTrue(buf.toString().startsWith(expected));
    }

    @Test
    public void testMDCConverter() throws Exception {
        MDC.clear();
        MDC.put("someKey", "someValue");
        final MDCConverter converter = new MDCConverter();
        optionList.clear();
        optionList.add("someKey");
        converter.setOptionList(optionList);
        converter.start();

        final ILoggingEvent event = makeLoggingEvent(null);

        final String result = converter.convert(event);
        assertEquals("someValue", result);
    }

    @Test
    public void contextNameConverter() {
        final ClassicConverter converter = new ContextNameConverter();
        // see http://jira.qos.ch/browse/LBCLASSIC-149
        final LoggerContext lcOther = new LoggerContext();
        lcOther.setName("another");
        converter.setContext(lcOther);

        lc.setName("aValue");
        final ILoggingEvent event = makeLoggingEvent(null);

        final String result = converter.convert(event);
        assertEquals("aValue", result);
    }

    @Test
    public void contextProperty() {
        final PropertyConverter converter = new PropertyConverter();
        converter.setContext(lc);
        final List<String> ol = new ArrayList<>();
        ol.add("k");
        converter.setOptionList(ol);
        converter.start();
        lc.setName("aValue");
        lc.putProperty("k", "v");
        final ILoggingEvent event = makeLoggingEvent(null);

        final String result = converter.convert(event);
        assertEquals("v", result);
    }
}
