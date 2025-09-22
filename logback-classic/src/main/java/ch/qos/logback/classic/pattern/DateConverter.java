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

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.util.CachingDateFormatter;

public class DateConverter extends ClassicConverter {

    CachingDateFormatter cachingDateFormatter = null;
    boolean isUnixSeconds = false;
    boolean isUnixMillis = false;

    public void start() {

        String datePattern = getFirstOption();

        if (datePattern == null) {
            datePattern = CoreConstants.ISO8601_PATTERN;
        } else if (datePattern.equals(CoreConstants.ISO8601_STR)) {
            datePattern = CoreConstants.ISO8601_PATTERN;
        }  else if (datePattern.equals(CoreConstants.STRICT_STR)) {
            datePattern = CoreConstants.STRICT_ISO8601_PATTERN;
        } else if (datePattern.equals("EPOCH_SECONDS")) {
            isUnixSeconds = true;
            super.start();
            return;
        } else if (datePattern.equals("EPOCH_MILLIS")) {
            isUnixMillis = true;
        }

        if (isUnixSeconds || isUnixMillis) {
            super.start();
            return;
        }

        List<String> optionList = getOptionList();
        ZoneId zoneId = null;
        // if the option list contains a TZ option, then set it.
        if (optionList != null && optionList.size() > 1) {
            String zoneIdString = (String) optionList.get(1);
            zoneId = ZoneId.of(zoneIdString);
            addInfo("Setting zoneId to \""+zoneId+"\"");
        }

        Locale locale = null;
        if (optionList != null && optionList.size() > 2) {
            String localeIdStr = (String) optionList.get(2);
            locale = Locale.forLanguageTag(localeIdStr);
            addInfo("Setting locale to \""+locale+"\"");
        }
        try {
            // if zoneId is null, the CachingDateFormatter will use the ZoneId.systemDefault()
            // if locale is null, the CachingDateFormatter will use the Locale.getDefault()
            cachingDateFormatter = new CachingDateFormatter(datePattern, zoneId, locale);
        } catch (IllegalArgumentException e) {
            addWarn("Could not instantiate SimpleDateFormat with pattern " + datePattern, e);
            // default to the ISO8601 format
            cachingDateFormatter = new CachingDateFormatter(CoreConstants.ISO8601_PATTERN, zoneId);
        }

        super.start();
    }

    public String convert(ILoggingEvent le) {
        long timestamp = le.getTimeStamp();
        if (isUnixSeconds) {
            return Long.toString(timestamp / 1000);
        } else if (isUnixMillis) {
            return Long.toString(timestamp);
        } else {
            return cachingDateFormatter.format(timestamp);
        }
    }
}
