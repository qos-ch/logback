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
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
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
 * JSON encoder that produces one JSON object per line in JSON Lines format, suitable for structured logging.
 * Each {@link ILoggingEvent} is encoded into a JSON object containing fields such as timestamp, level, message,
 * and optional elements like MDC properties, markers, and stack traces.
 *
 * <p>This encoder supports extensive configuration through boolean flags to include or exclude specific fields
 * in the output, allowing customization for different logging needs. For example, you can enable/disable
 * sequence numbers, nanoseconds, thread names, logger context, markers, MDC, key-value pairs, arguments,
 * and throwable information.</p>
 *
 * <p>The encoder is designed for extensibility: subclasses can override protected methods (e.g.,
 * {@link #appendLoggerContext}, {@link #appendThrowableProxy}, {@link #appendMarkers}) to customize
 * how specific parts of the JSON are generated. Additionally, the {@link #appendCustomFields} hook
 * allows appending custom top-level fields to the JSON object.</p>
 *
 * <h3>Configuration</h3>
 * <p>Use the setter methods (e.g., {@link #setWithSequenceNumber}, {@link #setWithTimestamp}) to control
 * which fields are included. By default, most fields are enabled except {@code withFormattedMessage}.</p>
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

    public static final String THROWABLE_ATTR_NAME = "throwable";

    private static final String CYCLIC_THROWABLE_ATTR_NAME = "cyclic";

    public static final String CAUSE_ATTR_NAME = "cause";

    public static final String SUPPRESSED_ATTR_NAME = "suppressed";

    public static final String COMMON_FRAMES_COUNT_ATTR_NAME = "commonFramesCount";

    public static final String CLASS_NAME_ATTR_NAME = "className";
    public static final String METHOD_NAME_ATTR_NAME = "methodName";
    private static final String FILE_NAME_ATTR_NAME = "fileName";
    private static final String LINE_NUMBER_ATTR_NAME = "lineNumber";

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
        }

        if (withTimestamp) {
            appendValueSeparator(sb, withSequenceNumber);
            appenderMemberWithLongValue(sb, TIMESTAMP_ATTR_NAME, event.getTimeStamp());
        }

        if (withNanoseconds) {
            appendValueSeparator(sb, withSequenceNumber, withTimestamp);
            appenderMemberWithLongValue(sb, NANOSECONDS_ATTR_NAME, event.getNanoseconds());
        }

        if (withLevel) {
            appendValueSeparator(sb, withNanoseconds, withSequenceNumber, withTimestamp);
            String levelStr = event.getLevel() != null ? event.getLevel().levelStr : NULL_STR;
            appenderMember(sb, LEVEL_ATTR_NAME, levelStr);
        }

        if (withThreadName) {
            appendValueSeparator(sb, withLevel, withNanoseconds, withSequenceNumber, withTimestamp);
            appenderMember(sb, THREAD_NAME_ATTR_NAME, jsonEscape(event.getThreadName()));
        }

        if (withLoggerName) {
            appendValueSeparator(sb, withThreadName, withLevel, withNanoseconds, withSequenceNumber, withTimestamp);
            appenderMember(sb, LOGGER_ATTR_NAME, event.getLoggerName());
        }

        if (withContext) {
            // at this stage we assume that at least one field was written
            sb.append(VALUE_SEPARATOR);
            appendLoggerContext(sb, event.getLoggerContextVO());
        }

        if (withMarkers)
            appendMarkers(sb, event);

        if (withMDC)
            appendMDC(sb, event);

        if (withKVPList)
            appendKeyValuePairs(sb, event);

        if (withMessage) {
            sb.append(VALUE_SEPARATOR);
            appenderMember(sb, MESSAGE_ATTR_NAME, jsonEscape(event.getMessage()));
        }

        if (withFormattedMessage) {
            sb.append(VALUE_SEPARATOR);
            appenderMember(sb, FORMATTED_MESSAGE_ATTR_NAME, jsonEscape(event.getFormattedMessage()));
        }

        if (withArguments) {
            appendArgumentArray(sb, event);
        }

        if (withThrowable)
            appendThrowableProxy(sb, THROWABLE_ATTR_NAME, event.getThrowableProxy());

        // allow subclasses to append custom top-level fields; default implementation is a no-op
        appendCustomFields(sb, event);

        sb.append(CLOSE_OBJ);
        sb.append(CoreConstants.JSON_LINE_SEPARATOR);
        return sb.toString().getBytes(UTF_8_CHARSET);
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
     * <p>This method is protected so subclasses that extend the encoder can
     * reuse or override the logic for inserting separators between generated
     * JSON members.</p>
     *
     * @param sb the {@link StringBuilder} to append the separator to; must not be {@code null}
     * @param subsequentConditionals one or more booleans indicating whether
     *                               subsequent JSON elements will be written.
     *                               If any value is {@code true}, a comma is appended.
     */
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

    protected void appendMap(StringBuilder sb, String attrName, Map<String, String> map) {
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

    protected void appendThrowableProxy(StringBuilder sb, String attributeName, IThrowableProxy itp) {
        appendThrowableProxy(sb, attributeName, itp, true);
    }

    protected void appendThrowableProxy(StringBuilder sb, String attributeName, IThrowableProxy itp, boolean appendValueSeparator) {

        if (appendValueSeparator)
            sb.append(VALUE_SEPARATOR);

        // in the nominal case, attributeName != null. However, attributeName will be null for suppressed
        // IThrowableProxy array, in which case no attribute name is needed
        if (attributeName != null) {
            sb.append(QUOTE).append(attributeName).append(QUOTE_COL);
            if (itp == null) {
                sb.append(NULL_STR);
                return;
            }
        }

        sb.append(OPEN_OBJ);

        appenderMember(sb, CLASS_NAME_ATTR_NAME, nullSafeStr(itp.getClassName()));

        sb.append(VALUE_SEPARATOR);
        appenderMember(sb, MESSAGE_ATTR_NAME, jsonEscape(itp.getMessage()));

        if (itp.isCyclic()) {
            sb.append(VALUE_SEPARATOR);
            appenderMember(sb, CYCLIC_THROWABLE_ATTR_NAME, jsonEscape("true"));
        }

        sb.append(VALUE_SEPARATOR);
        appendSTEPArray(sb, itp.getStackTraceElementProxyArray(), itp.getCommonFrames());

        if (itp.getCommonFrames() != 0) {
            sb.append(VALUE_SEPARATOR);
            appenderMemberWithIntValue(sb, COMMON_FRAMES_COUNT_ATTR_NAME, itp.getCommonFrames());
        }

        IThrowableProxy cause = itp.getCause();
        if (cause != null) {
            appendThrowableProxy(sb, CAUSE_ATTR_NAME, cause);
        }

        IThrowableProxy[] suppressedArray = itp.getSuppressed();
        if (suppressedArray != null && suppressedArray.length != 0) {
            sb.append(VALUE_SEPARATOR);
            sb.append(QUOTE).append(SUPPRESSED_ATTR_NAME).append(QUOTE_COL);
            sb.append(OPEN_ARRAY);

            boolean first = true;
            for (IThrowableProxy suppressedITP : suppressedArray) {
                appendThrowableProxy(sb, null, suppressedITP, !first);
                if (first)
                    first = false;
            }
            sb.append(CLOSE_ARRAY);
        }

        sb.append(CLOSE_OBJ);

    }

    protected void appendSTEPArray(StringBuilder sb, StackTraceElementProxy[] stepArray, int commonFrames) {
        sb.append(QUOTE).append(STEP_ARRAY_NAME_ATTRIBUTE).append(QUOTE_COL).append(OPEN_ARRAY);

        // If there are no stack trace elements, write an empty array and return early.
        if (stepArray == null || stepArray.length == 0) {
            sb.append(CLOSE_ARRAY);
            return;
        }

        int len = stepArray.length;

        if (commonFrames >= len) {
            commonFrames = 0;
        }

        for (int i = 0; i < len - commonFrames; i++) {
            if (i != 0)
                sb.append(VALUE_SEPARATOR);

            StackTraceElementProxy step = stepArray[i];

            sb.append(OPEN_OBJ);
            StackTraceElement ste = step.getStackTraceElement();

            appenderMember(sb, CLASS_NAME_ATTR_NAME, nullSafeStr(ste.getClassName()));
            sb.append(VALUE_SEPARATOR);

            appenderMember(sb, METHOD_NAME_ATTR_NAME, nullSafeStr(ste.getMethodName()));
            sb.append(VALUE_SEPARATOR);

            appenderMember(sb, FILE_NAME_ATTR_NAME, nullSafeStr(ste.getFileName()));
            sb.append(VALUE_SEPARATOR);

            appenderMemberWithIntValue(sb, LINE_NUMBER_ATTR_NAME, ste.getLineNumber());
            sb.append(CLOSE_OBJ);

        }

        sb.append(CLOSE_ARRAY);
    }

    /**
     * Hook allowing subclasses to append additional fields into the root JSON object.
     * Default implementation is a no-op.
     *
     * <p>Subclasses may append additional top-level JSON members here. If a
     * subclass writes additional members it should prepend them with
     * {@link #VALUE_SEPARATOR} (a comma) if necessary to keep the JSON valid.
     * Implementations must not close the root JSON object or write the final
     * line separator; {@link JsonEncoder} handles those.</p>
     *
     * @param sb the StringBuilder that accumulates the JSON output; never null
     * @param event the logging event being encoded; never null
     */
    protected void appendCustomFields(StringBuilder sb, ILoggingEvent event) {
        // no-op by default; subclasses may append VALUE_SEPARATOR then their fields
    }

    protected void appenderMember(StringBuilder sb, String key, String value) {
        sb.append(QUOTE).append(key).append(QUOTE_COL).append(QUOTE).append(value).append(QUOTE);
    }

    protected void appenderMemberWithIntValue(StringBuilder sb, String key, int value) {
        sb.append(QUOTE).append(key).append(QUOTE_COL).append(value);
    }

    protected void appenderMemberWithLongValue(StringBuilder sb, String key, long value) {
        sb.append(QUOTE).append(key).append(QUOTE_COL).append(value);
    }

    protected void appendKeyValuePairs(StringBuilder sb, ILoggingEvent event) {
        List<KeyValuePair> kvpList = event.getKeyValuePairs();
        if (kvpList == null || kvpList.isEmpty())
            return;

        sb.append(VALUE_SEPARATOR);
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
    }

    protected void appendArgumentArray(StringBuilder sb, ILoggingEvent event) {
        Object[] argumentArray = event.getArgumentArray();
        if (argumentArray == null)
            return;

        sb.append(VALUE_SEPARATOR);
        sb.append(QUOTE).append(ARGUMENT_ARRAY_ATTR_NAME).append(QUOTE_COL).append(SP).append(OPEN_ARRAY);
        final int len = argumentArray.length;
        for (int i = 0; i < len; i++) {
            if (i != 0)
                sb.append(VALUE_SEPARATOR);
            sb.append(QUOTE).append(jsonEscapedToString(argumentArray[i])).append(QUOTE);

        }
        sb.append(CLOSE_ARRAY);
    }

    protected void appendMarkers(StringBuilder sb, ILoggingEvent event) {
        List<Marker> markerList = event.getMarkerList();
        if (markerList == null)
            return;

        sb.append(VALUE_SEPARATOR);
        sb.append(QUOTE).append(MARKERS_ATTR_NAME).append(QUOTE_COL).append(SP).append(OPEN_ARRAY);
        final int len = markerList.size();
        for (int i = 0; i < len; i++) {
            if (i != 0)
                sb.append(VALUE_SEPARATOR);
            sb.append(QUOTE).append(jsonEscapedToString(markerList.get(i))).append(QUOTE);

        }
        sb.append(CLOSE_ARRAY);
    }

    private String jsonEscapedToString(Object o) {
        if (o == null)
            return NULL_STR;
        return jsonEscapeString(o.toString());
    }

    private String nullSafeStr(String s) {
        if (s == null)
            return NULL_STR;
        return s;
    }

    private String jsonEscape(String s) {
        if (s == null)
            return NULL_STR;
        return jsonEscapeString(s);
    }

    protected void appendMDC(StringBuilder sb, ILoggingEvent event) {
        Map<String, String> map = event.getMDCPropertyMap();
        sb.append(VALUE_SEPARATOR);
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

 }
