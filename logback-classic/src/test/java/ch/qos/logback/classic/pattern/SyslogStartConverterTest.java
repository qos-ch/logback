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
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class SyslogStartConverterTest {

    private LoggerContext lc;
    private SyslogStartConverter converter;
    private final String HOSTNAME = findHostname();
    private final Calendar calendar = Calendar.getInstance(Locale.US);

    @Before
    public void setUp() throws Exception {
        lc = new LoggerContext();
        converter = new SyslogStartConverter();
        converter.setOptionList(Arrays.asList("local7"));
        converter.start();
    }

    @After
    public void tearDown() throws Exception {
        lc = null;
        converter.stop();
        converter = null;
    }

    @Test
    public void datesLessThanTen() {
        // RFC 3164, section 4.1.2:
        // If the day of the month is less than 10, then it MUST be represented as
        // a space and then the number. For example, the 7th day of August would be
        // represented as "Aug  7", with two spaces between the "g" and the "7".
        LoggingEvent le = createLoggingEvent();
        calendar.set(2012, Calendar.AUGUST, 7, 13, 15, 0);
        le.setTimeStamp(calendar.getTimeInMillis());
        assertEquals("<191>Aug  7 13:15:00 " + HOSTNAME + " ", converter.convert(le));
    }

    @Test
    public void datesGreaterThanTen() {
        LoggingEvent le = createLoggingEvent();
        calendar.set(2012, Calendar.OCTOBER, 11, 22, 14, 15);
        le.setTimeStamp(calendar.getTimeInMillis());
        assertEquals("<191>Oct 11 22:14:15 " + HOSTNAME + " ", converter.convert(le));
    }

    @Test
    public void multipleConversions() {
        LoggingEvent le = createLoggingEvent();
        calendar.set(2012, Calendar.OCTOBER, 11, 22, 14, 15);
        le.setTimeStamp(calendar.getTimeInMillis());
        assertEquals("<191>Oct 11 22:14:15 " + HOSTNAME + " ", converter.convert(le));
        assertEquals("<191>Oct 11 22:14:15 " + HOSTNAME + " ", converter.convert(le));

        calendar.set(2012, Calendar.OCTOBER, 11, 22, 14, 16);
        le.setTimeStamp(calendar.getTimeInMillis());
        assertEquals("<191>Oct 11 22:14:16 " + HOSTNAME + " ", converter.convert(le));
    }

    @Test
    public void ignoreDefaultLocale() {
        Locale originalDefaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.TRADITIONAL_CHINESE);

        try {
            converter.start();

            LoggingEvent le = createLoggingEvent();
            calendar.set(2012, Calendar.OCTOBER, 11, 22, 14, 15);
            le.setTimeStamp(calendar.getTimeInMillis());
            String result = converter.convert(le);
            assertEquals("<191>Oct 11 22:14:15 " + HOSTNAME + " ", result);
        } finally {
            Locale.setDefault(originalDefaultLocale);
        }
    }

    @Test
    @Ignore
    public void hostnameShouldNotIncludeDomain() throws Exception {
        // RFC 3164, section 4.1.2:
        // The Domain Name MUST NOT be included in the HOSTNAME field.
        String host = HOSTNAME;
        final int firstPeriod = host.indexOf(".");
        if (firstPeriod != -1) {
            host = host.substring(0, firstPeriod);
        }
        LoggingEvent le = createLoggingEvent();
        calendar.set(2012, Calendar.OCTOBER, 11, 22, 14, 15);
        le.setTimeStamp(calendar.getTimeInMillis());
        assertEquals("<191>Oct 11 22:14:15 " + host + " ", converter.convert(le));
    }

    private LoggingEvent createLoggingEvent() {
        return new LoggingEvent(this.getClass().getName(), lc.getLogger(Logger.ROOT_LOGGER_NAME), Level.DEBUG, "test message", null, null);
    }

    private static String findHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "UNKNOWN_LOCALHOST";
        }
    }
}
