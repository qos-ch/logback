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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.JsonParser.JsonNumber;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyVO;
import ch.qos.logback.classic.spi.ThrowableProxyVOBuilder;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.helpers.BasicMarkerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static ch.qos.logback.classic.encoder.JsonEncoder.*;

/**
 * Reads the output of {@link JsonEncoder}, one JSON object per line, back into logging events.
 *
 * <p>Each member maps to the getter of the same name of the returned {@link JsonLoggingEvent}. A
 * member that is absent, for example because its {@code with*} flag was off when it was written,
 * gives {@code null}, or 0 for a number.</p>
 *
 * <ul>
 * <li>Markers are created by a {@link BasicMarkerFactory} private to this reader, never by the
 * global {@code MarkerFactory}. They have no references.</li>
 * <li>Arguments and key-value values come back as {@code null}, a {@link Boolean}, a
 * {@link String}, or a number: an {@link Integer} if it fits, else a {@link Long}, else a
 * {@link BigInteger} for an integral number, and a {@link Double} for any other.</li>
 * <li>Throwables are {@link ThrowableProxyVO} instances. The common frames that the encoder leaves
 * out are rebuilt from the frames of the enclosing throwable, so that
 * {@link IThrowableProxy#getStackTraceElementProxyArray()} returns every frame.</li>
 * <li>Members that this reader does not know, for example those written by
 * {@link JsonEncoder#appendCustomFields}, are passed to {@link #readUnknownMember}, which ignores
 * them by default.</li>
 * </ul>
 *
 * <p>The output of every logback version since 1.3.8 is accepted. In output written before
 * logback 1.6.8, a null value was written as the string {@code "null"}, which comes back as
 * that string, and arguments were written as strings.</p>
 *
 * <p>Instances are thread-safe, provided that subclasses keep them so.</p>
 *
 * @since 1.6.8
 */
public class JsonEventReader {

    private static final Set<String> KNOWN_MEMBERS = new HashSet<>(Arrays.asList(SEQUENCE_NUMBER_ATTR_NAME,
            TIMESTAMP_ATTR_NAME, NANOSECONDS_ATTR_NAME, LEVEL_ATTR_NAME, THREAD_NAME_ATTR_NAME, LOGGER_ATTR_NAME,
            CONTEXT_ATTR_NAME, MARKERS_ATTR_NAME, MDC_ATTR_NAME, KEY_VALUE_PAIRS_ATTR_NAME, MESSAGE_ATTR_NAME,
            FORMATTED_MESSAGE_ATTR_NAME, ARGUMENT_ARRAY_ATTR_NAME, CALLER_DATA_ATTR_NAME, THROWABLE_ATTR_NAME));

    private static final StackTraceElementProxy[] EMPTY_STEP_ARRAY = new StackTraceElementProxy[0];
    private static final IThrowableProxy[] EMPTY_ITP_ARRAY = new IThrowableProxy[0];

    private final IMarkerFactory markerFactory = new BasicMarkerFactory();

