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


package ch.qos.logback.classic.encoder;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jsonTest.JsonStringToLoggingEventMapper;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.helpers.BasicMarkerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Round trips through {@link JsonEncoder} and {@link JsonEventReader}.
 */
class JsonEventReaderTest {

    static final String NASTY = "q\" b\\ s/ c\u0001\u001F\n\r\t\b\f é 日本 " + new String(Character.toChars(0x1F600))
            + " lone" + (char) 0xD800 + " low" + (char) 0xDC00 + " end" + (char) 0xDBFF;

    LoggerContext loggerContext = new LoggerContext();
    Logger logger;
    BasicMarkerFactory markerFactory = new BasicMarkerFactory();
    JsonEncoder encoder = new JsonEncoder();
    JsonEventReader reader = new JsonEventReader();
    JsonStringToLoggingEventMapper jacksonMapper = new JsonStringToLoggingEventMapper(markerFactory);

    @BeforeEach
    void setUp() {
        loggerContext.setName("ctx");
        loggerContext.setMDCAdapter(new LogbackMDCAdapter());
        loggerContext.putProperty("p", "v");
        logger = loggerContext.getLogger("com.example.Foo");
        encoder.setContext(loggerContext);
        encoder.setWithFormattedMessage(true);
        encoder.setWithCallerData(true);
        encoder.start();
    }

    // ---------------------------------------------------------------- round trips

