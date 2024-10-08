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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.EncoderBase;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.qos.logback.core.CoreConstants.COLON_CHAR;
import static ch.qos.logback.core.CoreConstants.COMMA_CHAR;
import static ch.qos.logback.core.CoreConstants.DOUBLE_QUOTE_CHAR;
import static ch.qos.logback.core.CoreConstants.UTF_8_CHARSET;
import static ch.qos.logback.core.encoder.JsonEscapeUtil.jsonEscapeString;
import static ch.qos.logback.core.model.ModelConstants.NULL_STR;

/**
 *
 *
 * https://jsonlines.org/ https://datatracker.ietf.org/doc/html/rfc8259
 */
public class JsonEncoder extends EncoderBase<ILoggingEvent> {
    static final boolean DO_NOT_ADD_QUOTE_KEY = false;
    static final boolean ADD_QUOTE_KEY = true;
    static int DEFAULT_SIZE = 1024;
    static int DEFAULT_SIZE_WITH_THROWABLE = DEFAULT_SIZE * 8;

    static byte[] EMPTY_BYTES = new byte[0];

    public static final String CONTEXT_ATTR_NAME = "context";
    public static final String NAME_ATTR_NAME = "name";
    public static final String BIRTHDATE_ATTR_NAME = "birthdate";
    public static final String CONTEXT_PROPERTIES_ATTR_NAME = "properties";

    public static final String TIMESTAMP_ATTR_NAME = "timestamp";

    public static final String NANOSECONDS_ATTR_NAME = "nanoseconds";

    public static final String SEQUENCE_NUMBER_ATTR_NAME = "sequenceNumber";

    public static final String LEVEL_ATTR_NAME = "level";
    public static final String MARKERS_ATTR_NAME = "markers";
    public static final String THREAD_NAME_ATTR_NAME = "threadName";
    public static final String MDC_ATTR_NAME = "mdc";
    public static final String LOGGER_ATTR_NAME = "loggerName";

    public static final String MESSAGE_ATTR_NAME = "message";

    public static final String FORMATTED_MESSAGE_ATTR_NAME = "formattedMessage";

    public static final String ARGUMENT_ARRAY_ATTR_NAME = "arguments";
    public static final String KEY_VALUE_PAIRS_ATTR_NAME = "kvpList";

    public static final String THROWABLE_ATTR_NAME = "throwable";

    public static final String CAUSE_ATTR_NAME = "cause";

    public static final String SUPPRESSED_ATTR_NAME = "suppressed";

    public static final String COMMON_FRAMES_COUNT_ATTR_NAME = "commonFramesCount";

    public static final String CLASS_NAME_ATTR_NAME = "className";
    public static final String METHOD_NAME_ATTR_NAME = "methodName";

    public static final String STEP_ARRAY_NAME_ATTRIBUTE = "stepArray";

    static final char OPEN_OBJ = '{';
    static final char CLOSE_OBJ = '}';
    static final char OPEN_ARRAY = '[';
    static final char CLOSE_ARRAY = ']';

    static final char QUOTE = DOUBLE_QUOTE_CHAR;
    private static final char SP = ' ';
    private static final char ENTRY_SEPARATOR = COLON_CHAR;

    private static final String COL_SP = ": ";

    static final String QUOTE_COL = "\":";

    static final char VALUE_SEPARATOR = COMMA_CHAR;

    private boolean withSequenceNumber = true;

    private boolean withTimestamp = true;
    private boolean withNanoseconds = true;


    private boolean withLevel = true;
    private boolean withThreadName = true;
    private boolean withLoggerName = true;
    private boolean withContext = true;
    private boolean withMarkers = true;
    private boolean withMDC = true;
    private boolean withKVPList = true;
    private boolean withMessage = true;
    private boolean withArguments = true;
    private boolean withThrowable = true;
    private boolean withPlainStackTrace = false;
    private boolean withFormattedMessage = false;


