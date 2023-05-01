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
import ch.qos.logback.core.util.DirectJson;

import java.nio.charset.Charset;

import static ch.qos.logback.core.CoreConstants.COLON_CHAR;
import static ch.qos.logback.core.CoreConstants.COMMA_CHAR;
import static ch.qos.logback.core.CoreConstants.DOUBLE_QUOTE_CHAR;
import static ch.qos.logback.core.CoreConstants.UTF_8_CHARSET;
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
    public static final String LEVEL_ATTR_NAME = "level";
    public static final String MARKERS_ATTR_NAME = "markers";
    public static final String THREAD_ATTR_NAME = "thread";
    public static final String MDC_ATTR_NAME = "mdc";
    public static final String LOGGER_ATTR_NAME = "logger";
    public static final String MESSAGE_ATTR_NAME = "raw-message";
    public static final String THROWABLE_ATTR_NAME = "throwable";

    private static final char OPEN_OBJ = '{';
    private static final char CLOSE_OBJ = '}';
    private static final char OPEN_ARR = '[';
    private static final char CLOSE_ARR = ']';

    private static final char QUOTE = DOUBLE_QUOTE_CHAR;
    private static final char SP = ' ';
    private static final char ENTRY_SEPARATOR = COLON_CHAR;
    private static final char VALUE_SEPARATOR = COMMA_CHAR;



    @Override
    public byte[] headerBytes() {
        return EMPTY_BYTES;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        final int initialCapacity = event.getThrowableProxy() == null ? DEFAULT_SIZE: DEFAULT_SIZE_WITH_THROWABLE;
        StringBuilder sb = new StringBuilder(initialCapacity);



        return sb.toString().getBytes(UTF_8_CHARSET);
    }


    public void writeLevel(StringBuilder sb, Level level) {
        String levelString = level != null? level.toString() : NULL_STR;
        writeStringValue(sb, LEVEL_ATTR_NAME, levelString);
    }

    void writeStringValue(StringBuilder sb, String attrName, String value) {
        sb.append(attrName).append(ENTRY_SEPARATOR).append(SP).append(QUOTE).append(value);
        Character c = ' ';
    }

    public void writeSep(StringBuilder sb) {
        sb.append(',');
    }
    @Override
    public byte[] footerBytes() {
        return EMPTY_BYTES;
    }
}
