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
package ch.qos.logback.access.pattern;

import java.time.ZoneId;
import java.util.List;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.util.CachingDateFormatter;

public class DateConverter extends AccessConverter {

    CachingDateFormatter cachingDateFormatter = null;
 
    @Override
    public void start() {
 
        String datePattern = getFirstOption();
        if (datePattern == null) {
            datePattern = CoreConstants.CLF_DATE_PATTERN;
        }

        if (datePattern.equals(CoreConstants.ISO8601_STR)) {
            datePattern = CoreConstants.ISO8601_PATTERN;
        }
        ZoneId zoneId = null;
        List<String> optionList = getOptionList();

        // if the option list contains a TZ option, then set it.
        if (optionList != null && optionList.size() > 1) {
         	String zoneIdString = (String) optionList.get(1);
        	 zoneId = ZoneId.of(zoneIdString);
        }
        
        try {
            cachingDateFormatter = new CachingDateFormatter(datePattern, zoneId);
            // maximumCacheValidity = CachedDateFormat.getMaximumCacheValidity(pattern);
        } catch (IllegalArgumentException e) {
            addWarn("Could not instantiate SimpleDateFormat with pattern " + datePattern, e);
            addWarn("Defaulting to  " + CoreConstants.CLF_DATE_PATTERN);
            cachingDateFormatter = new CachingDateFormatter(CoreConstants.CLF_DATE_PATTERN, zoneId);
        }


   
    }

    @Override
    public String convert(IAccessEvent accessEvent) {
        long timestamp = accessEvent.getTimeStamp();
        return cachingDateFormatter.format(timestamp);
    }
}
