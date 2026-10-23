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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import ch.qos.logback.core.CoreConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DatePatternToRegexUtil}.
 */
public class DatePatternToRegexUtilTest {

    static final String[] ROUND_TRIP_PATTERNS = { CoreConstants.DAILY_DATE_PATTERN, CoreConstants.ISO8601_PATTERN,
            "yyyy-MM-dd_HH-mm", "yyyy-MM-dd'T'HH:mm:ss.SSS", "MMM", "MMMM", "yyyy.MMM.dd", "yyyy-MMMM-dd", "EEE",
            "EEEE", "EEE yyyy-MM-dd", "EEEE yyyy-MM-dd", "LL", "LLL", "LLLL", "LLLLL", "yyyy-LLLL", "yyyy/LLL/dd", "yyyy-MM-dd hh a", "yyyy-MM-dd HH:mm:ss Z",
            "yyyy-MM-dd HH:mm:ss z", "yyyy-ww", "yyyy-DDD" };

    static Stream<Locale> locales() {
        return Stream.of("en-US", "de-DE", "fr-FR", "cs-CZ", "pl-PL", "fi-FI", "ru-RU", "tr-TR", "el-GR", "he-IL",
                "ar-EG", "hi-IN", "th-TH", "ja-JP", "ko-KR", "zh-CN").map(Locale::forLanguageTag);
    }

