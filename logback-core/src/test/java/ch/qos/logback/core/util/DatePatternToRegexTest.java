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

import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.qos.logback.core.CoreConstants;

public class DatePatternToRegexTest {
    static Calendar CAL_2009_08_3_NIGHT = Calendar.getInstance();
    static Calendar CAL_2009_08_3_MORNING = Calendar.getInstance();
    static Locale CZ_LOCALE = new Locale("cs", "CZ");
    static Locale KO_LOCALE = new Locale("ko", "KR");

    @BeforeClass
    public static void setUpCalendars() {
        CAL_2009_08_3_NIGHT.set(2009, 8, 3, 21, 57, 16);
        CAL_2009_08_3_NIGHT.set(Calendar.MILLISECOND, 333);

        CAL_2009_08_3_MORNING.set(2009, 8, 3, 10, 24, 37);
        CAL_2009_08_3_MORNING.set(Calendar.MILLISECOND, 333);
    }

    @Test
    public void ISO8601() {
        doTest(CoreConstants.ISO8601_PATTERN, CAL_2009_08_3_NIGHT);
    }

    @Test
    public void withQuotes() {
        doTest("yyyy-MM-dd'T'HH:mm:ss,SSS", CAL_2009_08_3_NIGHT);

    }

    @Test
    public void month() {
        doTest("yyyy-MMM-dd", CAL_2009_08_3_NIGHT);
        doTest("yyyy-MMM-dd", CAL_2009_08_3_NIGHT, CZ_LOCALE);
        doTest("yyyy-MMM-dd", CAL_2009_08_3_NIGHT, KO_LOCALE);

        doTest("yyyy-MMMM-dd", CAL_2009_08_3_NIGHT);
        doTest("yyyy-MMMM-dd", CAL_2009_08_3_NIGHT, CZ_LOCALE);
        doTest("yyyy-MMMM-dd", CAL_2009_08_3_NIGHT, KO_LOCALE);

    }

    public void monthWithLocal() {

    }

    @Test
    public void dot() {
        doTest("yyyy.MMM.dd", CAL_2009_08_3_NIGHT);
        ;
    }

    @Test
    public void timeZone() {
        doTest("yyyy-MMM-dd HH:mm:ss z", CAL_2009_08_3_NIGHT);
        doTest("yyyy-MMM-dd HH:mm:ss Z", CAL_2009_08_3_NIGHT);
    }

    @Test
    public void dayInWeek() {
        doTest("EE", CAL_2009_08_3_NIGHT);
        doTest("EE", CAL_2009_08_3_NIGHT, CZ_LOCALE);
        doTest("EE", CAL_2009_08_3_NIGHT, KO_LOCALE);

        doTest("EEEE", CAL_2009_08_3_NIGHT);
        doTest("EEEE", CAL_2009_08_3_NIGHT, CZ_LOCALE);
        doTest("EEEE", CAL_2009_08_3_NIGHT, KO_LOCALE);
    }

    @Test
    public void amPm() {
        doTest("yyyy-MM-dd a", CAL_2009_08_3_NIGHT);
        doTest("yyyy-MM-dd a", CAL_2009_08_3_NIGHT, CZ_LOCALE);
        doTest("yyyy-MM-dd a", CAL_2009_08_3_NIGHT, KO_LOCALE);

        doTest("yyyy-MM-dd a", CAL_2009_08_3_MORNING);
        doTest("yyyy-MM-dd a", CAL_2009_08_3_MORNING, CZ_LOCALE);
        doTest("yyyy-MM-dd a", CAL_2009_08_3_MORNING, KO_LOCALE);

    }

    void doTest(String datePattern, Calendar calendar) {
        doTest(datePattern, calendar, null);
    }

    void doTest(String datePattern, Calendar calendar, Locale locale) {
        Locale oldDefaultLocale = Locale.getDefault();
        if (locale != null) {
            Locale.setDefault(locale);
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
            DateTokenConverter<?> dtc = makeDTC(datePattern);
            verify(sdf, calendar, dtc);
        } finally {
            if (locale != null)
                Locale.setDefault(oldDefaultLocale);

        }
    }

    Locale locale;

    // void doTest(String datePattern, Calendar calendar) {
    // doTest(datePattern, calendar, false);
    // }

    void verify(SimpleDateFormat sdf, Calendar calendar, DateTokenConverter<?> dtc) {
        String expected = sdf.format(calendar.getTime());
        // if (slashified) {
        // expected = expected.replace('\\', '/');
        // }
        String regex = dtc.toRegex();
        // System.out.println("expected="+expected);
        // System.out.println(regex);
        assertTrue("[" + expected + "] does not match regex [" + regex + "]", expected.matches(regex));
    }

    private DateTokenConverter<?> makeDTC(String datePattern) {
        DateTokenConverter<?> dtc = new DateTokenConverter<Object>();
        List<String> optionList = new ArrayList<String>();
        optionList.add(datePattern);
        dtc.setOptionList(optionList);
        dtc.start();
        return dtc;
    }
}
