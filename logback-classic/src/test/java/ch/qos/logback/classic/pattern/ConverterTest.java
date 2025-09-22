/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.net.SyslogConstants;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.pattern.FormatInfo;
import ch.qos.logback.core.util.EnvUtil;
import ch.qos.logback.core.util.StatusPrinter;

public class ConverterTest {

    LoggerContext loggerContext = new LoggerContext();
    LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();
    Logger logger = loggerContext.getLogger(ConverterTest.class);
    LoggingEvent le;
    //List<String> optionList = new ArrayList<String>();

    // The LoggingEvent is massaged with an FCQN of FormattingConverter. This
    // forces the returned caller information to match the caller stack for
    // this particular test.
    LoggingEvent makeLoggingEvent(Exception ex) {
        return new LoggingEvent(ch.qos.logback.core.pattern.FormattingConverter.class.getName(), logger, Level.INFO,
                "Some message", ex, null);
    }

    Exception getException(String msg, Exception cause) {
        return new Exception(msg, cause);
    }

    @BeforeEach
    public void setUp() throws Exception {
        loggerContext.setMDCAdapter(logbackMDCAdapter);
        Exception rootEx = getException("Innermost", null);
        Exception nestedEx = getException("Nested", rootEx);

        Exception ex = new Exception("Bogus exception", nestedEx);

        le = makeLoggingEvent(ex);
    }

    @Test
    public void testLineOfCaller() {
        {
            DynamicConverter<ILoggingEvent> converter = new LineOfCallerConverter();
            StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            // the number below should be the line number of the previous line
            assertEquals("79", buf.toString());
        }
    }

    @Test
    public void testLevel() {
        {
            DynamicConverter<ILoggingEvent> converter = new LevelConverter();
            StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            assertEquals("INFO", buf.toString());
        }
        {
            DynamicConverter<ILoggingEvent> converter = new LevelConverter();
            converter.setFormattingInfo(new FormatInfo(1, 1, true, false));
            StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            assertEquals("I", buf.toString());
        }
    }

    @Test
    public void testThread() {
        DynamicConverter<ILoggingEvent> converter = new ThreadConverter();
        StringBuilder buf = new StringBuilder();
        converter.write(buf, le);
        System.out.println(buf.toString());
        String regex = ClassicTestConstants.NAKED_MAIN_REGEX;
        assertTrue(buf.toString().matches(regex));
    }

    @Test
    public void testMessage() {
        DynamicConverter<ILoggingEvent> converter = new MessageConverter();
        StringBuilder buf = new StringBuilder();
        converter.write(buf, le);
        assertEquals("Some message", buf.toString());
    }

    @Test
    public void testLineSeparator() {
        DynamicConverter<ILoggingEvent> converter = new LineSeparatorConverter();
        StringBuilder buf = new StringBuilder();
        converter.write(buf, le);
        assertEquals(CoreConstants.LINE_SEPARATOR, buf.toString());
    }

    @Test
    public void testException() {
        {
            DynamicConverter<ILoggingEvent> converter = new ThrowableProxyConverter();
            StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
        }

        {
            DynamicConverter<ILoggingEvent> converter = new ThrowableProxyConverter();
            converter.setOptionList(List.of("3"));
            StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
        }
    }

    @Test
    public void testLogger() {
        {
            ClassicConverter converter = new LoggerConverter();
            StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            assertEquals(this.getClass().getName(), buf.toString());
        }

        {
            ClassicConverter converter = new LoggerConverter();
            converter.setOptionList(List.of("20"));
            converter.start();
            StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            assertEquals("c.q.l.c.p.ConverterTest", buf.toString());
        }

        {
            DynamicConverter<ILoggingEvent> converter = new LoggerConverter();
            converter.setOptionList(List.of("0"));
            converter.start();
            StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            assertEquals("ConverterTest", buf.toString());
        }
    }