    @Test
    public void emptyPattern() {
        assertEquals("", toRegex("", Locale.US));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("locales")
    public void numericFields(Locale locale) {
        assertEquals("\\d{4}", toRegex("yyyy", locale));
        assertEquals("\\d{2}", toRegex("MM", locale));
        assertEquals("\\d{2}", toRegex("LL", locale));
        assertEquals("\\d{2}", toRegex("dd", locale));
        assertEquals("\\d{2}", toRegex("HH", locale));
        assertEquals("\\d{2}", toRegex("mm", locale));
        assertEquals("\\d{2}", toRegex("ss", locale));
        assertEquals("\\d{3}", toRegex("SSS", locale));
        assertEquals("\\d{3}", toRegex("DDD", locale));
        assertEquals("\\d{2}", toRegex("ww", locale));
        assertEquals("\\d{1}", toRegex("W", locale));
        assertEquals("\\d{1}", toRegex("F", locale));
        assertEquals("\\d{2}", toRegex("kk", locale));
        assertEquals("\\d{2}", toRegex("KK", locale));
        assertEquals("\\d{2}", toRegex("hh", locale));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("locales")
    public void dailyPattern(Locale locale) {
        assertEquals("\\d{4}-\\d{2}-\\d{2}", toRegex("yyyy-MM-dd", locale));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("locales")
    public void hourlyPatternWithSeparators(Locale locale) {
        assertEquals("\\d{4}-\\d{2}-\\d{2}_\\d{2}", toRegex("yyyy-MM-dd_HH", locale));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("locales")
    public void dotIsEscaped(Locale locale) {
        String regex = toRegex("yyyy.MM.dd", locale);
        assertEquals("\\d{4}\\.\\d{2}\\.\\d{2}", regex);
        assertTrue("2009.09.03".matches(regex));
        assertFalse("2009x09x03".matches(regex));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("locales")
    public void slashIsKeptAsLiteral(Locale locale) {
        String regex = toRegex("yyyy/MM/dd", locale);
        assertEquals("\\d{4}/\\d{2}/\\d{2}", regex);
        assertTrue("2009/09/03".matches(regex));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("locales")
    public void repeatedLiteralCharacter(Locale locale) {
        String regex = toRegex("yyyy--MM", locale);
        assertEquals("\\d{4}-{2}\\d{2}", regex);
        assertTrue("2009--09".matches(regex));
        assertFalse("2009-09".matches(regex));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("locales")
    public void singleQuotesAreDropped(Locale locale) {
        String regex = toRegex("yyyy-MM-dd'T'HH", locale);
        assertEquals("\\d{4}-\\d{2}-\\d{2}T\\d{2}", regex);
        assertTrue("2009-09-03T21".matches(regex));
    }

    @Test
    public void escapedSingleQuoteIsRejected() {
        assertThrows(IllegalStateException.class, () -> toRegex("yyyy''MM", Locale.US));
    }

    @Test
    public void backslashIsRejected() {
        assertThrows(IllegalStateException.class, () -> toRegex("yyyy\\MM", Locale.US));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("locales")
    public void eraAndGeneralTimeZoneMatchAnything(Locale locale) {
        assertEquals(".*", toRegex("G", locale));
        assertEquals(".*", toRegex("z", locale));
        assertEquals(".*", toRegex("zzzz", locale));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("locales")
    public void rfc822TimeZone(Locale locale) {
        String regex = toRegex("Z", locale);
        assertEquals("(\\+|-)\\d{4}", regex);
        assertTrue("+0200".matches(regex));
        assertTrue("-0500".matches(regex));
        assertFalse("0200".matches(regex));
        assertFalse("+02:00".matches(regex));
    }

    // only locales and forms whose symbols are unlikely to change between JDK versions
    @ParameterizedTest(name = "{0} {1} -> {2}")
    @CsvSource(delimiter = '|', value = {
            // "May" is the shortest, "September" the longest
            "en-US | MMMM | .{3,9}",
            // "Monday", "Friday", "Sunday" are the shortest, "Wednesday" the longest
            "en-US | EEEE | .{6,9}",
            "en-US | EEE  | .{3,3}",
            "en-US | E    | .{3,3}",
            "en-US | MMM  | .{3,3}",
            "en-US | a    | .{2,2}",
            // stand-alone month names
            "en-US | LLL  | .{3,3}",
            "en-US | LLLL | .{3,9}",
            "en-US | LLLLL | .{1,1}",
            // "maj" is the shortest, "październik" the longest stand-alone month
            "pl-PL | LLLL | .{3,11}",
            // "maja" is the shortest, "października" the longest format month
            "pl-PL | MMMM | .{4,12}",
            // "Mai" is the shortest, "September" the longest
            "de-DE | MMMM | .{3,9}",
            // "Montag" is the shortest, "Donnerstag" the longest
            "de-DE | EEEE | .{6,10}",
            // "mai" is the shortest, "septembre" the longest
            "fr-FR | MMMM | .{3,9}",
            // "lundi" is the shortest, "mercredi" the longest
            "fr-FR | EEEE | .{5,8}" })
    public void symbolBasedFields(String languageTag, String datePattern, String expectedRegex) {
        assertEquals(expectedRegex, toRegex(datePattern, Locale.forLanguageTag(languageTag)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("locales")
    public void toRegexIsIdempotent(Locale locale) {
        DatePatternToRegexUtil util = new DatePatternToRegexUtil("yyyy-MMMM-dd'T'HH:mm EEEE a", locale);
        String first = util.toRegex();
        assertEquals(first, util.toRegex());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("locales")
    public void formattedDatesMatchTheirRegex(Locale locale) {
        // format every day of a year, at varying hours, so that all months, days of
        // the week and am/pm markers are exercised. As in CachingDateFormatter, dates
        // are formatted with DateTimeFormatter.
        for (String pattern : ROUND_TRIP_PATTERNS) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern, locale);
            Pattern regex = Pattern.compile(toRegex(pattern, locale));
            ZonedDateTime start = ZonedDateTime.of(2009, 1, 1, 0, 57, 16, 333_000_000, ZoneId.of("Europe/Paris"));
            for (int day = 0; day < 365; day++) {
                ZonedDateTime dateTime = start.plusDays(day).withHour((day * 7) % 24);
                String formatted = dtf.format(dateTime);
                assertTrue(regex.matcher(formatted).matches(), "[" + formatted + "] does not match regex [" + regex
                        + "] for pattern [" + pattern + "] in locale [" + locale + "]");
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("locales")
    public void regexRejectsWronglyShapedInput(Locale locale) {
        String regex = toRegex(CoreConstants.DAILY_DATE_PATTERN, locale);
        assertTrue("2009-09-03".matches(regex));
        assertFalse("2009-9-03".matches(regex));
        assertFalse("09-09-03".matches(regex));
        assertFalse("2009-09-03.log".matches(regex));
        assertFalse("2009_09_03".matches(regex));
    }

    String toRegex(String datePattern, Locale locale) {
        return new DatePatternToRegexUtil(datePattern, locale).toRegex();
    }
}