    /**
     * Read one line written by {@link JsonEncoder}.
     *
     * @param line one JSON object; surrounding whitespace, including the line separator, is ignored
     * @return the logging event
     * @throws IllegalArgumentException if the line is not valid JSON, is not a JSON object, or has a
     *                                  member of the wrong type
     */
    public JsonLoggingEvent read(String line) {
        Object parsed = JsonParser.parse(line);
        if (!(parsed instanceof Map)) {
            throw new IllegalArgumentException("Expected a JSON object");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) parsed;

        JsonLoggingEvent event = newEvent();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (KNOWN_MEMBERS.contains(name)) {
                readMember(event, name, value);
            } else {
                readUnknownMember(event, name, toJava(value));
            }
        }
        return event;
    }

    /**
     * Read every line of the given reader. Blank lines are skipped. The reader is not closed.
     *
     * @param reader the input
     * @return the logging events, in the order of the input
     * @throws IOException if reading fails
     * @throws IllegalArgumentException if a line cannot be read, see {@link #read(String)}; the message
     *                                  gives the line number
     */
    public List<ILoggingEvent> readAll(Reader reader) throws IOException {
        List<ILoggingEvent> events = new ArrayList<>();
        try {
            Iterator<ILoggingEvent> it = iterator(reader);
            while (it.hasNext()) {
                events.add(it.next());
            }
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        return events;
    }

    /**
     * Return an iterator over the events of the given reader, reading one line at a time, for inputs
     * too large to hold in memory. Blank lines are skipped. The reader is not closed.
     *
     * <p>{@link Iterator#hasNext()} and {@link Iterator#next()} throw an {@link UncheckedIOException}
     * if reading fails; {@code next()} throws an {@link IllegalArgumentException} giving the line
     * number if a line cannot be read.</p>
     *
     * @param reader the input
     * @return an iterator over the events
     */
    public Iterator<ILoggingEvent> iterator(Reader reader) {
        final BufferedReader bufferedReader = reader instanceof BufferedReader ? (BufferedReader) reader
                : new BufferedReader(reader);

        return new Iterator<ILoggingEvent>() {
            private String nextLine;
            private int lineNumber;
            private boolean exhausted;

            private void advance() {
                if (nextLine != null || exhausted) {
                    return;
                }
                try {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        lineNumber++;
                        if (!line.trim().isEmpty()) {
                            nextLine = line;
                            return;
                        }
                    }
                    exhausted = true;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public boolean hasNext() {
                advance();
                return nextLine != null;
            }

            @Override
            public ILoggingEvent next() {
                advance();
                if (nextLine == null) {
                    throw new NoSuchElementException();
                }
                String line = nextLine;
                nextLine = null;
                try {
                    return read(line);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Line " + lineNumber + ": " + e.getMessage(), e);
                }
            }
        };
    }

    /**
     * Create the event that {@link #read(String)} fills. Subclasses may return a subclass of
     * {@link JsonLoggingEvent}, for example to hold members written by
     * {@link JsonEncoder#appendCustomFields}.
     *
     * @return a new, empty event
     */
    protected JsonLoggingEvent newEvent() {
        return new JsonLoggingEvent();
    }

    /**
     * Called for each member of the root object that this reader does not know. Does nothing by
     * default.
     *
     * @param event the event being read
     * @param name the member name
     * @param value the member value: {@code null}, a {@link String}, a {@link Boolean}, a number as
     *              described in the class documentation, a {@code List} or a {@code Map} of such values
     */
    protected void readUnknownMember(JsonLoggingEvent event, String name, Object value) {
    }

    private void readMember(JsonLoggingEvent event, String name, Object value) {
        switch (name) {
        case SEQUENCE_NUMBER_ATTR_NAME:
            event.setSequenceNumber(toLong(name, value));
            break;
        case TIMESTAMP_ATTR_NAME:
            event.setTimeStamp(toLong(name, value));
            break;
        case NANOSECONDS_ATTR_NAME:
            event.setNanoseconds(toInt(name, value));
            break;
        case LEVEL_ATTR_NAME:
            String levelStr = asString(name, value);
            event.setLevel(levelStr == null ? null : Level.toLevel(levelStr, null));
            break;
        case THREAD_NAME_ATTR_NAME:
            event.setThreadName(asString(name, value));
            break;
        case LOGGER_ATTR_NAME:
            event.setLoggerName(asString(name, value));
            break;
        case CONTEXT_ATTR_NAME:
            event.setLoggerContextVO(readContext(value));
            break;
        case MARKERS_ATTR_NAME:
            event.setMarkerList(readMarkers(value));
            break;
        case MDC_ATTR_NAME:
            event.setMDCPropertyMap(readStringMap(name, value));
            break;
        case KEY_VALUE_PAIRS_ATTR_NAME:
            event.setKeyValuePairs(readKeyValuePairs(value));
            break;
        case MESSAGE_ATTR_NAME:
            event.setMessage(asString(name, value));
            break;
        case FORMATTED_MESSAGE_ATTR_NAME:
            event.setFormattedMessage(asString(name, value));
            break;
        case ARGUMENT_ARRAY_ATTR_NAME:
            event.setArgumentArray(readArguments(value));
            break;
        case CALLER_DATA_ATTR_NAME:
            event.setCallerData(readCallerData(value));
            break;
        case THROWABLE_ATTR_NAME:
            event.setThrowableProxy(readThrowable(value, null));
            break;
        default:
            throw new IllegalStateException("unhandled member " + name);
        }
    }

    private LoggerContextVO readContext(Object value) {
        if (value == null) {
            return null;
        }
        Map<String, Object> map = asObject(CONTEXT_ATTR_NAME, value);
        String name = asString(NAME_ATTR_NAME, map.get(NAME_ATTR_NAME));
        long birthTime = toLong(BIRTHDATE_ATTR_NAME, map.get(BIRTHDATE_ATTR_NAME));
        Map<String, String> properties = readStringMap(CONTEXT_PROPERTIES_ATTR_NAME,
                map.get(CONTEXT_PROPERTIES_ATTR_NAME));
        return new LoggerContextVO(name, properties, birthTime);
    }

    private List<Marker> readMarkers(Object value) {
        if (value == null) {
            return null;
        }
        List<Object> list = asArray(MARKERS_ATTR_NAME, value);
        List<Marker> markers = new ArrayList<>(list.size());
        for (Object o : list) {
            String markerName = asString(MARKERS_ATTR_NAME, o);
            markers.add(markerName == null ? null : markerFactory.getMarker(markerName));
        }
        return markers;
    }

    private Map<String, String> readStringMap(String memberName, Object value) {
        if (value == null) {
            return null;
        }
        Map<String, Object> map = asObject(memberName, value);
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result.put(entry.getKey(), asString(memberName, entry.getValue()));
        }
        return result;
    }

    private List<KeyValuePair> readKeyValuePairs(Object value) {
        if (value == null) {
            return null;
        }
        List<Object> list = asArray(KEY_VALUE_PAIRS_ATTR_NAME, value);
        List<KeyValuePair> pairs = new ArrayList<>(list.size());
        for (Object o : list) {
            if (o == null) {
                pairs.add(null);
                continue;
            }
            Map<String, Object> pairMap = asObject(KEY_VALUE_PAIRS_ATTR_NAME, o);
            for (Map.Entry<String, Object> entry : pairMap.entrySet()) {
                pairs.add(new KeyValuePair(entry.getKey(), toArgument(entry.getValue())));
            }
        }
        return pairs;
    }

    private Object[] readArguments(Object value) {
        if (value == null) {
            return null;
        }
        List<Object> list = asArray(ARGUMENT_ARRAY_ATTR_NAME, value);
        Object[] arguments = new Object[list.size()];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = toArgument(list.get(i));
        }
        return arguments;
    }

    private StackTraceElement[] readCallerData(Object value) {
        if (value == null) {
            return null;
        }
        List<Object> list = asArray(CALLER_DATA_ATTR_NAME, value);
        StackTraceElement[] callerData = new StackTraceElement[list.size()];
        for (int i = 0; i < callerData.length; i++) {
            callerData[i] = readStackTraceElement(CALLER_DATA_ATTR_NAME, list.get(i));
        }
        return callerData;
    }

    /**
     * @param enclosingFrames every frame of the enclosing throwable, or null for the root throwable
     */
    private IThrowableProxy readThrowable(Object value, StackTraceElementProxy[] enclosingFrames) {
        if (value == null) {
            return null;
        }
        Map<String, Object> map = asObject(THROWABLE_ATTR_NAME, value);
        ThrowableProxyVOBuilder builder = ThrowableProxyVO.builder();
        builder.setClassName(asString(CLASS_NAME_ATTR_NAME, map.get(CLASS_NAME_ATTR_NAME)));
        builder.setMessage(asString(MESSAGE_ATTR_NAME, map.get(MESSAGE_ATTR_NAME)));
        builder.setCyclic(isTrue(map.get(CYCLIC_THROWABLE_ATTR_NAME)));

        List<StackTraceElementProxy> frames = new ArrayList<>();
        Object stepArrayValue = map.get(STEP_ARRAY_NAME_ATTRIBUTE);
        if (stepArrayValue != null) {
            for (Object o : asArray(STEP_ARRAY_NAME_ATTRIBUTE, stepArrayValue)) {
                StackTraceElement ste = readStackTraceElement(STEP_ARRAY_NAME_ATTRIBUTE, o);
                frames.add(ste == null ? null : new StackTraceElementProxy(ste));
            }
        }

        int commonFrames = 0;
        if (map.containsKey(COMMON_FRAMES_COUNT_ATTR_NAME)) {
            commonFrames = toInt(COMMON_FRAMES_COUNT_ATTR_NAME, map.get(COMMON_FRAMES_COUNT_ATTR_NAME));
        }
        if (commonFrames > 0 && enclosingFrames != null) {
            int count = Math.min(commonFrames, enclosingFrames.length);
            frames.addAll(Arrays.asList(enclosingFrames).subList(enclosingFrames.length - count, enclosingFrames.length));
        }
        builder.setCommonFramesCount(commonFrames);

        StackTraceElementProxy[] allFrames = frames.toArray(EMPTY_STEP_ARRAY);
        builder.setStackTraceElementProxyArray(allFrames);

        builder.setCause(readThrowable(map.get(CAUSE_ATTR_NAME), allFrames));

        Object suppressedValue = map.get(SUPPRESSED_ATTR_NAME);
        if (suppressedValue == null) {
            builder.setSuppressed(EMPTY_ITP_ARRAY);
        } else {
            List<Object> list = asArray(SUPPRESSED_ATTR_NAME, suppressedValue);
            IThrowableProxy[] suppressed = new IThrowableProxy[list.size()];
            for (int i = 0; i < suppressed.length; i++) {
                suppressed[i] = readThrowable(list.get(i), allFrames);
            }
            builder.setSuppressed(suppressed);
        }
        return builder.build();
    }

    private StackTraceElement readStackTraceElement(String memberName, Object value) {
        if (value == null) {
            return null;
        }
        Map<String, Object> map = asObject(memberName, value);
        String className = asString(CLASS_NAME_ATTR_NAME, map.get(CLASS_NAME_ATTR_NAME));
        String methodName = asString(METHOD_NAME_ATTR_NAME, map.get(METHOD_NAME_ATTR_NAME));
        String fileName = asString(FILE_NAME_ATTR_NAME, map.get(FILE_NAME_ATTR_NAME));
        int lineNumber = map.containsKey(LINE_NUMBER_ATTR_NAME)
                ? toInt(LINE_NUMBER_ATTR_NAME, map.get(LINE_NUMBER_ATTR_NAME))
                : -1;
        // StackTraceElement rejects a null class or method name
        return new StackTraceElement(className == null ? "" : className, methodName == null ? "" : methodName,
                fileName, lineNumber);
    }

    // ---- conversions of parsed values ----

    private static boolean isTrue(Object value) {
        // logback versions before 1.6.8 wrote "cyclic":"true"
        return Boolean.TRUE.equals(value) || "true".equals(value);
    }

    /**
     * Convert an argument or key-value value.
     */
    static Object toArgument(Object value) {
        if (value instanceof JsonNumber) {
            return toNumber((JsonNumber) value);
        }
        if (value == null || value instanceof String || value instanceof Boolean) {
            return value;
        }
        return toJava(value);
    }

    static Number toNumber(JsonNumber number) {
        String text = number.text;
        if (number.isIntegral()) {
            BigInteger big = new BigInteger(text);
            if (big.bitLength() < 32) {
                return big.intValue();
            }
            if (big.bitLength() < 64) {
                return big.longValue();
            }
            return big;
        }
        return Double.valueOf(text);
    }

    /**
     * Convert a parsed value to plain Java values, numbers included.
     */
    static Object toJava(Object value) {
        if (value instanceof JsonNumber) {
            return toNumber((JsonNumber) value);
        }
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.put((String) entry.getKey(), toJava(entry.getValue()));
            }
            return result;
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<Object> result = new ArrayList<>(list.size());
            for (Object o : list) {
                result.add(toJava(o));
            }
            return result;
        }
        return value;
    }

    private static String asString(String memberName, Object value) {
        if (value == null || value instanceof String) {
            return (String) value;
        }
        if (value instanceof JsonNumber || value instanceof Boolean) {
            return value.toString();
        }
        throw wrongType(memberName, "a string", value);
    }

    private static long toLong(String memberName, Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof JsonNumber) {
            JsonNumber number = (JsonNumber) value;
            if (number.isIntegral()) {
                try {
                    return Long.parseLong(number.text);
                } catch (NumberFormatException e) {
                    // fall through
                }
            }
        }
        throw wrongType(memberName, "an integer", value);
    }

    private static int toInt(String memberName, Object value) {
        long l = toLong(memberName, value);
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw wrongType(memberName, "an int", value);
        }
        return (int) l;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asObject(String memberName, Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        throw wrongType(memberName, "an object", value);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asArray(String memberName, Object value) {
        if (value instanceof List) {
            return (List<Object>) value;
        }
        throw wrongType(memberName, "an array", value);
    }

    private static IllegalArgumentException wrongType(String memberName, String expected, Object value) {
        String found;
        if (value == null) {
            found = "null";
        } else if (value instanceof Map) {
            found = "an object";
        } else if (value instanceof List) {
            found = "an array";
        } else if (value instanceof JsonNumber) {
            found = "the number " + value;
        } else if (value instanceof Boolean) {
            found = value.toString();
        } else {
            found = "a string";
        }
        return new IllegalArgumentException("Member \"" + memberName + "\": expected " + expected + ", found " + found);
    }
}