    @Test
    public void testVeryLongLoggerName() {
        ClassicConverter converter = new LoggerConverter();
        converter.setOptionList(List.of("5"));
        converter.start();
        StringBuilder buf = new StringBuilder();

        char c = 'a';
        int extraParts = 3;
        int totalParts = ClassicConstants.MAX_DOTS + extraParts;
        StringBuilder loggerNameBuf = new StringBuilder();
        StringBuilder witness = new StringBuilder();

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
        DynamicConverter<ILoggingEvent> converter = new ClassOfCallerConverter();
        StringBuilder buf = new StringBuilder();
        converter.write(buf, le);
        assertEquals(this.getClass().getName(), buf.toString());
    }

    @Test
    public void testMethodOfCaller() {
        DynamicConverter<ILoggingEvent> converter = new MethodOfCallerConverter();
        StringBuilder buf = new StringBuilder();
        converter.write(buf, le);
        assertEquals("testMethodOfCaller", buf.toString());
    }

    @Test
    public void testFileOfCaller() {
        DynamicConverter<ILoggingEvent> converter = new FileOfCallerConverter();
        StringBuilder buf = new StringBuilder();
        converter.write(buf, le);
        assertEquals("ConverterTest.java", buf.toString());
    }

    @Test
    public void testCallerData() {
        {
            DynamicConverter<ILoggingEvent> converter = new CallerDataConverter();
            converter.start();

            StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            if (buf.length() < 10) {
                fail("buf is too short");
            }
        }

        {
            DynamicConverter<ILoggingEvent> converter = new CallerDataConverter();
            converter.setOptionList(List.of("2", "XXX"));
            converter.start();

            StringBuilder buf = new StringBuilder();
            LoggingEvent event = makeLoggingEvent(null);
            event.addMarker(MarkerFactory.getMarker("XXX"));
            converter.write(buf, event);
            if (buf.length() < 10) {
                fail("buf is too short");
            }
        }

        {
            DynamicConverter<ILoggingEvent> converter = new CallerDataConverter();
            converter.setOptionList(List.of("2", "XXX", "*"));
            converter.start();

            StringBuilder buf = new StringBuilder();
            LoggingEvent event = makeLoggingEvent(null);
            event.addMarker(MarkerFactory.getMarker("YYY"));
            converter.write(buf, event);
            if (buf.length() < 10) {
                fail("buf is too short");
            }
        }
        {
            DynamicConverter<ILoggingEvent> converter = new CallerDataConverter();
            converter.setOptionList(List.of("2", "XXX", "*"));
            converter.start();

            StringBuilder buf = new StringBuilder();
            LoggingEvent event = makeLoggingEvent(null);
            event.addMarker(MarkerFactory.getMarker("YYY"));
            converter.write(buf, event);
            if (buf.length() < 10) {
                fail("buf is too short");
            }
        }

        {
            DynamicConverter<ILoggingEvent> converter = new CallerDataConverter();
            converter.setOptionList(List.of("2", "XXX", "*"));
            converter.start();

            StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            if (buf.length() < 10) {
                fail("buf is too short");
            }
            // System.out.println(buf);
        }

        {
            DynamicConverter<ILoggingEvent> converter = new CallerDataConverter();

            boolean jdk18 = EnvUtil.isJDK18OrHigher();
            // jdk 18EA creates a different stack trace
            converter.setOptionList(jdk18 ? List.of("2..3") : List.of("4..5"));
            converter.start();

            StringBuilder buf = new StringBuilder();
            converter.write(buf, le);
            assertTrue( buf.length() >= 10, "buf is too short");

            String expectedRegex = "Caller\\+4";
            if(jdk18) {
                expectedRegex = "Caller\\+2";
            }
            expectedRegex+="\t at (java.base\\/)?java.lang.reflect.Method.invoke.*$";
            String actual = buf.toString();
            assertTrue( Pattern.compile(expectedRegex).matcher(actual).find(), "actual: " + actual);

        }
    }