    @Override
    public byte[] headerBytes() {
        return EMPTY_BYTES;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        final int initialCapacity = event.getThrowableProxy() == null ? DEFAULT_SIZE : DEFAULT_SIZE_WITH_THROWABLE;
        StringBuilder sb = new StringBuilder(initialCapacity);
        sb.append(OPEN_OBJ);

        if (withSequenceNumber) {
            appenderMemberWithLongValue(sb, SEQUENCE_NUMBER_ATTR_NAME, event.getSequenceNumber());
            sb.append(VALUE_SEPARATOR);
        }

        if (withTimestamp) {
            appenderMemberWithLongValue(sb, TIMESTAMP_ATTR_NAME, event.getTimeStamp());
            sb.append(VALUE_SEPARATOR);
        }

        if (withNanoseconds) {
            appenderMemberWithLongValue(sb, NANOSECONDS_ATTR_NAME, event.getNanoseconds());
            sb.append(VALUE_SEPARATOR);
        }

        if (withLevel) {
            String levelStr = event.getLevel() != null ? event.getLevel().levelStr : NULL_STR;
            appenderMember(sb, LEVEL_ATTR_NAME, levelStr);
            sb.append(VALUE_SEPARATOR);
        }

        if (withThreadName) {
            appenderMember(sb, THREAD_NAME_ATTR_NAME, jsonEscape(event.getThreadName()));
            sb.append(VALUE_SEPARATOR);
        }

        if (withLoggerName) {
            appenderMember(sb, LOGGER_ATTR_NAME, event.getLoggerName());
            sb.append(VALUE_SEPARATOR);
        }

        if (withContext) {
            appendLoggerContext(sb, event.getLoggerContextVO());
            sb.append(VALUE_SEPARATOR);
        }
        if (withMarkers)
            appendMarkers(sb, event);

        if (withMDC)
            appendMDC(sb, event);

        if (withKVPList)
            appendKeyValuePairs(sb, event);

        if (withMessage) {
            appenderMember(sb, MESSAGE_ATTR_NAME, jsonEscape(event.getMessage()));
            sb.append(VALUE_SEPARATOR);
        }

        if (withFormattedMessage) {
            appenderMember(sb, FORMATTED_MESSAGE_ATTR_NAME, jsonEscape(event.getFormattedMessage()));
            sb.append(VALUE_SEPARATOR);
        }

        if (withArguments)
            appendArgumentArray(sb, event);

        if (withThrowable)
            ThrowableEncoder.appendThrowableProxy(sb, event.getThrowableProxy(), withPlainStackTrace);

        sb.append(CLOSE_OBJ);
        sb.append(CoreConstants.JSON_LINE_SEPARATOR);
        return sb.toString().getBytes(UTF_8_CHARSET);
    }

    private void appendLoggerContext(StringBuilder sb, LoggerContextVO loggerContextVO) {

        sb.append(QUOTE).append(CONTEXT_ATTR_NAME).append(QUOTE_COL);
        if (loggerContextVO == null) {
            sb.append(NULL_STR);
            return;
        }

        sb.append(OPEN_OBJ);
        appenderMember(sb, NAME_ATTR_NAME, nullSafeStr(loggerContextVO.getName()));
        sb.append(VALUE_SEPARATOR);
        appenderMemberWithLongValue(sb, BIRTHDATE_ATTR_NAME, loggerContextVO.getBirthTime());
        sb.append(VALUE_SEPARATOR);

        appendMap(sb, CONTEXT_PROPERTIES_ATTR_NAME, loggerContextVO.getPropertyMap());
        sb.append(CLOSE_OBJ);

    }

    private void appendMap(StringBuilder sb, String attrName, Map<String, String> map) {
        sb.append(QUOTE).append(attrName).append(QUOTE_COL);
        if (map == null) {
            sb.append(NULL_STR);
            return;
        }

        sb.append(OPEN_OBJ);

        boolean addComma = false;
        Set<Map.Entry<String, String>> entries = map.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            if (addComma) {
                sb.append(VALUE_SEPARATOR);
            }
            addComma = true;
            appenderMember(sb, jsonEscapedToString(entry.getKey()), jsonEscapedToString(entry.getValue()));
        }

