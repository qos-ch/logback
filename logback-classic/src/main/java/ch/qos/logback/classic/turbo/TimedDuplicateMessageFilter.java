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
package ch.qos.logback.classic.turbo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.util.Duration;
import org.slf4j.Marker;

/**
 * 
 * See {@link http://logback.qos.ch/manual/filters.html#TimedDuplicateMessageFilter}
 * for details.
 * 
 * @author Ceki Gulcu
 * 
 */
public class TimedDuplicateMessageFilter extends TurboFilter {

    /**
     * The default cache size.
     */
    public static final int DEFAULT_CACHE_SIZE = 100;
    /**
     * The default number of allows repetitions.
     */
    public static final int DEFAULT_ALLOWED_REPETITIONS = 5;
    /**
     * The default duration log messages expire after.
     */
    public static final Duration DEFAULT_SUPPRESSION_TIME = new Duration(65000);

    public int allowedRepetitions = DEFAULT_ALLOWED_REPETITIONS;
    public int cacheSize = DEFAULT_CACHE_SIZE;
    public Duration suppressionTime = DEFAULT_SUPPRESSION_TIME;

    private ExpiringLRUMessageCache msgCache;

    @Override
    public void start() {
        msgCache = new ExpiringLRUMessageCache(cacheSize, suppressionTime);
        super.start();
    }

    @Override
    public void stop() {
        msgCache.clear();
        msgCache = null;
        super.stop();
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        int count = msgCache.getMessageCountAndThenIncrement(format);
        if (count <= allowedRepetitions) {
            return FilterReply.NEUTRAL;
        } else {
            return FilterReply.DENY;
        }
    }

    public int getAllowedRepetitions() {
        return allowedRepetitions;
    }

    /**
     * The allowed number of repetitions before
     * 
     * @param allowedRepetitions
     */
    public void setAllowedRepetitions(int allowedRepetitions) {
        this.allowedRepetitions = allowedRepetitions;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public Duration getSuppressionTime() {
        return suppressionTime;
    }

    /**
     * The allowed duration log messages expire after
     *
     * @param suppressionTime
     */
    public void setSuppressionTime(Duration suppressionTime) {
        this.suppressionTime = suppressionTime;
    }
}