    @Test
    void smoke() throws Exception {
        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, "hello {}", null, new Object[] { "world" });
        roundTrip(event);
    }

    @Test
    void escapedStrings() throws Exception {
        JsonLoggingEvent event = baseEvent();
        event.setLoggerName(NASTY);
        event.setThreadName(NASTY);
        event.setMessage(NASTY);
        Map<String, String> mdc = new LinkedHashMap<>();
        mdc.put(NASTY, NASTY);
        event.setMDCPropertyMap(mdc);
        Map<String, String> props = new LinkedHashMap<>();
        props.put(NASTY, NASTY);
        event.setLoggerContextVO(new LoggerContextVO(NASTY, props, 12));
        event.setKeyValuePairs(new ArrayList<>(List.of(new KeyValuePair(NASTY, NASTY))));
        event.setArgumentArray(new Object[] { NASTY });
        event.setMarkerList(new ArrayList<>(List.of(markerFactory.getMarker(NASTY))));

        String line = roundTrip(event);
        assertTrue(line.contains("\\uD800"), line);
        assertTrue(line.contains("\\uDC00"), line);
        assertTrue(line.contains("\\uDBFF"), line);
        assertFalse(line.contains("?"), line);
        assertTrue(line.contains("\\u0001"), line);
        assertTrue(line.contains("\\u001F"), line);
    }

    @Test
    void escapedStackTraceElements() throws Exception {
        Throwable t = new RuntimeException(NASTY);
        t.setStackTrace(new StackTraceElement[] { new StackTraceElement(NASTY, NASTY, NASTY, 3),
                new StackTraceElement("C", "m", null, -2) });
        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, "x", t, null);
        event.setCallerData(t.getStackTrace());
        roundTrip(event);
    }

    @Test
    void nullValues() throws Exception {
        JsonLoggingEvent event = baseEvent();
        event.setThreadName(null);
        event.setMessage(null);
        event.setLevel(null);
        event.setLoggerName(null);
        event.setLoggerContextVO(null);
        Map<String, String> mdc = new LinkedHashMap<>();
        mdc.put("m", null);
        event.setMDCPropertyMap(mdc);
        List<KeyValuePair> kvps = new ArrayList<>();
        kvps.add(new KeyValuePair("k", null));
        kvps.add(new KeyValuePair(null, "v"));
        event.setKeyValuePairs(kvps);
        event.setArgumentArray(new Object[] { null });

        String line = roundTrip(event, (e, r) -> {
            // a null key-value key is written as "null"
            assertEquals("null", r.getKeyValuePairs().get(1).key);
        });
        assertTrue(line.contains("\"threadName\":null"), line);
        assertTrue(line.contains("\"message\":null"), line);
        assertTrue(line.contains("\"level\":null"), line);
        assertTrue(line.contains("\"loggerName\":null"), line);
        assertTrue(line.contains("\"context\":null"), line);
        assertTrue(line.contains("{\"m\":null}"), line);
        assertTrue(line.contains("{\"k\":null}"), line);
        assertTrue(line.contains("\"arguments\": [null]"), line);
        assertTrue(line.contains("\"formattedMessage\":null"), line);
    }

    @Test
    void nullStrings() throws Exception {
        JsonLoggingEvent event = baseEvent();
        event.setThreadName("null");
        event.setMessage("null");
        Map<String, String> mdc = new LinkedHashMap<>();
        mdc.put("m", "null");
        event.setMDCPropertyMap(mdc);
        event.setKeyValuePairs(new ArrayList<>(List.of(new KeyValuePair("k", "null"))));
        event.setArgumentArray(new Object[] { "null" });

        String line = roundTrip(event);
        assertTrue(line.contains("\"threadName\":\"null\""), line);
        assertTrue(line.contains("\"message\":\"null\""), line);
        assertTrue(line.contains("{\"m\":\"null\"}"), line);
        assertTrue(line.contains("{\"k\":\"null\"}"), line);
        assertTrue(line.contains("\"arguments\": [\"null\"]"), line);
    }

    @Test
    void nullAndEmptyMDC() throws Exception {
        JsonLoggingEvent event = baseEvent();
        event.setMDCPropertyMap(null);
        String line = roundTrip(event);
        assertTrue(line.contains("\"mdc\": null"), line);

        event = baseEvent();
        event.setMDCPropertyMap(new HashMap<>());
        line = roundTrip(event);
        assertTrue(line.contains("\"mdc\": {}"), line);
    }

    @Test
    void nullAndEmptyLists() throws Exception {
        JsonLoggingEvent event = baseEvent();
        event.setMarkerList(null);
        event.setKeyValuePairs(null);
        event.setArgumentArray(null);
        event.setCallerData(null);
        String line = roundTrip(event);
        assertFalse(line.contains("\"markers\""), line);
        assertFalse(line.contains("\"kvpList\""), line);
        assertFalse(line.contains("\"arguments\""), line);
        assertFalse(line.contains("\"callerData\""), line);

        event = baseEvent();
        event.setMarkerList(new ArrayList<>());
        event.setKeyValuePairs(new ArrayList<>());
        event.setArgumentArray(new Object[0]);
        event.setCallerData(new StackTraceElement[0]);
        line = roundTrip(event);
        assertTrue(line.contains("\"markers\": []"), line);
        assertTrue(line.contains("\"kvpList\": []"), line);
        assertTrue(line.contains("\"arguments\": []"), line);
        assertTrue(line.contains("\"callerData\":[]"), line);
    }

    static final Object THROWING_TO_STRING = new Object() {
        @Override
        public String toString() {
            throw new IllegalStateException("boom");
        }
    };

    @Test
    void argumentTypes() throws Exception {
        Object[] arguments = { null, true, false, (byte) 1, (short) 2, 3, 4L, Long.MAX_VALUE,
                new BigInteger("123456789012345678901234567890"), 1.5f, 0.1f, 2.5d, 1e-7d, -0.0d,
                new BigDecimal("123.456"), new BigDecimal("1E+3"), Float.NaN, Float.POSITIVE_INFINITY,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, "str", 'c',
                new StringBuilder("sb"), THROWING_TO_STRING };
        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, "m", null, arguments);
        event.addKeyValuePair(new KeyValuePair("int", 3));
        event.addKeyValuePair(new KeyValuePair("bool", true));
        event.addKeyValuePair(new KeyValuePair("float", 0.1f));
        event.addKeyValuePair(new KeyValuePair("nan", Double.NaN));
        event.addKeyValuePair(new KeyValuePair("obj", new StringBuilder("sb")));

        String line = roundTrip(event);
        assertTrue(line.contains("\"arguments\": [null,true,false,1,2,3,4,9223372036854775807,"
                + "123456789012345678901234567890,1.5,0.1,2.5,1.0E-7,-0.0,123.456,1E+3,\"NaN\",\"Infinity\","
                + "\"NaN\",\"Infinity\",\"-Infinity\",\"str\",\"c\",\"sb\",\"[FAILED toString()]\"]"), line);
        assertTrue(line.contains("\"kvpList\": [{\"int\":3},{\"bool\":true},{\"float\":0.1},{\"nan\":\"NaN\"},{\"obj\":\"sb\"}]"), line);

        Object[] read = reader.read(line).getArgumentArray();
        assertEquals(Integer.valueOf(1), read[3]);
        assertEquals(Integer.valueOf(4), read[6]);
        assertEquals(Long.MAX_VALUE, read[7]);
        assertEquals(new BigInteger("123456789012345678901234567890"), read[8]);
        assertEquals(Double.valueOf(0.1), read[10]);
        assertEquals(Double.valueOf(1000), read[15]);
        assertEquals(Boolean.TRUE, read[1]);
    }

    @Test
    void markersWithReferences() throws Exception {
        Marker parent = markerFactory.getDetachedMarker("PARENT");
        Marker child = markerFactory.getDetachedMarker("CHILD");
        child.add(markerFactory.getDetachedMarker("GRANDCHILD"));
        parent.add(child);
        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, "m", null, null);
        event.addMarker(parent);
        event.addMarker(markerFactory.getDetachedMarker("PLAIN"));

        String line = roundTrip(event);
        assertTrue(line.contains("\"markers\": [\"PARENT\",\"PLAIN\"]"), line);
        List<Marker> markers = reader.read(line).getMarkerList();
        assertFalse(markers.get(0).hasReferences());
    }

    @Test
    void callerData() throws Exception {
        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, "m", null, null);
        event.setCallerData(new Throwable().getStackTrace());
        String line = roundTrip(event);
        assertTrue(line.contains("\"callerData\":[{\"className\":\"" + getClass().getName() + "\""), line);
    }

    @Test
    void callerDataIsNotComputed() {
        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, "m", null, null);
        String line = encode(encoder, event);
        assertFalse(line.contains("callerData"), line);
        assertFalse(event.hasCallerData());
    }

    @Test
    void callerDataOffByDefault() {
        JsonEncoder defaultEncoder = new JsonEncoder();
        defaultEncoder.setContext(loggerContext);
        defaultEncoder.start();
        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, "m", null, null);
        event.setCallerData(new Throwable().getStackTrace());
        assertFalse(encode(defaultEncoder, event).contains("callerData"));
    }

    @Test
    void noThrowable() throws Exception {
        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, "m", null, null);
        String line = roundTrip(event);
        assertTrue(line.contains("\"throwable\":null"), line);
    }

    @Test
    void throwableWithCauseAndSuppressed() throws Exception {
        Throwable t = makeThrowableWithCauseAndSuppressed();
        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.ERROR, "m", t, null);
        String line = roundTrip(event);
        assertTrue(line.contains("\"commonFramesCount\":"), line);

        IThrowableProxy read = reader.read(line).getThrowableProxy();
        assertTrue(read.getCause().getCommonFrames() > 0);
        assertTrue(read.getSuppressed()[0].getCommonFrames() > 0);
    }

    @Test
    void cyclicThrowable() throws Exception {
        Throwable cause = new IllegalStateException("c");
        Throwable t = new RuntimeException("t", cause);
        cause.initCause(t);
        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.ERROR, "m", t, null);
        String line = roundTrip(event);
        assertTrue(line.contains("\"cyclic\":true"), line);
        assertTrue(reader.read(line).getThrowableProxy().getCause().getCause().isCyclic());
    }

    // ---------------------------------------------------------------- common frames

    @Test
    void commonFramesRebuiltForCauseAndSuppressed() {
        Throwable t = makeThrowableWithCauseAndSuppressed();
        ThrowableProxy tp = new ThrowableProxy(t);
        assertTrue(tp.getCause().getCommonFrames() > 0);
        assertTrue(tp.getSuppressed()[0].getCommonFrames() > 0);

        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.ERROR, "m", t, null);
        IThrowableProxy read = reader.read(encode(encoder, event)).getThrowableProxy();

        assertFramesEqual(tp.getCause().getStackTraceElementProxyArray(),
                read.getCause().getStackTraceElementProxyArray());
        assertFramesEqual(tp.getSuppressed()[0].getStackTraceElementProxyArray(),
                read.getSuppressed()[0].getStackTraceElementProxyArray());
        assertEquals(tp.getCause().getCommonFrames(), read.getCause().getCommonFrames());
    }

    @Test
    void everyFrameCommon() throws Exception {
        Throwable t = new RuntimeException("t");
        StackTraceElement[] frames = t.getStackTrace();
        Throwable cause = new IllegalStateException("c");
        cause.setStackTrace(Arrays.copyOfRange(frames, 2, frames.length));
        Throwable suppressed = new IllegalArgumentException("s");
        suppressed.setStackTrace(Arrays.copyOfRange(frames, 1, frames.length));
        t.initCause(cause);
        t.addSuppressed(suppressed);

        ThrowableProxy tp = new ThrowableProxy(t);
        int causeFrames = frames.length - 2;
        assertEquals(causeFrames, tp.getCause().getCommonFrames());
        assertEquals(causeFrames, tp.getCause().getStackTraceElementProxyArray().length);

        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.ERROR, "m", t, null);
        String line = roundTrip(event);
        assertTrue(line.contains("\"message\":\"c\",\"stepArray\":[],\"commonFramesCount\":" + causeFrames), line);
        assertTrue(line.contains("\"message\":\"s\",\"stepArray\":[],\"commonFramesCount\":" + (frames.length - 1)),
                line);

        IThrowableProxy read = reader.read(line).getThrowableProxy();
        assertEquals(causeFrames, read.getCause().getStackTraceElementProxyArray().length);
        assertEquals(causeFrames, read.getCause().getCommonFrames());
    }

    // ---------------------------------------------------------------- valid JSON

    static final List<BiConsumer<JsonEncoder, Boolean>> FLAG_SETTERS = List.of(JsonEncoder::setWithSequenceNumber,
            JsonEncoder::setWithTimestamp, JsonEncoder::setWithNanoseconds, JsonEncoder::setWithLevel,
            JsonEncoder::setWithThreadName, JsonEncoder::setWithLoggerName, JsonEncoder::setWithContext,
            JsonEncoder::setWithMarkers, JsonEncoder::setWithMDC, JsonEncoder::setWithKVPList,
            JsonEncoder::setWithMessage, JsonEncoder::setWithFormattedMessage, JsonEncoder::setWithArguments,
            JsonEncoder::setWithCallerData, JsonEncoder::setWithThrowable);

    @Test
    void everyFlagCombinationGivesValidJson() throws Exception {
        Throwable t = new RuntimeException("t", new IllegalStateException("c"));
        t.setStackTrace(Arrays.copyOf(t.getStackTrace(), 3));
        t.getCause().setStackTrace(Arrays.copyOf(t.getCause().getStackTrace(), 4));
        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, "m {}", t, new Object[] { 1 });
        event.addMarker(markerFactory.getMarker("M"));
        event.addKeyValuePair(new KeyValuePair("k", "v"));
        event.setCallerData(Arrays.copyOf(new Throwable().getStackTrace(), 2));

        int flagCount = FLAG_SETTERS.size();
        for (int bits = 0; bits < (1 << flagCount); bits++) {
            JsonEncoder e = new JsonEncoder();
            e.setContext(loggerContext);
            for (int i = 0; i < flagCount; i++) {
                FLAG_SETTERS.get(i).accept(e, (bits & (1 << i)) != 0);
            }
            e.start();
            String line = encode(e, event);
            try {
                jacksonMapper.readTree(line);
                assertNotNull(JsonParser.parse(line));
                reader.read(line);
            } catch (Exception ex) {
                fail("flags " + Integer.toBinaryString(bits) + " gave invalid JSON: " + line, ex);
            }
            if (bits == 0) {
                assertEquals("{}\n", line);
            }
        }
    }

    @Test
    void customFields() throws Exception {
        JsonEncoder custom = new JsonEncoder() {
            @Override
            protected void appendCustomFields(StringBuilder sb, ILoggingEvent event) {
                appendMemberSeparator(sb);
                appenderMember(sb, "host", "h\"1");
                appendMemberSeparator(sb);
                appenderMemberWithLongValue(sb, "eventId", 42);
            }
        };
        custom.setContext(loggerContext);
        for (BiConsumer<JsonEncoder, Boolean> setter : FLAG_SETTERS) {
            setter.accept(custom, false);
        }
        custom.start();
        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, "m", null, null);
        assertEquals("{\"host\":\"h\\\"1\",\"eventId\":42}\n", encode(custom, event));

        custom.setWithMessage(true);
        String line = encode(custom, event);
        assertEquals("{\"message\":\"m\",\"host\":\"h\\\"1\",\"eventId\":42}\n", line);

        Map<String, Object> unknown = new LinkedHashMap<>();
        JsonEventReader customReader = new JsonEventReader() {
            @Override
            protected void readUnknownMember(JsonLoggingEvent event, String name, Object value) {
                unknown.put(name, value);
            }
        };
        assertEquals("m", customReader.read(line).getMessage());
        assertEquals("h\"1", unknown.get("host"));
        assertEquals(42, unknown.get("eventId"));
        // the default reader ignores unknown members
        assertEquals("m", reader.read(line).getMessage());
    }

    // ---------------------------------------------------------------- old output

    @Test
    void readLogback15Output() throws IOException {
        List<ILoggingEvent> events;
        try (Reader r = Files.newBufferedReader(Path.of(ClassicTestConstants.INPUT_PREFIX + "json/logback-1.5.38-output.jsonl"))) {
            events = reader.readAll(r);
        }
        assertEquals(3, events.size());

        ILoggingEvent e1 = events.get(0);
        assertEquals(Level.INFO, e1.getLevel());
        assertEquals("ctx", e1.getLoggerContextVO().getName());
        assertEquals("pv", e1.getLoggerContextVO().getPropertyMap().get("prop"));
        assertEquals("PARENT [ CHILD ]", e1.getMarkerList().get(0).getName());
        assertEquals("PLAIN", e1.getMarkerList().get(1).getName());
        assertEquals("null", e1.getMDCPropertyMap().get("m"));
        assertEquals("null", e1.getKeyValuePairs().get(0).value);
        assertEquals("7", e1.getKeyValuePairs().get(1).value);
        assertArrayEquals(new Object[] { "null", "42", "true" }, e1.getArgumentArray());
        assertEquals("hello null 42 true", e1.getFormattedMessage());
        assertNull(e1.getThrowableProxy());
        assertNull(e1.getCallerData());

        IThrowableProxy t2 = events.get(1).getThrowableProxy();
        assertEquals("java.lang.RuntimeException", t2.getClassName());
        IThrowableProxy cause = t2.getCause();
        assertEquals("cause", cause.getMessage());
        // common frames are rebuilt from the enclosing throwable
        StackTraceElementProxy[] enclosing = t2.getStackTraceElementProxyArray();
        StackTraceElementProxy[] causeFrames = cause.getStackTraceElementProxyArray();
        assertTrue(cause.getCommonFrames() > 0);
        assertEquals(enclosing[enclosing.length - 1], causeFrames[causeFrames.length - 1]);
        assertEquals("supp", t2.getSuppressed()[0].getMessage());

        IThrowableProxy t3 = events.get(2).getThrowableProxy();
        assertFalse(t3.isCyclic());
        assertTrue(t3.getCause().getCause().isCyclic());
    }

    // ---------------------------------------------------------------- streams

    @Test
    void readAllAndIterator() throws IOException {
        JsonLoggingEvent e1 = baseEvent();
        e1.setMessage("one");
        JsonLoggingEvent e2 = baseEvent();
        e2.setMessage("two");
        String input = encode(encoder, e1) + "\n  \n" + encode(encoder, e2);

        List<ILoggingEvent> all = reader.readAll(new StringReader(input));
        assertEquals(2, all.size());
        assertEquals("one", all.get(0).getMessage());
        assertEquals("two", all.get(1).getMessage());

        Iterator<ILoggingEvent> it = reader.iterator(new StringReader(input));
        assertTrue(it.hasNext());
        assertEquals("one", it.next().getMessage());
        assertEquals("two", it.next().getMessage());
        assertFalse(it.hasNext());
    }

    @Test
    void errorsGiveLineNumber() {
        String input = "{\"message\":\"ok\"}\n{\"message\":}\n";
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> reader.readAll(new StringReader(input)));
        assertTrue(e.getMessage().startsWith("Line 2: "), e.getMessage());
        assertTrue(e.getMessage().contains("offset 11"), e.getMessage());
    }

    @Test
    void notAnObject() {
        assertThrows(IllegalArgumentException.class, () -> reader.read("[1]"));
        assertThrows(IllegalArgumentException.class, () -> reader.read("{\"timestamp\":\"x\"}"));
    }

    @Test
    void markersComeFromPrivateFactory() {
        String name = "readerOnly-" + System.nanoTime();
        String line = "{\"markers\":[\"" + name + "\"]}";
        JsonLoggingEvent r = reader.read(line);
        assertEquals(name, r.getMarkerList().get(0).getName());
        assertSame(r.getMarkerList().get(0), reader.read(line).getMarkerList().get(0));
        assertFalse(org.slf4j.MarkerFactory.getIMarkerFactory().exists(name));
    }

    // ---------------------------------------------------------------- helpers

    private Throwable makeThrowableWithCauseAndSuppressed() {
        Throwable cause = new IllegalStateException("cause");
        Throwable t = new RuntimeException("top", cause);
        t.addSuppressed(new IllegalArgumentException("suppressed"));
        return t;
    }

    JsonLoggingEvent baseEvent() {
        JsonLoggingEvent e = new JsonLoggingEvent();
        e.setSequenceNumber(7);
        e.setTimeStamp(1_700_000_000_123L);
        e.setNanoseconds(123_456_789);
        e.setLevel(Level.WARN);
        e.setThreadName("main");
        e.setLoggerName("com.example.Foo");
        e.setLoggerContextVO(new LoggerContextVO(loggerContext));
        e.setMDCPropertyMap(new HashMap<>());
        e.setMessage("hello");
        return e;
    }

    static String encode(JsonEncoder encoder, ILoggingEvent event) {
        return new String(encoder.encode(event), StandardCharsets.UTF_8);
    }

    String roundTrip(ILoggingEvent event) throws JsonProcessingException {
        return roundTrip(event, (e, r) -> {
        });
    }

    String roundTrip(ILoggingEvent event, BiConsumer<ILoggingEvent, ILoggingEvent> extraChecks)
            throws JsonProcessingException {
        String line = encode(encoder, event);
        // independent check that the output is valid JSON
        jacksonMapper.readTree(line);
        JsonLoggingEvent read = reader.read(line);
        assertSameEvent(event, read);
        extraChecks.accept(event, read);
        return line;
    }

    static void assertSameEvent(ILoggingEvent e, ILoggingEvent r) {
        assertEquals(e.getSequenceNumber(), r.getSequenceNumber());
        assertEquals(e.getTimeStamp(), r.getTimeStamp());
        assertEquals(e.getNanoseconds(), r.getNanoseconds());
        assertEquals(e.getLevel(), r.getLevel());
        assertEquals(e.getThreadName(), r.getThreadName());
        assertEquals(e.getLoggerName(), r.getLoggerName());
        assertEquals(e.getMessage(), r.getMessage());
        assertEquals(e.getFormattedMessage(), r.getFormattedMessage());

        LoggerContextVO ec = e.getLoggerContextVO();
        LoggerContextVO rc = r.getLoggerContextVO();
        if (ec == null) {
            assertNull(rc);
        } else {
            assertEquals(ec.getName(), rc.getName());
            assertEquals(ec.getBirthTime(), rc.getBirthTime());
            assertEquals(ec.getPropertyMap(), rc.getPropertyMap());
        }

        assertEquals(e.getMDCPropertyMap(), r.getMDCPropertyMap());
        assertEquals(markerNames(e.getMarkerList()), markerNames(r.getMarkerList()));

        List<KeyValuePair> ek = e.getKeyValuePairs();
        List<KeyValuePair> rk = r.getKeyValuePairs();
        if (ek == null) {
            assertNull(rk);
        } else {
            assertEquals(ek.size(), rk.size());
            for (int i = 0; i < ek.size(); i++) {
                if (ek.get(i).key != null) {
                    assertEquals(ek.get(i).key, rk.get(i).key);
                }
                assertSameArgument(ek.get(i).value, rk.get(i).value);
            }
        }

        Object[] ea = e.getArgumentArray();
        Object[] ra = r.getArgumentArray();
        if (ea == null) {
            assertNull(ra);
        } else {
            assertEquals(ea.length, ra.length);
            for (int i = 0; i < ea.length; i++) {
                assertSameArgument(ea[i], ra[i]);
            }
        }

        if (e.hasCallerData()) {
            assertSteArrayEquals(e.getCallerData(), r.getCallerData());
        } else {
            assertNull(r.getCallerData());
        }

        assertSameThrowable(e.getThrowableProxy(), r.getThrowableProxy());
    }

    static List<String> markerNames(List<Marker> markers) {
        if (markers == null)
            return null;
        List<String> names = new ArrayList<>();
        for (Marker m : markers) {
            names.add(m == null ? null : m.getName());
        }
        return names;
    }

    static void assertSameArgument(Object original, Object read) {
        if (original == null) {
            assertNull(read);
            return;
        }
        Class<?> c = original.getClass();
        if (c == Boolean.class) {
            assertEquals(original, read);
        } else if ((c == Float.class && !Float.isFinite((Float) original))
                || (c == Double.class && !Double.isFinite((Double) original))) {
            assertEquals(original.toString(), read);
        } else if (c == Byte.class || c == Short.class || c == Integer.class || c == Long.class
                || c == BigInteger.class || c == BigDecimal.class || c == Float.class || c == Double.class) {
            assertTrue(read instanceof Number, "expected a number, got " + read);
            assertEquals(0, new BigDecimal(original.toString()).compareTo(new BigDecimal(read.toString())),
                    original + " vs " + read);
        } else {
            String expected;
            try {
                expected = original.toString();
            } catch (RuntimeException ex) {
                expected = "[FAILED toString()]";
            }
            assertEquals(expected, read);
        }
    }

    static void assertSameThrowable(IThrowableProxy e, IThrowableProxy r) {
        if (e == null) {
            assertNull(r);
            return;
        }
        assertNotNull(r);
        assertEquals(e.getClassName(), r.getClassName());
        assertEquals(e.getMessage(), r.getMessage());
        assertEquals(e.isCyclic(), r.isCyclic());
        assertFramesEqual(e.getStackTraceElementProxyArray(), r.getStackTraceElementProxyArray());
        assertEquals(e.getCommonFrames(), r.getCommonFrames());
        assertSameThrowable(e.getCause(), r.getCause());
        IThrowableProxy[] es = e.getSuppressed() == null ? new IThrowableProxy[0] : e.getSuppressed();
        IThrowableProxy[] rs = r.getSuppressed() == null ? new IThrowableProxy[0] : r.getSuppressed();
        assertEquals(es.length, rs.length);
        for (int i = 0; i < es.length; i++) {
            assertSameThrowable(es[i], rs[i]);
        }
    }

    static void assertFramesEqual(StackTraceElementProxy[] e, StackTraceElementProxy[] r) {
        StackTraceElement[] es = new StackTraceElement[e.length];
        for (int i = 0; i < e.length; i++)
            es[i] = e[i].getStackTraceElement();
        StackTraceElement[] rs = new StackTraceElement[r.length];
        for (int i = 0; i < r.length; i++)
            rs[i] = r[i].getStackTraceElement();
        assertSteArrayEquals(es, rs);
    }

    /**
     * Compare class name, method name, file name and line number; {@link StackTraceElement#equals} also
     * compares the class loader and module, which are not written.
     */
    static void assertSteArrayEquals(StackTraceElement[] e, StackTraceElement[] r) {
        assertNotNull(r);
        assertEquals(e.length, r.length);
        for (int i = 0; i < e.length; i++) {
            assertEquals(e[i].getClassName(), r[i].getClassName(), "frame " + i);
            assertEquals(e[i].getMethodName(), r[i].getMethodName(), "frame " + i);
            assertEquals(e[i].getFileName(), r[i].getFileName(), "frame " + i);
            assertEquals(e[i].getLineNumber(), r[i].getLineNumber(), "frame " + i);
        }
    }
}
