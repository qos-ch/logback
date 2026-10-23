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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.text.DateFormatSymbols;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CharSequenceToRegexMapperTest {
    static Locale KO_LOCALE = new Locale("ko", "KR");

    @Test
    public void findMinMaxLengthsInSymbolsWithTrivialInputs() {
        String[] symbols = new String[] { "a", "bb" };
        int[] results = CharSequenceToRegexMapper.findMinMaxLengthsInSymbols(symbols);
        assertEquals(1, results[0]);
        assertEquals(2, results[1]);
    }

    @Test
    public void emptyStringValuesShouldBeIgnoredByFindMinMaxLengthsInSymbols() {
        String[] symbols = new String[] { "aaa", "" };
        int[] results = CharSequenceToRegexMapper.findMinMaxLengthsInSymbols(symbols);
        assertEquals(3, results[0]);
        assertEquals(3, results[1]);
    }

    @Test
    public void localeArgumentIsHonored() {
        CharSequenceToRegexMapper usMapper = new CharSequenceToRegexMapper(Locale.US);
        // "Monday" is the shortest US weekday, "Wednesday" the longest
        assertEquals(".{6,9}", usMapper.toRegex(makeState('E', 4)));

        CharSequenceToRegexMapper germanMapper = new CharSequenceToRegexMapper(Locale.GERMANY);
        // "Montag" is the shortest German weekday, "Donnerstag" the longest
        assertEquals(".{6,10}", germanMapper.toRegex(makeState('E', 4)));
    }

    @Test
    public void defaultConstructorUsesDefaultLocale() {
        CharSequenceToRegexMapper defaultMapper = new CharSequenceToRegexMapper();
        CharSequenceToRegexMapper explicitMapper = new CharSequenceToRegexMapper(
                Locale.getDefault(Locale.Category.FORMAT));
        assertEquals(explicitMapper.symbols, defaultMapper.symbols);
    }

    private CharSequenceState makeState(char c, int occurrences) {
        CharSequenceState state = new CharSequenceState(c);
        for (int i = 1; i < occurrences; i++) {
            state.incrementOccurrences();
        }
        return state;
    }

    @Test
    @Disabled
    public void noneOfTheSymbolsAreOfZeroLengthForKorean() {
        noneOfTheSymbolsAreOfZeroLength(KO_LOCALE);
    }

    @Test
    @Disabled
    public void noneOfTheSymbolsAreOfZeroLengthForSwiss() {
        noneOfTheSymbolsAreOfZeroLength(new Locale("fr", "CH"));
    }

    private void noneOfTheSymbolsAreOfZeroLength(Locale locale) {
        DateFormatSymbols dateFormatSymbols = new CharSequenceToRegexMapper(locale).symbols;
        // checkEmptyString(dateFormatSymbols.getShortMonths(), "ShortMonths");
        // checkEmptyString(dateFormatSymbols.getMonths(), "Months");
        checkEmptyString(dateFormatSymbols.getShortWeekdays(), "ShortWeekdays");
        checkEmptyString(dateFormatSymbols.getWeekdays(), "Weekdays");
        checkEmptyString(dateFormatSymbols.getAmPmStrings(), "AmPmStrings");

    }

    private void checkEmptyString(String[] symbolArray, String category) {
        for (String s : symbolArray) {
            System.out.println(category + " [" + s + "]");
            Assertions.assertTrue(s.length() > 0, category + " contains empty strings");
        }
    }

}
