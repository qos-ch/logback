/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.util;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.text.DateFormatSymbols;
import java.util.Locale;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class CharSequenceToRegexMapperTest {
    static Locale KO_LOCALE = new Locale("ko", "KR");
    Locale oldLocale = Locale.getDefault();

    @After
    public void tearDown() {
        Locale.setDefault(oldLocale);
    }

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
    @Ignore
    public void noneOfTheSymbolsAreOfZeroLengthForKorean() {
        Locale.setDefault(KO_LOCALE);
        noneOfTheSymbolsAreOfZeroLength();
    }

    @Test
    @Ignore
    public void noneOfTheSymbolsAreOfZeroLengthForSwiss() {
        Locale.setDefault(new Locale("fr", "CH"));
        noneOfTheSymbolsAreOfZeroLength();
    }

    private void noneOfTheSymbolsAreOfZeroLength() {
        DateFormatSymbols dateFormatSymbols = DateFormatSymbols.getInstance();
        // checkEmptyString(dateFormatSymbols.getShortMonths(), "ShortMonths");
        // checkEmptyString(dateFormatSymbols.getMonths(), "Months");
        checkEmptyString(dateFormatSymbols.getShortWeekdays(), "ShortWeekdays");
        checkEmptyString(dateFormatSymbols.getWeekdays(), "Weekdays");
        checkEmptyString(dateFormatSymbols.getAmPmStrings(), "AmPmStrings");

    }

    private void checkEmptyString(String[] symbolArray, String category) {
        for (String s : symbolArray) {
            System.out.println(category + " [" + s + "]");
            assertTrue(category + " contains empty strings", s.length() > 0);
        }
    }

}
