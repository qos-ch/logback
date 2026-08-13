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
package ch.qos.logback.classic.sift;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.AbstractDiscriminator;
import ch.qos.logback.core.util.BatchedFixedIntervalInvocationGate;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.OptionHelper;

import java.util.Map;

/**
 * MDCBasedDiscriminator essentially returns the value mapped to an MDC key. If
 * the said value is null, then a default value is returned.
 * <p/>
 * <p>
 * Both Key and the DefaultValue are user specified properties.
 * </p>
 * <p>
 * Path characters ({@code /} and {@code \}) are removed from the discriminating
 * value so that it is safe to use in file names or similar path segments.
 * </p>
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class MDCBasedDiscriminator extends AbstractDiscriminator<ILoggingEvent> {

    private static final char FORWARD_SLASH = '/';
    private static final char BACKWARD_SLASH = '\\';
    static final String REQUIRED_SANITIZING_WARNING = "Required sanitizing of path characters from MDC value [%s]";
    private String key;
    private String defaultValue;
    /** Limits how often path-sanitization warnings are emitted on the hot path. */
    private final BatchedFixedIntervalInvocationGate invocationGate =
            new BatchedFixedIntervalInvocationGate(4, Duration.buildByMinutes(10));

    @Override
    public void start() {
        int errors = 0;
        if (OptionHelper.isNullOrEmptyOrAllSpaces(key)) {
            errors++;
            addError("The \"Key\" property must be set");
        }
        if (OptionHelper.isNullOrEmptyOrAllSpaces(defaultValue)) {
            errors++;
            addError("The \"DefaultValue\" property must be set");
        }
        if (errors == 0) {
            started = true;
        }
    }

    /**
     * Return the value associated with an MDC entry designated by the Key property.
     * If that value is null, then return the value assigned to the DefaultValue
     * property.
     * <p>
     * Path characters ({@code /} and {@code \}) are stripped from the result.
     * </p>
     */
    public String getDiscriminatingValue(ILoggingEvent event) {
        // http://jira.qos.ch/browse/LBCLASSIC-213
        Map<String, String> mdcMap = event.getMDCPropertyMap();
        if (mdcMap == null) {
            return defaultValue;
        }
        String mdcValue = mdcMap.get(key);
        if (mdcValue == null) {
            return defaultValue;
        } else {
            return sanitizePathCharacters(mdcValue, event.getTimeStamp());
        }
    }

    /**
     * Removes every {@code /} and {@code \} (any number of occurrences) so the
     * value is safe as a file-name segment.
     * <p>
     * Optimized for the common case where neither character is present: a single
     * scan and no allocation. When sanitization is needed, the prefix is kept and
     * the remainder is copied while skipping all path separators.
     * </p>
     */
     String sanitizePathCharacters(String value, long timestamp) {
        if (value == null) {
            return null;
        }
        final int len = value.length();
        int i = 0;
        for (; i < len; i++) {
            char c = value.charAt(i);
            if (c == FORWARD_SLASH || c == BACKWARD_SLASH) {
                break;
            }
        }
        // no path separators → return original (fast path, zero allocation)
        if (i == len) {
            return value;
        }

        if(!invocationGate.isTooSoon(timestamp)) {
            addWarn(String.format(REQUIRED_SANITIZING_WARNING, value));
        }

        StringBuilder sb = new StringBuilder(len - 1);
        sb.append(value, 0, i);
        for (i++; i < len; i++) {
            char c = value.charAt(i);
            if (c != FORWARD_SLASH && c != BACKWARD_SLASH) {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return
     * @see #setDefaultValue(String)
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * The default MDC value in case the MDC is not set for {@link #setKey(String)
     * mdcKey}.
     * <p/>
     * <p>
     * For example, if {@link #setKey(String) Key} is set to the value "someKey",
     * and the MDC is not set for "someKey", then this appender will use the default
     * value, which you can set with the help of this method.
     *
     * @param defaultValue
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
