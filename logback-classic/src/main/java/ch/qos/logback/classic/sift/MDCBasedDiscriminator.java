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
 * MDC values containing any of the characters {@code /}, {@code \}, {@code $},
 * <code>{</code>, <code>}</code>, <code>[</code>, <code>]</code>, <code>(</code>,
 * <code>)</code> , <code>|</code>, <code>?</code>, <code>*</code>, <code>+</code>,
 * <code>%</code>, <code>,</code>, <code>@</code> or the sequence {@code ..} are rejected,
 * in which case the default value is returned. This prevents path separators, relative path components,
 * variable substitutions such as <code>${file.separator}</code>, regex
 * special characters as well as the anchor character '%' or the ',' and '@' characters from
 * leaking into file names or email addresses.
 * </p>
 *
 * <p>Moreover, the maximum length of an MDC value is limited to 64 characters.</p>
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class MDCBasedDiscriminator extends AbstractDiscriminator<ILoggingEvent> {

    static final String FORBIDDEN_CHARACTERS = "/\\${}[]()|?*+%@,";
    private static final int MAX_MDC_VALUE = 64;

    private static final char DOT = '.';

    static final String EMPTY_VALUE_WARNING = "MDC value [%s] is empty, using default value [%s] instead";
    static final String REJECTED_VALUE_WARNING = "MDC value [%s] contains forbidden characters, using default value [%s] instead";
    static final String REJECTED_VALUE_LENGTH_WARNING = "MDC value of length %s is longer than the maximum allowed length of " + MAX_MDC_VALUE + " characters, using default value [%s] instead";


    private String key;
    private String defaultValue;
    /**
     * Limits how often rejected-value warnings are emitted on the hot path.
     */
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
     * If the MDC value contains any of the characters {@code /}, {@code \},
     * {@code $}, <code>{</code> or <code>}</code>, or the sequence {@code ..}, then
     * the value assigned to the DefaultValue property is returned.
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
            String sanitized = sanitizePathCharacters(mdcValue, event.getTimeStamp());
            return sanitized == null ? defaultValue : sanitized;
        }
    }


    /**
     * Returns the value unchanged if it is safe as a file-name segment, and
     * {@code null} otherwise.
     * <p>
     * A value is rejected if it contains any of the characters {@code /},
     * {@code \}, {@code $}, <code>{</code> or <code>}</code>, or the sequence
     * {@code ..}. A (rate limited) warning is emitted for each rejected value.
     * </p>
     * <p>
     * Performs a single scan and no allocation.
     * </p>
     */
    String sanitizePathCharacters(String value, long timestamp) {
        if (value == null) {
            return null;
        }
        final int len = value.length();
        if(len == 0) {
            if (!invocationGate.isTooSoon(timestamp)) {
                addWarn(String.format(EMPTY_VALUE_WARNING, value, defaultValue));
            }
            return null;
        }

        if  (len > MAX_MDC_VALUE) {
            if (!invocationGate.isTooSoon(timestamp)) {
                addWarn(String.format(REJECTED_VALUE_LENGTH_WARNING, len, defaultValue));
            }
            return null;
        }

        char previous = 0;
        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            if (isForbiddenCharacter(c) || (c == DOT && previous == DOT)) {
                if (!invocationGate.isTooSoon(timestamp)) {
                    addWarn(String.format(REJECTED_VALUE_WARNING, value, defaultValue));
                }
                return null;
            }
            previous = c;
        }
        return value;
    }

    private static boolean isForbiddenCharacter(char c) {
        return FORBIDDEN_CHARACTERS.indexOf(c) != -1;
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