    @Test
    public void testRelativeTime() throws Exception {
        DynamicConverter<ILoggingEvent> converter = new RelativeTimeConverter();
        StringBuilder buf0 = new StringBuilder();
        StringBuilder buf1 = new StringBuilder();
        long timestamp = System.currentTimeMillis();
        LoggingEvent e0 = makeLoggingEvent(null);
        e0.setTimeStamp(timestamp);
        LoggingEvent e1 = makeLoggingEvent(null);
        e1.setTimeStamp(timestamp);
        converter.write(buf0, e0);
        converter.write(buf1, e1);
        assertEquals(buf0.toString(), buf1.toString());
    }

    @Test
    public void testSyslogStart() throws Exception {
        DynamicConverter<ILoggingEvent> converter = new SyslogStartConverter();
        converter.setOptionList(List.of("MAIL"));
        converter.start();

        ILoggingEvent event = makeLoggingEvent(null);

        StringBuilder buf = new StringBuilder();
        converter.write(buf, event);

        String expected = "<" + (SyslogConstants.LOG_MAIL + SyslogConstants.INFO_SEVERITY) + ">";
        assertTrue(buf.toString().startsWith(expected));
    }

    @Test
    public void testMDCConverter() throws Exception {
        logbackMDCAdapter.clear();
        logbackMDCAdapter.put("someKey", "someValue");
        MDCConverter converter = new MDCConverter();
        converter.setOptionList(List.of("someKey"));
        converter.start();

        ILoggingEvent event = makeLoggingEvent(null);

        String result = converter.convert(event);
        assertEquals("someValue", result);
    }

    @Test
    public void contextNameConverter() {
        ClassicConverter converter = new ContextNameConverter();
        // see http://jira.qos.ch/browse/LBCLASSIC-149
        LoggerContext lcOther = new LoggerContext();
        lcOther.setName("another");
        converter.setContext(lcOther);

        loggerContext.setName("aValue");
        ILoggingEvent event = makeLoggingEvent(null);

        String result = converter.convert(event);
        assertEquals("aValue", result);
    }

    @Test
    public void contextProperty() {
        PropertyConverter converter = new PropertyConverter();
        converter.setContext(loggerContext);
        converter.setOptionList(List.of("k"));
        converter.start();
        loggerContext.setName("aValue");
        loggerContext.putProperty("k", "v");
        ILoggingEvent event = makeLoggingEvent(null);

        String result = converter.convert(event);
        assertEquals("v", result);
    }
    
    @Test
    public void testSequenceNumber() {
        //lc.setSequenceNumberGenerator(new BasicSequenceNumberGenerator());
        SequenceNumberConverter converter = new SequenceNumberConverter();
        converter.setContext(loggerContext);
        converter.start();

        assertTrue(converter.isStarted());
        LoggingEvent event = makeLoggingEvent(null);

        event.setSequenceNumber(123);
        assertEquals("123", converter.convert(event));
        StatusPrinter.print(loggerContext);
    }

    @Test
    void dateConverterTest() {
        // 2024-08-14T1Z:29:25,956 GMT
        long millis = 1_723_649_365_956L;
        dateConverterChecker(millis, List.of("STRICT", "GMT"), "2024-08-14T15:29:25,956");
        dateConverterChecker(millis, List.of("ISO8601", "GMT"), "2024-08-14 15:29:25,956");
        dateConverterChecker(millis, List.of("ISO8601", "UTC"), "2024-08-14 15:29:25,956");
        dateConverterChecker(millis, List.of("yyyy-MM-EE", "UTC", "fr-CH"), "2024-08-mer.");
        dateConverterChecker(millis, List.of("EPOCH_SECONDS"), "1723649365");
        dateConverterChecker(millis, List.of("EPOCH_MILLIS"), "1723649365956");
    }

    void dateConverterChecker(long millis, List<String> options, String expected) {
        DateConverter dateConverter = new DateConverter();
        dateConverter.setOptionList(options) ;
        dateConverter.setContext(loggerContext);
        dateConverter.start();

        assertTrue(dateConverter.isStarted());
        LoggingEvent event = makeLoggingEvent(null);

        Instant now = Instant.ofEpochMilli(millis);
        event.setInstant(now);
        String result = dateConverter.convert(event);
        assertEquals(expected, result);
    }
}
