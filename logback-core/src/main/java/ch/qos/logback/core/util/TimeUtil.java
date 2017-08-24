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

import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

    public static long computeStartOfNextSecond(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.SECOND, 1);
        return cal.getTime().getTime();
    }

    public static long computeStartOfNextMinute(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.MINUTE, 1);
        return cal.getTime().getTime();
    }

    public static long computeStartOfNextHour(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.add(Calendar.HOUR, 1);
        return cal.getTime().getTime();
    }

    public static long computeStartOfNextDay(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));

        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return cal.getTime().getTime();
    }

    public static long computeStartOfNextWeek(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));

        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        return cal.getTime().getTime();
    }

    public static long computeStartOfNextMonth(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));

        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MONTH, 1);
        return cal.getTime().getTime();
    }

}
