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

import java.text.DateFormatSymbols;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * This class supports mapping character sequences to regular expressions as
 * appropriate for DateTimeFormatter.
 *
 * @author Ceki
 */
class CharSequenceToRegexMapper {

    // characters with a special meaning in regular expressions outside of character classes
    static final String REGEX_META_CHARACTERS = "\\.[]{}()*+?^$|";

    final Locale locale;
    final DateFormatSymbols symbols;

    /**
     * Create a mapper using the symbols of the default locale.
     */
    CharSequenceToRegexMapper() {
        this(Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * Create a mapper using the symbols of the given locale.
     *
     * @param locale the locale whose month, weekday and am/pm symbols are used
     */
    CharSequenceToRegexMapper(Locale locale) {
        this.locale = locale;
        this.symbols = DateFormatSymbols.getInstance(locale);
    }

    String toRegex(CharSequenceState css) {
        final int occurrences = css.occurrences;
        final char c = css.c;
        switch (css.c) {
        case 'G':
        case 'z':
            return ".*";
        case 'M':
            if (occurrences <= 2)
                return number(occurrences);
            else if (occurrences == 3)
                return getRegexForShortMonths();
            else
                return getRegexForLongMonths();
        case 'L':
            if (occurrences <= 2)
                return number(occurrences);
            else
                return getRegexForStandaloneMonths(occurrences);
        case 'y':
        case 'w':
        case 'W':
        case 'D':
        case 'd':
        case 'F':
        case 'H':
        case 'k':
        case 'K':
        case 'h':
        case 'm':
        case 's':
        case 'S':
            return number(occurrences);
        case 'E':
            if (occurrences >= 4) {
                return getRegexForLongDaysOfTheWeek();
            } else {
                return getRegexForShortDaysOfTheWeek();
            }
        case 'a':
            return getRegexForAmPms();
        case 'Z':
            return "(\\+|-)\\d{4}";
        case '\\':
            throw new IllegalStateException("Forward slashes are not allowed");
        case '\'':
            if (occurrences == 1) {
                return "";
            }
            throw new IllegalStateException("Too many single quotes");
        default:
            String literal = escapeRegexMetaCharacter(c);
            if (occurrences == 1) {
                return literal;
            } else {
                return literal + "{" + occurrences + "}";
            }
        }
    }

    /**
     * Escapes a literal character of the date pattern if it has a special meaning in
     * regular expressions, e.g. '(' in "yyyy-MM-dd_(HH)".
     */
    static String escapeRegexMetaCharacter(char c) {
        if (REGEX_META_CHARACTERS.indexOf(c) != -1) {
            return "\\" + c;
        } else {
            return String.valueOf(c);
        }
    }

    private String number(int occurrences) {
        return "\\d{" + occurrences + "}";
    }

    private String getRegexForAmPms() {
        return symbolArrayToRegex(symbols.getAmPmStrings());
    }

    private String getRegexForLongDaysOfTheWeek() {
        return symbolArrayToRegex(symbols.getWeekdays());
    }

    private String getRegexForShortDaysOfTheWeek() {
        return symbolArrayToRegex(symbols.getShortWeekdays());
    }

    private String getRegexForLongMonths() {
        return symbolArrayToRegex(symbols.getMonths());
    }

    String getRegexForShortMonths() {
        return symbolArrayToRegex(symbols.getShortMonths());
    }

    // DateFormatSymbols does not provide stand-alone month names. They are obtained
    // by formatting each month with the stand-alone pattern itself.
    private String getRegexForStandaloneMonths(int occurrences) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("L".repeat(occurrences), locale);
        Month[] months = Month.values();
        String[] monthNames = new String[months.length];
        for (int i = 0; i < months.length; i++) {
            monthNames[i] = dtf.format(months[i]);
        }
        return symbolArrayToRegex(monthNames);
    }

    private String symbolArrayToRegex(String[] symbolArray) {
        int[] minMax = findMinMaxLengthsInSymbols(symbolArray);
        return ".{" + minMax[0] + "," + minMax[1] + "}";
    }

    static int[] findMinMaxLengthsInSymbols(String[] symbols) {
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (String symbol : symbols) {
            int len = symbol.length();
            // some SENTINEL values can be empty strings, the month at index 12 or the
            // weekday at index 0
            if (len == 0)
                continue;
            min = Math.min(min, len);
            max = Math.max(max, len);
        }
        return new int[] { min, max };
    }
}
