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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DurationTest {

    static long HOURS_CO = 60 * 60;
    static long DAYS_CO = 24 * 60 * 60;

    @Test
    public void test() {
        {
            Duration d = Duration.valueOf("12");
            assertEquals(12, d.getMilliseconds());
        }

        {
            Duration d = Duration.valueOf("159 milli");
            assertEquals(159, d.getMilliseconds());
        }

        {
            Duration d = Duration.valueOf("15 millis");
            assertEquals(15, d.getMilliseconds());
        }

        {
            Duration d = Duration.valueOf("8 milliseconds");
            assertEquals(8, d.getMilliseconds());
        }

        {
            Duration d = Duration.valueOf("10.7 millisecond");
            assertEquals(10, d.getMilliseconds());
        }

        {
            Duration d = Duration.valueOf("10 SECOnds");
            assertEquals(10 * 1000, d.getMilliseconds());
        }

        {
            Duration d = Duration.valueOf("12seconde");
            assertEquals(12 * 1000, d.getMilliseconds());
        }

        {
            Duration d = Duration.valueOf("14 SECONDES");
            assertEquals(14 * 1000, d.getMilliseconds());
        }

        {
            Duration d = Duration.valueOf("12second");
            assertEquals(12 * 1000, d.getMilliseconds());
        }

        {
            Duration d = Duration.valueOf("10.7 seconds");
            assertEquals(10700, d.getMilliseconds());
        }

        {
            Duration d = Duration.valueOf("1 minute");
            assertEquals(1000 * 60, d.getMilliseconds());
        }

        {
            Duration d = Duration.valueOf("2.2 minutes");
            assertEquals(2200 * 60, d.getMilliseconds());
        }

        {
            Duration d = Duration.valueOf("1 hour");
            assertEquals(1000 * HOURS_CO, d.getMilliseconds());
        }

        {
            Duration d = Duration.valueOf("4.2 hours");
            assertEquals(4200 * HOURS_CO, d.getMilliseconds());
        }

        {
            Duration d = Duration.valueOf("5 days");
            assertEquals(5000 * DAYS_CO, d.getMilliseconds());
        }
    }
}
