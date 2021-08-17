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
package ch.qos.logback.core.rolling.helper;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.util.CachingDateFormatter;
import ch.qos.logback.core.util.DatePatternToRegexUtil;

/**
 * Returns a date formatted by SimpleDateFormatter.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class DateTokenConverter<E> extends DynamicConverter<E> implements MonoTypedConverter {

    /**
     * The conversion word/character with which this converter is registered.
     */
    public final static String CONVERTER_KEY = "d";
    public final static String AUXILIARY_TOKEN = "AUX";
    public static final String DEFAULT_DATE_PATTERN = CoreConstants.DAILY_DATE_PATTERN;

    private String datePattern;
    private ZoneId zoneId;
    private CachingDateFormatter cdf;
    // is this token converter primary or auxiliary? Only the primary converter
    // determines the rolling period
    private boolean primary = true;

    public void start() {
        this.datePattern = getFirstOption();
        if (this.datePattern == null) {
            this.datePattern = DEFAULT_DATE_PATTERN;
        }

        final List<String> optionList = getOptionList();
        if (optionList != null) {
            for (int optionIndex = 1; optionIndex < optionList.size(); optionIndex++) {
                String option = optionList.get(optionIndex);
                if (AUXILIARY_TOKEN.equalsIgnoreCase(option)) {
                    primary = false;
                } else {
                	zoneId = ZoneId.of(option);
                }
            }
        }

        cdf = new CachingDateFormatter(datePattern, zoneId);
    }

    public String convert(Date date) {
        return cdf.format(date.getTime());
    }

    public String convert(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("Null argument forbidden");
        }
        if (o instanceof Date) {
            return convert((Date) o);
        }
        throw new IllegalArgumentException("Cannot convert " + o + " of type" + o.getClass().getName());
    }

    /**
     * Return the date pattern.
     */
    public String getDatePattern() {
        return datePattern;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public boolean isApplicable(Object o) {
        return (o instanceof Date);
    }

    public String toRegex() {
        DatePatternToRegexUtil datePatternToRegexUtil = new DatePatternToRegexUtil(datePattern);
        return datePatternToRegexUtil.toRegex();
    }

    public boolean isPrimary() {
        return primary;
    }
}