        sb.append(CLOSE_OBJ);
    }

    static void appenderMember(StringBuilder sb, String key, String value) {
        sb.append(QUOTE).append(key).append(QUOTE_COL).append(QUOTE).append(value).append(QUOTE);
    }

    static void appenderMemberWithIntValue(StringBuilder sb, String key, int value) {
        sb.append(QUOTE).append(key).append(QUOTE_COL).append(value);
    }

    private void appenderMemberWithLongValue(StringBuilder sb, String key, long value) {
        sb.append(QUOTE).append(key).append(QUOTE_COL).append(value);
    }

    private void appendKeyValuePairs(StringBuilder sb, ILoggingEvent event) {
        List<KeyValuePair> kvpList = event.getKeyValuePairs();
        if (kvpList == null || kvpList.isEmpty())
            return;

        sb.append(QUOTE).append(KEY_VALUE_PAIRS_ATTR_NAME).append(QUOTE_COL).append(SP).append(OPEN_ARRAY);
        final int len = kvpList.size();
        for (int i = 0; i < len; i++) {
            if (i != 0)
                sb.append(VALUE_SEPARATOR);
            KeyValuePair kvp = kvpList.get(i);
            sb.append(OPEN_OBJ);
            appenderMember(sb, jsonEscapedToString(kvp.key), jsonEscapedToString(kvp.value));
            sb.append(CLOSE_OBJ);
        }
        sb.append(CLOSE_ARRAY);
        sb.append(VALUE_SEPARATOR);
    }

    private void appendArgumentArray(StringBuilder sb, ILoggingEvent event) {
        Object[] argumentArray = event.getArgumentArray();
        if (argumentArray == null)
            return;

        sb.append(QUOTE).append(ARGUMENT_ARRAY_ATTR_NAME).append(QUOTE_COL).append(SP).append(OPEN_ARRAY);
        final int len = argumentArray.length;
        for (int i = 0; i < len; i++) {
            if (i != 0)
                sb.append(VALUE_SEPARATOR);
            sb.append(QUOTE).append(jsonEscapedToString(argumentArray[i])).append(QUOTE);

        }
        sb.append(CLOSE_ARRAY);
        sb.append(VALUE_SEPARATOR);
    }

    private void appendMarkers(StringBuilder sb, ILoggingEvent event) {
        List<Marker> markerList = event.getMarkerList();
        if (markerList == null)
            return;

        sb.append(QUOTE).append(MARKERS_ATTR_NAME).append(QUOTE_COL).append(SP).append(OPEN_ARRAY);
        final int len = markerList.size();
        for (int i = 0; i < len; i++) {
            if (i != 0)
                sb.append(VALUE_SEPARATOR);
            sb.append(QUOTE).append(jsonEscapedToString(markerList.get(i))).append(QUOTE);

        }
        sb.append(CLOSE_ARRAY);
        sb.append(VALUE_SEPARATOR);

    }

    private String jsonEscapedToString(Object o) {
        if (o == null)
            return NULL_STR;
        return jsonEscapeString(o.toString());
    }

    static String nullSafeStr(String s) {
        if (s == null)
            return NULL_STR;
        return s;
    }

    static String jsonEscape(String s) {
        if (s == null)
            return NULL_STR;
        return jsonEscapeString(s);
    }

    private void appendMDC(StringBuilder sb, ILoggingEvent event) {
        Map<String, String> map = event.getMDCPropertyMap();

        sb.append(QUOTE).append(MDC_ATTR_NAME).append(QUOTE_COL).append(SP).append(OPEN_OBJ);
        if (isNotEmptyMap(map)) {
            Set<Map.Entry<String, String>> entrySet = map.entrySet();
            int i = 0;
            for (Map.Entry<String, String> entry : entrySet) {
                if (i != 0)
                    sb.append(VALUE_SEPARATOR);
                appenderMember(sb, jsonEscapedToString(entry.getKey()), jsonEscapedToString(entry.getValue()));
                i++;
            }

        }
        sb.append(CLOSE_OBJ);
        sb.append(VALUE_SEPARATOR);

    }

    boolean isNotEmptyMap(Map map) {
        if (map == null)
            return false;
        return !map.isEmpty();
    }

    @Override
    public byte[] footerBytes() {
        return EMPTY_BYTES;
    }

    /**
     * @param withSequenceNumber
     * @since 1.5.0
     */
    public void setWithSequenceNumber(boolean withSequenceNumber) {
        this.withSequenceNumber = withSequenceNumber;
    }

    /**
     * @param withTimestamp
     * @since 1.5.0
     */
    public void setWithTimestamp(boolean withTimestamp) {
        this.withTimestamp = withTimestamp;
    }

    /**
     * @param withNanoseconds
     * @since 1.5.0
     */
    public void setWithNanoseconds(boolean withNanoseconds) {
        this.withNanoseconds = withNanoseconds;
    }

    public void setWithLevel(boolean withLevel) {
        this.withLevel = withLevel;
    }

    public void setWithThreadName(boolean withThreadName) {
        this.withThreadName = withThreadName;
    }

    public void setWithLoggerName(boolean withLoggerName) {
        this.withLoggerName = withLoggerName;
    }

    public void setWithContext(boolean withContext) {
        this.withContext = withContext;
    }

    public void setWithMarkers(boolean withMarkers) {
        this.withMarkers = withMarkers;
    }

    public void setWithMDC(boolean withMDC) {
        this.withMDC = withMDC;
    }

    public void setWithKVPList(boolean withKVPList) {
        this.withKVPList = withKVPList;
    }

    public void setWithMessage(boolean withMessage) {
        this.withMessage = withMessage;
    }

    public void setWithArguments(boolean withArguments) {
        this.withArguments = withArguments;
    }

    public void setWithThrowable(boolean withThrowable) {
        this.withThrowable = withThrowable;
    }

    /**
     * @param withPlainStackTrace
     * @since 1.5.7
     */
    public void setWithPlainStackTrace(boolean withPlainStackTrace) {
        this.withPlainStackTrace = withPlainStackTrace;
    }

    public void setWithFormattedMessage(boolean withFormattedMessage) {
        this.withFormattedMessage = withFormattedMessage;
    }
}
