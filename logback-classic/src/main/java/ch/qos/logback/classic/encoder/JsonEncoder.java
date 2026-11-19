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
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.EncoderBase;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static ch.qos.logback.core.CoreConstants.COLON_CHAR;
import static ch.qos.logback.core.CoreConstants.COMMA_CHAR;
import static ch.qos.logback.core.CoreConstants.DOUBLE_QUOTE_CHAR;
import static ch.qos.logback.core.CoreConstants.UTF_8_CHARSET;
import static ch.qos.logback.core.encoder.JsonEscapeUtil.appendJsonEscapedString;

/**
 * JSON encoder that produces one JSON object per line in JSON Lines format, suitable for structured logging.
 * Each {@link ILoggingEvent} is encoded into a JSON object containing fields such as timestamp, level, message,
 * and optional elements like MDC properties, markers, and stack traces.
 *
 * <p>This encoder supports extensive configuration through boolean flags to include or exclude specific fields
 * in the output, allowing customization for different logging needs. For example, you can enable/disable
 * sequence numbers, nanoseconds, thread names, logger context, markers, MDC, key-value pairs, arguments,
 * caller data and throwable information.</p>
 *
 * <p>Every combination of flags gives a valid JSON object; with every flag off, the output is {@code {}}.
 * A null value is written as a bare JSON {@code null}, never as the string {@code "null"}. Arguments and
 * key-value values that are numbers or booleans are written as JSON numbers and booleans; any other value
 * is written as the string returned by its {@code toString()} method.</p>
 *
 * <p>The encoder is designed for extensibility: subclasses can override protected methods (e.g.,
 * {@link #appendLoggerContext}, {@link #appendThrowableProxy}, {@link #appendMarkers}) to customize
 * how specific parts of the JSON are generated. Additionally, the {@link #appendCustomFields} hook
 * allows appending custom top-level fields to the JSON object.</p>
 *
 * <p>{@link JsonEventReader} reads the output of this encoder back into logging events.</p>
 *
 * <h3>Configuration</h3>
 * <p>Use the setter methods (e.g., {@link #setWithSequenceNumber}, {@link #setWithTimestamp}) to control
 * which fields are included. By default, most fields are enabled except {@code withFormattedMessage} and
 * {@code withCallerData}.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
 *   <encoder class="ch.qos.logback.classic.encoder.JsonEncoder">
 *     <withSequenceNumber>false</withSequenceNumber>
 *     <withNanoseconds>false</withNanoseconds>
 *     <withThreadName>false</withThreadName>
 *   </encoder>
 * </appender>
 * }</pre>
 *
 * <p>This produces output similar to the following (on a single line):
 * </p>
 *
 * <pre>{"timestamp":1640995200000,"level":"INFO","loggerName":"com.example.MyClass","context":{"name":"default","birthdate":1640995200000,"properties":{}},"message":"Hello World"}</pre>
 *
 * @see <a href="https://jsonlines.org/">JSON Lines</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc8259">RFC 8259 (The JavaScript Object Notation (JSON) Data Interchange Format)</a>
 * @see ch.qos.logback.core.encoder.EncoderBase
 * @since 1.3.8/1.4.8
 * @author Ceki G&uuml;lc&uuml;
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

    /**
     * @since 1.6.8
     */
    public static final String CALLER_DATA_ATTR_NAME = "callerData";

    public static final String THROWABLE_ATTR_NAME = "throwable";

    /**
     * Public since 1.6.8.
     */
    public static final String CYCLIC_THROWABLE_ATTR_NAME = "cyclic";

    public static final String CAUSE_ATTR_NAME = "cause";

    public static final String SUPPRESSED_ATTR_NAME = "suppressed";

    public static final String COMMON_FRAMES_COUNT_ATTR_NAME = "commonFramesCount";

    public static final String CLASS_NAME_ATTR_NAME = "className";
    public static final String METHOD_NAME_ATTR_NAME = "methodName";
    /**
     * Public since 1.6.8.
     */
    public static final String FILE_NAME_ATTR_NAME = "fileName";
    /**
     * Public since 1.6.8.
     */
    public static final String LINE_NUMBER_ATTR_NAME = "lineNumber";

    public static final String STEP_ARRAY_NAME_ATTRIBUTE = "stepArray";

    protected static final char OPEN_OBJ = '{';
    protected static final char CLOSE_OBJ = '}';
    protected static final char OPEN_ARRAY = '[';
    protected static final char CLOSE_ARRAY = ']';

    protected static final char QUOTE = DOUBLE_QUOTE_CHAR;
    protected static final char SP = ' ';
    protected static final char ENTRY_SEPARATOR = COLON_CHAR;

    protected static final String COL_SP = ": ";

    protected static final String QUOTE_COL = "\":";

    protected static final char VALUE_SEPARATOR = COMMA_CHAR;

    /**
     * The JSON null literal.
     *
     * @since 1.6.8
     */
    protected static final String JSON_NULL = "null";

    /**
     * Written in place of a value whose {@code toString()} method throws, as SLF4J's
     * {@code MessageFormatter} does.
     */
    static final String FAILED_TO_STRING = "[FAILED toString()]";

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
    private boolean withFormattedMessage = false;
    private boolean withCallerData = false;

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
            appendMemberSeparator(sb);
            appenderMemberWithLongValue(sb, SEQUENCE_NUMBER_ATTR_NAME, event.getSequenceNumber());
        }

        if (withTimestamp) {
            appendMemberSeparator(sb);
            appenderMemberWithLongValue(sb, TIMESTAMP_ATTR_NAME, event.getTimeStamp());
        }

        if (withNanoseconds) {
            appendMemberSeparator(sb);
            appenderMemberWithLongValue(sb, NANOSECONDS_ATTR_NAME, event.getNanoseconds());
        }

        if (withLevel) {
            appendMemberSeparator(sb);
            Level level = event.getLevel();
            appenderMember(sb, LEVEL_ATTR_NAME, level != null ? level.levelStr : null);
        }

        if (withThreadName) {
            appendMemberSeparator(sb);
            appenderMember(sb, THREAD_NAME_ATTR_NAME, event.getThreadName());
        }

        if (withLoggerName) {
            appendMemberSeparator(sb);
            appenderMember(sb, LOGGER_ATTR_NAME, event.getLoggerName());
        }

        if (withContext) {
            appendMemberSeparator(sb);
            appendLoggerContext(sb, event.getLoggerContextVO());
        }

        // the following methods write their own member separator
        if (withMarkers)
            appendMarkers(sb, event);

        if (withMDC)
            appendMDC(sb, event);

        if (withKVPList)
            appendKeyValuePairs(sb, event);

        if (withMessage) {
            appendMemberSeparator(sb);
            appenderMember(sb, MESSAGE_ATTR_NAME, event.getMessage());
        }

        if (withFormattedMessage) {
            appendMemberSeparator(sb);
            appenderMember(sb, FORMATTED_MESSAGE_ATTR_NAME, event.getFormattedMessage());
        }

        if (withArguments)
            appendArgumentArray(sb, event);

        if (withCallerData)
            appendCallerData(sb, event);

        if (withThrowable)
            appendThrowableProxy(sb, THROWABLE_ATTR_NAME, event.getThrowableProxy());

        // allow subclasses to append custom top-level fields; default implementation is a no-op
        appendCustomFields(sb, event);

        sb.append(CLOSE_OBJ);
        sb.append(CoreConstants.JSON_LINE_SEPARATOR);
        return sb.toString().getBytes(UTF_8_CHARSET);
    }

    /**
     * Append a member separator (a comma) if a member or an element was written before in the
     * object or array being written.
     *
     * <p>Whether a member came before is read from the end of the builder: no comma is written
     * right after the opening brace of an object, the opening bracket of an array, or a comma.
     * Every method of this class that writes a member, and {@link #appendCustomFields} in
     * subclasses, call this method before writing it, so that every combination of members gives
     * valid JSON.</p>
     *
     * @param sb the builder holding the JSON written so far; must not be {@code null}
     * @since 1.6.8
     */
    protected void appendMemberSeparator(StringBuilder sb) {
        int i = sb.length() - 1;
        while (i >= 0 && sb.charAt(i) == SP) {
            i--;
        }
        if (i < 0) {
            return;
        }
        char last = sb.charAt(i);
        if (last == OPEN_OBJ || last == OPEN_ARRAY || last == VALUE_SEPARATOR || last == ENTRY_SEPARATOR) {
            return;
        }
        sb.append(VALUE_SEPARATOR);
    }

    /**
     * Append a JSON value separator (a comma) to the provided {@link StringBuilder}
     * when any of the supplied boolean flags indicate that a subsequent element
     * is present.
     *
     * <p>Callers pass a sequence of booleans that represent whether subsequent
     * JSON members will be written. If at least one of those booleans is
     * {@code true}, this method appends a single comma (',') to separate JSON
     * fields.</p>
     *
     * @param sb the {@link StringBuilder} to append the separator to; must not be {@code null}
     * @param subsequentConditionals one or more booleans indicating whether
     *                               subsequent JSON elements will be written.
     *                               If any value is {@code true}, a comma is appended.
     * @deprecated since 1.6.8, no longer used by this class; use {@link #appendMemberSeparator(StringBuilder)},
     * which knows whether a member was written before.
     */
    @Deprecated
    protected void appendValueSeparator(StringBuilder sb, boolean... subsequentConditionals) {
        boolean enabled = false;
        for (boolean subsequent : subsequentConditionals) {
            if (subsequent) {
                enabled = true;
                break;
            }
        }

        if (enabled)
            sb.append(VALUE_SEPARATOR);
    }

    protected void appendLoggerContext(StringBuilder sb, LoggerContextVO loggerContextVO) {

        appendMemberName(sb, CONTEXT_ATTR_NAME);
        if (loggerContextVO == null) {
            sb.append(JSON_NULL);
            return;
        }

        sb.append(OPEN_OBJ);
        appenderMember(sb, NAME_ATTR_NAME, loggerContextVO.getName());
        sb.append(VALUE_SEPARATOR);
        appenderMemberWithLongValue(sb, BIRTHDATE_ATTR_NAME, loggerContextVO.getBirthTime());
        sb.append(VALUE_SEPARATOR);

        appendMap(sb, CONTEXT_PROPERTIES_ATTR_NAME, loggerContextVO.getPropertyMap());
        sb.append(CLOSE_OBJ);

    }

    protected void appendMap(StringBuilder sb, String attrName, Map<String, String> map) {
        appendMemberName(sb, attrName);
        appendMapValue(sb, map);
    }

    private void appendMapValue(StringBuilder sb, Map<String, String> map) {
        if (map == null) {
            sb.append(JSON_NULL);
            return;
        }

        sb.append(OPEN_OBJ);
        boolean addComma = false;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (addComma) {
                sb.append(VALUE_SEPARATOR);
            }
            addComma = true;
            appenderMember(sb, entry.getKey(), entry.getValue());
        }
        sb.append(CLOSE_OBJ);
    }

    protected void appendThrowableProxy(StringBuilder sb, String attributeName, IThrowableProxy itp) {
        appendThrowableProxy(sb, attributeName, itp, true);
    }

    /**
     * Write a throwable proxy, recursively with its cause and suppressed throwables.
     *
     * <p>The last {@code commonFramesCount} frames, the ones shared with the enclosing throwable, are
     * left out of {@code stepArray}, even when every frame is shared; a reader rebuilds them from the
     * frames of the enclosing throwable.</p>
     *
     * @param sb the builder to write to
     * @param attributeName the member name, or {@code null} for an element of the suppressed array
     * @param itp the throwable proxy, may be {@code null}
     * @param appendValueSeparator whether to write a member separator first, if one is needed
     */
    protected void appendThrowableProxy(StringBuilder sb, String attributeName, IThrowableProxy itp, boolean appendValueSeparator) {

        if (appendValueSeparator)
            appendMemberSeparator(sb);

        // in the nominal case, attributeName != null. However, attributeName will be null for suppressed
        // IThrowableProxy array, in which case no attribute name is needed
        if (attributeName != null) {
            appendMemberName(sb, attributeName);
        }
        if (itp == null) {
            sb.append(JSON_NULL);
            return;
        }

        sb.append(OPEN_OBJ);

        appenderMember(sb, CLASS_NAME_ATTR_NAME, itp.getClassName());

        sb.append(VALUE_SEPARATOR);
        appenderMember(sb, MESSAGE_ATTR_NAME, itp.getMessage());

        if (itp.isCyclic()) {
            sb.append(VALUE_SEPARATOR);
            appendMemberName(sb, CYCLIC_THROWABLE_ATTR_NAME);
            sb.append(true);
        }

        StackTraceElementProxy[] stepArray = itp.getStackTraceElementProxyArray();
        int omittedFrames = omittedFrameCount(stepArray, itp.getCommonFrames());

        sb.append(VALUE_SEPARATOR);
        appendSTEPArray(sb, stepArray, omittedFrames);

        if (omittedFrames != 0) {
            sb.append(VALUE_SEPARATOR);
            appenderMemberWithIntValue(sb, COMMON_FRAMES_COUNT_ATTR_NAME, omittedFrames);
        }

        IThrowableProxy cause = itp.getCause();
        if (cause != null) {
            appendThrowableProxy(sb, CAUSE_ATTR_NAME, cause);
        }

        IThrowableProxy[] suppressedArray = itp.getSuppressed();
        if (suppressedArray != null && suppressedArray.length != 0) {
            sb.append(VALUE_SEPARATOR);
            appendMemberName(sb, SUPPRESSED_ATTR_NAME);
            sb.append(OPEN_ARRAY);

            boolean first = true;
            for (IThrowableProxy suppressedITP : suppressedArray) {
                appendThrowableProxy(sb, null, suppressedITP, !first);
                first = false;
            }
            sb.append(CLOSE_ARRAY);
        }

        sb.append(CLOSE_OBJ);

    }

    /**
     * The number of trailing frames left out of {@code stepArray}: {@code commonFrames}, or 0 when it
     * is out of range.
     */
    static int omittedFrameCount(StackTraceElementProxy[] stepArray, int commonFrames) {
        int len = stepArray == null ? 0 : stepArray.length;
        if (commonFrames < 0 || commonFrames > len) {
            return 0;
        }
        return commonFrames;
    }

    /**
     * Write the {@code stepArray} member, leaving out the last {@code commonFrames} frames.
     *
     * @param sb the builder to write to
     * @param stepArray the frames, may be {@code null}
     * @param commonFrames the number of trailing frames to leave out; a value below 0 or above the
     *                     number of frames is taken as 0
     */
    protected void appendSTEPArray(StringBuilder sb, StackTraceElementProxy[] stepArray, int commonFrames) {
        appendMemberName(sb, STEP_ARRAY_NAME_ATTRIBUTE);
        sb.append(OPEN_ARRAY);

        // If there are no stack trace elements, write an empty array and return early.
        if (stepArray == null || stepArray.length == 0) {
            sb.append(CLOSE_ARRAY);
            return;
        }

        int len = stepArray.length;

        if (commonFrames < 0 || commonFrames > len) {
            commonFrames = 0;
        }

        for (int i = 0; i < len - commonFrames; i++) {
            if (i != 0)
                sb.append(VALUE_SEPARATOR);
            appendStackTraceElement(sb, stepArray[i].getStackTraceElement());
        }

        sb.append(CLOSE_ARRAY);
    }

    /**
     * Write a stack trace element as an object with the members {@code className},
     * {@code methodName}, {@code fileName} and {@code lineNumber}. Used for {@code stepArray} and
     * {@code callerData}.
     *
     * @param sb the builder to write to
     * @param ste the stack trace element, may be {@code null}
     * @since 1.6.8
     */
    protected void appendStackTraceElement(StringBuilder sb, StackTraceElement ste) {
        if (ste == null) {
            sb.append(JSON_NULL);
            return;
        }
        sb.append(OPEN_OBJ);
        appenderMember(sb, CLASS_NAME_ATTR_NAME, ste.getClassName());
        sb.append(VALUE_SEPARATOR);
        appenderMember(sb, METHOD_NAME_ATTR_NAME, ste.getMethodName());
        sb.append(VALUE_SEPARATOR);
        appenderMember(sb, FILE_NAME_ATTR_NAME, ste.getFileName());
        sb.append(VALUE_SEPARATOR);
        appenderMemberWithIntValue(sb, LINE_NUMBER_ATTR_NAME, ste.getLineNumber());
        sb.append(CLOSE_OBJ);
    }

    /**
     * Write the {@code callerData} member when the event already holds caller data. Caller data is
     * never computed for the sake of writing it.
     *
     * @param sb the builder to write to
     * @param event the logging event
     * @since 1.6.8
     */
    protected void appendCallerData(StringBuilder sb, ILoggingEvent event) {
        if (!event.hasCallerData())
            return;
        StackTraceElement[] callerData = event.getCallerData();
        if (callerData == null)
            return;

        appendMemberSeparator(sb);
        appendMemberName(sb, CALLER_DATA_ATTR_NAME);
        sb.append(OPEN_ARRAY);
        for (int i = 0; i < callerData.length; i++) {
            if (i != 0)
                sb.append(VALUE_SEPARATOR);
            appendStackTraceElement(sb, callerData[i]);
        }
        sb.append(CLOSE_ARRAY);
    }

    /**
     * Hook allowing subclasses to append additional fields into the root JSON object.
     * Default implementation is a no-op.
     *
     * <p>Subclasses may append additional top-level JSON members here. Before each member, call
     * {@link #appendMemberSeparator(StringBuilder)}, which writes a comma only if a member was
     * written before, then write the member, for example with
     * {@link #appenderMember(StringBuilder, String, String)}:</p>
     *
     * <pre>{@code
     * protected void appendCustomFields(StringBuilder sb, ILoggingEvent event) {
     *     appendMemberSeparator(sb);
     *     appenderMember(sb, "host", hostName);
     * }
     * }</pre>
     *
     * <p>Implementations must not close the root JSON object or write the final
     * line separator; {@link JsonEncoder} handles those.</p>
     *
     * @param sb the StringBuilder that accumulates the JSON output; never null
     * @param event the logging event being encoded; never null
     */
    protected void appendCustomFields(StringBuilder sb, ILoggingEvent event) {
        // no-op by default; subclasses may call appendMemberSeparator(sb) then write their fields
    }

    /**
     * Write a member name and the colon that follows it. The name is escaped; a {@code null} name
     * is written as {@code "null"}, since a JSON member name cannot be null.
     *
     * @param sb the builder to write to
     * @param name the member name, not escaped
     * @since 1.6.8
     */
    protected void appendMemberName(StringBuilder sb, String name) {
        sb.append(QUOTE);
        appendJsonEscapedString(sb, name == null ? JSON_NULL : name);
        sb.append(QUOTE_COL);
    }

    /**
     * Write a string value: {@code null} for a {@code null}, an escaped and quoted string otherwise.
     * Every string value of the output is written by this method.
     *
     * @param sb the builder to write to
     * @param value the value, not escaped, may be {@code null}
     * @since 1.6.8
     */
    protected void appendStringValue(StringBuilder sb, String value) {
        if (value == null) {
            sb.append(JSON_NULL);
            return;
        }
        sb.append(QUOTE);
        appendJsonEscapedString(sb, value);
        sb.append(QUOTE);
    }

    /**
     * Write a member whose value is a string.
     *
     * @param sb the builder to write to
     * @param key the member name, not escaped
     * @param value the value, not escaped, may be {@code null}
     */
    protected void appenderMember(StringBuilder sb, String key, String value) {
        appendMemberName(sb, key);
        appendStringValue(sb, value);
    }

    protected void appenderMemberWithIntValue(StringBuilder sb, String key, int value) {
        appendMemberName(sb, key);
        sb.append(value);
    }

    protected void appenderMemberWithLongValue(StringBuilder sb, String key, long value) {
        appendMemberName(sb, key);
        sb.append(value);
    }

    /**
     * Write an argument or a key-value value:
     *
     * <ul>
     * <li>{@code null} as {@code null};</li>
     * <li>a {@link Boolean} as {@code true} or {@code false};</li>
     * <li>a {@link Byte}, {@link Short}, {@link Integer}, {@link Long}, {@link BigInteger} or
     * {@link BigDecimal}, and a finite {@link Float} or {@link Double}, as a JSON number;</li>
     * <li>a {@link Float} or {@link Double} that is NaN or infinite as the string {@code "NaN"},
     * {@code "Infinity"} or {@code "-Infinity"};</li>
     * <li>anything else as the string returned by its {@code toString()} method, or
     * {@code "[FAILED toString()]"} if that method throws.</li>
     * </ul>
     *
     * @param sb the builder to write to
     * @param value the value, may be {@code null}
     * @since 1.6.8
     */
    protected void appendArgumentValue(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append(JSON_NULL);
            return;
        }

        Class<?> c = value.getClass();
        if (c == Boolean.class) {
            sb.append(((Boolean) value).booleanValue());
        } else if (c == Integer.class || c == Long.class || c == Short.class || c == Byte.class
                || c == BigInteger.class || c == BigDecimal.class) {
            sb.append(value);
        } else if (c == Double.class) {
            double d = (Double) value;
            if (Double.isFinite(d)) {
                sb.append(Double.toString(d));
            } else {
                appendStringValue(sb, Double.toString(d));
            }
        } else if (c == Float.class) {
            float f = (Float) value;
            if (Float.isFinite(f)) {
                sb.append(Float.toString(f));
            } else {
                appendStringValue(sb, Float.toString(f));
            }
        } else {
            appendStringValue(sb, safeToString(value));
        }
    }

    private static String safeToString(Object o) {
        try {
            return o.toString();
        } catch (Throwable t) {
            return FAILED_TO_STRING;
        }
    }

    protected void appendKeyValuePairs(StringBuilder sb, ILoggingEvent event) {
        List<KeyValuePair> kvpList = event.getKeyValuePairs();
        if (kvpList == null)
            return;

        appendMemberSeparator(sb);
        appendMemberName(sb, KEY_VALUE_PAIRS_ATTR_NAME);
        sb.append(SP).append(OPEN_ARRAY);
        final int len = kvpList.size();
        for (int i = 0; i < len; i++) {
            if (i != 0)
                sb.append(VALUE_SEPARATOR);
            KeyValuePair kvp = kvpList.get(i);
            if (kvp == null) {
                sb.append(JSON_NULL);
                continue;
            }
            sb.append(OPEN_OBJ);
            appendMemberName(sb, kvp.key);
            appendArgumentValue(sb, kvp.value);
            sb.append(CLOSE_OBJ);
        }
        sb.append(CLOSE_ARRAY);
    }

    protected void appendArgumentArray(StringBuilder sb, ILoggingEvent event) {
        Object[] argumentArray = event.getArgumentArray();
        if (argumentArray == null)
            return;

        appendMemberSeparator(sb);
        appendMemberName(sb, ARGUMENT_ARRAY_ATTR_NAME);
        sb.append(SP).append(OPEN_ARRAY);
        final int len = argumentArray.length;
        for (int i = 0; i < len; i++) {
            if (i != 0)
                sb.append(VALUE_SEPARATOR);
            appendArgumentValue(sb, argumentArray[i]);
        }
        sb.append(CLOSE_ARRAY);
    }

    /**
     * Write the {@code markers} member, an array of marker names. The references of a marker are not
     * written.
     *
     * @param sb the builder to write to
     * @param event the logging event
     */
    protected void appendMarkers(StringBuilder sb, ILoggingEvent event) {
        List<Marker> markerList = event.getMarkerList();
        if (markerList == null)
            return;

        appendMemberSeparator(sb);
        appendMemberName(sb, MARKERS_ATTR_NAME);
        sb.append(SP).append(OPEN_ARRAY);
        final int len = markerList.size();
        for (int i = 0; i < len; i++) {
            if (i != 0)
                sb.append(VALUE_SEPARATOR);
            Marker marker = markerList.get(i);
            appendStringValue(sb, marker == null ? null : marker.getName());
        }
        sb.append(CLOSE_ARRAY);
    }

    /**
     * Write the {@code mdc} member: {@code null} when the event's MDC property map is {@code null}, an
     * object otherwise, empty or not.
     *
     * @param sb the builder to write to
     * @param event the logging event
     */
    protected void appendMDC(StringBuilder sb, ILoggingEvent event) {
        Map<String, String> map = event.getMDCPropertyMap();
        appendMemberSeparator(sb);
        appendMemberName(sb, MDC_ATTR_NAME);
        sb.append(SP);
        appendMapValue(sb, map);
    }

    /**
     * Return {@code true} when the provided map is non-null and non-empty.
     *
     * @param map the map to check; may be null
     * @return {@code true} if the map contains at least one entry
     */
    boolean isNotEmptyMap(Map<?, ?> map) {
        if (map == null)
            return false;
        return !map.isEmpty();
    }

    @Override
    public byte[] footerBytes() {
        return EMPTY_BYTES;
    }

    /**
     * Set whether the sequence number is included in each encoded event.
     * @param withSequenceNumber {@code true} to include the sequence number in the output
     * @since 1.5.0
     */
    public void setWithSequenceNumber(boolean withSequenceNumber) {
        this.withSequenceNumber = withSequenceNumber;
    }

    /**
     * Set whether the event timestamp is included in each encoded event.
     * @param withTimestamp {@code true} to include the event timestamp in the output
     * @since 1.5.0
     */
    public void setWithTimestamp(boolean withTimestamp) {
        this.withTimestamp = withTimestamp;
    }

    /**
     * Set whether nanoseconds will be included in the timestamp output.
     * @param withNanoseconds {@code true} to include nanoseconds in the timestamp output
     * @since 1.5.0
     */
    public void setWithNanoseconds(boolean withNanoseconds) {
        this.withNanoseconds = withNanoseconds;
    }

    /**
     * Enable or disable the inclusion of the log level in the encoded output.
     *
     * @param withLevel {@code true} to include the log level. Default is {@code true}.
     */
     public void setWithLevel(boolean withLevel) {
         this.withLevel = withLevel;
     }

     /**
      * Enable or disable the inclusion of the thread name in the encoded output.
      *
      * @param withThreadName {@code true} to include the thread name. Default is {@code true}.
      */
     public void setWithThreadName(boolean withThreadName) {
         this.withThreadName = withThreadName;
     }

     /**
      * Enable or disable the inclusion of the logger name in the encoded output.
      *
      * @param withLoggerName {@code true} to include the logger name. Default is {@code true}.
      */
     public void setWithLoggerName(boolean withLoggerName) {
         this.withLoggerName = withLoggerName;
     }

     /**
      * Enable or disable the inclusion of the logger context information.
      *
      * @param withContext {@code true} to include the logger context. Default is {@code true}.
      */
     public void setWithContext(boolean withContext) {
         this.withContext = withContext;
     }

     /**
      * Enable or disable the inclusion of markers in the encoded output.
      *
      * @param withMarkers {@code true} to include markers. Default is {@code true}.
      */
     public void setWithMarkers(boolean withMarkers) {
         this.withMarkers = withMarkers;
     }

     /**
      * Enable or disable the inclusion of MDC properties in the encoded output.
      *
      * @param withMDC {@code true} to include MDC properties. Default is {@code true}.
      */
     public void setWithMDC(boolean withMDC) {
         this.withMDC = withMDC;
     }

     /**
      * Enable or disable the inclusion of key-value pairs attached to the logging event.
      *
      * @param withKVPList {@code true} to include the key/value pairs list. Default is {@code true}.
      */
     public void setWithKVPList(boolean withKVPList) {
         this.withKVPList = withKVPList;
     }

     /**
      * Enable or disable the inclusion of the raw message text in the encoded output.
      *
      * @param withMessage {@code true} to include the message. Default is {@code true}.
      */
     public void setWithMessage(boolean withMessage) {
         this.withMessage = withMessage;
     }

     /**
      * Enable or disable the inclusion of the event argument array in the encoded output.
      *
      * @param withArguments {@code true} to include the argument array. Default is {@code true}.
      */
     public void setWithArguments(boolean withArguments) {
         this.withArguments = withArguments;
     }

     /**
      * Enable or disable the inclusion of throwable information in the encoded output.
      *
      * @param withThrowable {@code true} to include throwable/stacktrace information. Default is {@code true}.
      */
     public void setWithThrowable(boolean withThrowable) {
         this.withThrowable = withThrowable;
     }

     /**
      * Enable or disable the inclusion of the formatted message in the encoded output.
      *
      * @param withFormattedMessage {@code true} to include the formatted message. Default is {@code false}.
      */
     public void setWithFormattedMessage(boolean withFormattedMessage) {
         this.withFormattedMessage = withFormattedMessage;
     }

     /**
      * Enable or disable the inclusion of caller data in the encoded output. Caller data is written
      * only when the event already holds it; it is never computed for the sake of writing it.
      *
      * @param withCallerData {@code true} to include caller data. Default is {@code false}.
      * @since 1.6.8
      */
     public void setWithCallerData(boolean withCallerData) {
         this.withCallerData = withCallerData;
     }

 }
