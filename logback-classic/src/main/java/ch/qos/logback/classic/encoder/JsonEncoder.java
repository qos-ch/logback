/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2023, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.encoder;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.util.List;
import java.util.Map;

import static ch.qos.logback.core.CoreConstants.COLON_CHAR;
import static ch.qos.logback.core.CoreConstants.COMMA_CHAR;
import static ch.qos.logback.core.CoreConstants.DOUBLE_QUOTE_CHAR;
import static ch.qos.logback.core.CoreConstants.UTF_8_CHARSET;
import static ch.qos.logback.core.encoder.JsonEscapeUtil.jsonEscapeString;
import static ch.qos.logback.core.model.ModelConstants.NULL_STR;

/**
 *
 *
 */
public class JsonEncoder extends EncoderBase<ILoggingEvent> {



    static int DEFAULT_SIZE = 1024;
    static int DEFAULT_SIZE_WITH_THROWABLE = DEFAULT_SIZE*8;

    static byte[] EMPTY_BYTES = new byte[0];


    public static final String CONTEXT_ATTR_NAME = "context";
    public static final String TIMESTAMP_ATTR_NAME = "timestamp";

    public static final String NANOSECONDS_ATTR_NAME = "nanoseconds";

    public static final String SEQUENCE_NUMBER_ATTR_NAME = "sequenceNumbers";


    public static final String LEVEL_ATTR_NAME = "level";
    public static final String MARKERS_ATTR_NAME = "markers";
    public static final String THREAD_ATTR_NAME = "thread";
    public static final String MDC_ATTR_NAME = "mdc";
    public static final String LOGGER_ATTR_NAME = "logger";
    public static final String MESSAGE_ATTR_NAME = "rawMessage";

    public static final String ARGUMENT_ARRAY_ATTR_NAME = "arguments";
    public static final String KEY_VALUE_PAIRS_ATTR_NAME = "keyValuePairs";

    public static final String THROWABLE_ATTR_NAME = "throwable";

    private static final char OPEN_OBJ = '{';
    private static final char CLOSE_OBJ = '}';
    private static final char OPEN_ARRAY = '[';
    private static final char CLOSE_ARRAY = ']';

    private static final char QUOTE = DOUBLE_QUOTE_CHAR;
    private static final char SP = ' ';
    private static final char ENTRY_SEPARATOR = COLON_CHAR;

    private static final char COL_SP = COLON_CHAR+SP;

    private static final char VALUE_SEPARATOR = COMMA_CHAR;



    @Override
    public byte[] headerBytes() {
        return EMPTY_BYTES;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        final int initialCapacity = event.getThrowableProxy() == null ? DEFAULT_SIZE: DEFAULT_SIZE_WITH_THROWABLE;
        StringBuilder sb = new StringBuilder(initialCapacity);
        sb.append(OPEN_OBJ);


        sb.append(SEQUENCE_NUMBER_ATTR_NAME).append(COL_SP).append(event.getSequenceNumber());
        sb.append(VALUE_SEPARATOR);


        sb.append(TIMESTAMP_ATTR_NAME).append(COL_SP).append(event.getTimeStamp());
        sb.append(VALUE_SEPARATOR);

        sb.append(NANOSECONDS_ATTR_NAME).append(COL_SP).append(event.getNanoseconds());
        sb.append(VALUE_SEPARATOR);


        String levelStr = event.getLevel() != null ? event.getLevel().levelStr : NULL_STR;
        sb.append(LEVEL_ATTR_NAME).append(COL_SP).append(QUOTE).append(levelStr).append(QUOTE);
        sb.append(VALUE_SEPARATOR);

        sb.append(THREAD_ATTR_NAME).append(COL_SP).append(QUOTE).append(jsonSafeStr(event.getThreadName())).append(QUOTE);
        sb.append(VALUE_SEPARATOR);

        sb.append(LOGGER_ATTR_NAME).append(COL_SP).append(QUOTE).append(event.getLoggerName()).append(QUOTE);
        sb.append(VALUE_SEPARATOR);

        appendMarkers(sb, event);
        appendMDC(sb, event);
        appendKeyValuePairs(sb, event);

        sb.append(MESSAGE_ATTR_NAME).append(COL_SP).append(QUOTE).append(jsonSafeStr(event.getMessage())).append(QUOTE);
        sb.append(VALUE_SEPARATOR);

        appendArgumentArray(sb, event);

        sb.append(CLOSE_OBJ);
        return sb.toString().getBytes(UTF_8_CHARSET);
    }

