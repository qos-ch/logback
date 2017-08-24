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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LevelToSyslogSeverity;
import ch.qos.logback.core.net.SyslogAppenderBase;

public class SyslogStartConverter extends ClassicConverter {

    long lastTimestamp = -1;
    String timesmapStr = null;
    SimpleDateFormat simpleMonthFormat;
    SimpleDateFormat simpleTimeFormat;
    private final Calendar calendar = Calendar.getInstance(Locale.US);

    String localHostName;
    int facility;

    public void start() {
        int errorCount = 0;

        String facilityStr = getFirstOption();
        if (facilityStr == null) {
            addError("was expecting a facility string as an option");
            return;
        }

        facility = SyslogAppenderBase.facilityStringToint(facilityStr);

        localHostName = getLocalHostname();
        try {
            // hours should be in 0-23, see also http://jira.qos.ch/browse/LBCLASSIC-48
            simpleMonthFormat = new SimpleDateFormat("MMM", Locale.US);
            simpleTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        } catch (IllegalArgumentException e) {
            addError("Could not instantiate SimpleDateFormat", e);
            errorCount++;
        }

        if (errorCount == 0) {
            super.start();
        }
    }

    public String convert(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();

        int pri = facility + LevelToSyslogSeverity.convert(event);

        sb.append("<");
        sb.append(pri);
        sb.append(">");
        sb.append(computeTimeStampString(event.getTimeStamp()));
        sb.append(' ');
        sb.append(localHostName);
        sb.append(' ');

        return sb.toString();
    }

    /**
     * This method gets the network name of the machine we are running on.
     * Returns "UNKNOWN_LOCALHOST" in the unlikely case where the host name 
     * cannot be found.
     * @return String the name of the local host
     */
    public String getLocalHostname() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (UnknownHostException uhe) {
            addError("Could not determine local host name", uhe);
            return "UNKNOWN_LOCALHOST";
        }
    }

    String computeTimeStampString(long now) {
        synchronized (this) {
            // Since the formatted output is only precise to the second, we can use the same cached string if the
            // current
            // second is the same (stripping off the milliseconds).
            if ((now / 1000) != lastTimestamp) {
                lastTimestamp = now / 1000;
                Date nowDate = new Date(now);
                calendar.setTime(nowDate);
                timesmapStr = String.format("%s %2d %s", simpleMonthFormat.format(nowDate), calendar.get(Calendar.DAY_OF_MONTH),
                                simpleTimeFormat.format(nowDate));
            }
            return timesmapStr;
        }
    }
}
