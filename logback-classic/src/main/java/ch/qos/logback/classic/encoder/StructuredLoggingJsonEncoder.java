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

import java.time.Instant;
import java.util.Arrays;
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
public class StructuredLoggingJsonEncoder extends JsonEncoder {

    protected boolean withTimestampSeconds = false;
    protected boolean withTimestampNanos = false;
    protected boolean withTime = false;
    protected boolean withSeverity = true;

    public StructuredLoggingJsonEncoder() {
        super();
        withArguments = false;
        withLevel = false;
    }

    @Override
    protected void appenderTimestamp(StringBuilder sb, ILoggingEvent event) {
        sb.append(QUOTE).append("timestamp").append(QUOTE_COL);
        sb.append(OPEN_OBJ);
        Instant timestamp = event.getInstant();
        appenderMemberWithLongValue(sb, "seconds", timestamp.getEpochSecond());
        sb.append(VALUE_SEPARATOR);
        appenderMemberWithIntValue(sb, "nanos", timestamp.getNano());
        sb.append(CLOSE_OBJ);
    }

    @Override
    protected void appenderExtra(StringBuilder sb, ILoggingEvent event) {
        Instant timestamp = event.getInstant();
        if (withTimestampSeconds) {
            sb.append(VALUE_SEPARATOR);
            appenderMemberWithLongValue(sb, "timestampSeconds", timestamp.getEpochSecond());
        }
        if (withTimestampNanos) {
            sb.append(VALUE_SEPARATOR);
            appenderMemberWithIntValue(sb, "timestampNanos", timestamp.getNano());
        }
        if (withTime) {
            sb.append(VALUE_SEPARATOR);
            appenderMember(sb, "time", java.time.format.DateTimeFormatter.ISO_INSTANT.format(timestamp));
        }
        if (withSeverity) {
            sb.append(VALUE_SEPARATOR);
            String levelStr = event.getLevel() != null ? event.getLevel().levelStr : NULL_STR;
            appenderMember(sb, "severity",levelStr);
        }
    }

    @Override
    protected void appenderMessage(StringBuilder sb, ILoggingEvent event) {
        appenderMember(sb, MESSAGE_ATTR_NAME, jsonEscapeString(event.getFormattedMessage()));
    }

    public void setWithTimestampSeconds(boolean withTimestampSeconds) {
        this.withTimestampSeconds = withTimestampSeconds;
    }

    public void setWithTimestampNanos(boolean withTimestampNanos) {
        this.withTimestampNanos = withTimestampNanos;
    }

    public void setWithTime(boolean withTime) {
        this.withTime = withTime;
    }
}