    private void appendKeyValuePairs(StringBuilder sb, ILoggingEvent event) {
        List<KeyValuePair> kvpList = event.getKeyValuePairs();
        if(kvpList == null || kvpList.isEmpty())
            return;

        sb.append(KEY_VALUE_PAIRS_ATTR_NAME).append(ENTRY_SEPARATOR).append(SP).append(OPEN_ARRAY);
        final int len = kvpList.size();
        for(int i = 0; i < len; i++) {
            KeyValuePair kvp = kvpList.get(i);
            sb.append(QUOTE).append(jsonSafeToString(kvp.key)).append(QUOTE);
            sb.append(COL_SP);
            sb.append(QUOTE).append(jsonSafeToString(kvp.value)).append(QUOTE);

            if(i != len)
                sb.append(VALUE_SEPARATOR);
        }
        sb.append(CLOSE_ARRAY);
    }

    private void appendArgumentArray(StringBuilder sb, ILoggingEvent event) {
        Object[] argumentArray = event.getArgumentArray();
        if(argumentArray == null)
            return;

        sb.append(ARGUMENT_ARRAY_ATTR_NAME).append(ENTRY_SEPARATOR).append(SP).append(OPEN_ARRAY);
        final int len = argumentArray.length;
        for(int i = 0; i < len; i++) {
            sb.append(QUOTE).append(jsonSafeToString(argumentArray[i])).append(QUOTE);
            if(i != len)
                sb.append(VALUE_SEPARATOR);
        }
        sb.append(CLOSE_ARRAY);
    }

    private void appendMarkers(StringBuilder sb, ILoggingEvent event) {
        List<Marker> markerList = event.getMarkerList();
        if(markerList == null)
            return;

        sb.append(MARKERS_ATTR_NAME).append(ENTRY_SEPARATOR).append(SP).append(OPEN_ARRAY);
        final int len = markerList.size();
        for(int i = 0; i < len; i++) {
            sb.append(QUOTE).append(jsonSafeToString(markerList.get(i))).append(QUOTE);
            if(i != len)
                sb.append(VALUE_SEPARATOR);
        }
        sb.append(CLOSE_ARRAY);
    }

    private String jsonSafeToString(Object o) {
        if(o == null)
            return NULL_STR;
        return jsonEscapeString(o.toString());
    }

    private String jsonSafeStr(String s) {
        if(s == null)
            return NULL_STR;
        return jsonEscapeString(s);
    }


    private void appendMDC(StringBuilder sb, ILoggingEvent event) {
        Map<String, String> map = event.getMDCPropertyMap();

        sb.append(MDC_ATTR_NAME).append(ENTRY_SEPARATOR).append(SP).append(OPEN_OBJ);
        if(isNotEmptyMap(map)) {
            map.entrySet().stream().forEach(e -> appendMapEntry(sb, e));
        }
        sb.append(CLOSE_OBJ);

    }

    private void appendMapEntry(StringBuilder sb, Map.Entry<String, String> entry) {
        if(entry == null)
            return;

        sb.append(QUOTE).append(jsonSafeToString(entry.getKey())).append(QUOTE).append(COL_SP).append(QUOTE)
                .append(jsonSafeToString(entry.getValue())).append(QUOTE);
    }

    boolean isNotEmptyMap(Map map) {
       if(map == null)
           return false;
       return !map.isEmpty();
    }

    @Override
    public byte[] footerBytes() {
        return EMPTY_BYTES;
    }
}
