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
package ch.qos.logback.core.util;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class is concerned with computing a regex corresponding to a date
 * pattern (in {@link DateTimeFormatter} format).
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class DatePatternToRegexUtil {

    final String datePattern;
    final int datePatternLength;
    final CharSequenceToRegexMapper regexMapper;

    /**
     * Create an instance using the symbols of the default locale.
     *
     * @param datePattern date pattern in {@link DateTimeFormatter} format
     */
    public DatePatternToRegexUtil(String datePattern) {
        this(datePattern, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * Create an instance using the symbols of the given locale.
     *
     * @param datePattern date pattern in {@link DateTimeFormatter} format
     * @param locale      the locale whose month, weekday and am/pm symbols are used
     * @since 1.7.0
     */
    public DatePatternToRegexUtil(String datePattern, Locale locale) {
        this.datePattern = datePattern;
        datePatternLength = datePattern.length();
        regexMapper = new CharSequenceToRegexMapper(locale);
    }

    public String toRegex() {
        List<CharSequenceState> charSequenceList = tokenize();
        StringBuilder sb = new StringBuilder();
        for (CharSequenceState seq : charSequenceList) {
            sb.append(regexMapper.toRegex(seq));
        }
        return sb.toString();
    }

    private List<CharSequenceState> tokenize() {
        List<CharSequenceState> sequenceList = new ArrayList<CharSequenceState>();

        CharSequenceState lastCharSequenceState = null;

        for (int i = 0; i < datePatternLength; i++) {
            char t = datePattern.charAt(i);
            if (lastCharSequenceState == null || lastCharSequenceState.c != t) {
                lastCharSequenceState = new CharSequenceState(t);
                sequenceList.add(lastCharSequenceState);
            } else {
                lastCharSequenceState.incrementOccurrences();
            }
        }
        return sequenceList;
    }
}
