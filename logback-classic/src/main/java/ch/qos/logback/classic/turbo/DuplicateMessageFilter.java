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

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.FilterReply;

/**
 * 
 * See {@link http://logback.qos.ch/manual/filters.html#DuplicateMessageFilter}
 * for details.
 * 
 * @author Ceki Gulcu
 * 
 */
public class DuplicateMessageFilter extends TurboFilter {

    /**
     * The default cache size.
     */
    public static final int DEFAULT_CACHE_SIZE = 100;
    /**
     * The default number of allows repetitions.
     */
    public static final int DEFAULT_ALLOWED_REPETITIONS = 5;

    public int allowedRepetitions = DEFAULT_ALLOWED_REPETITIONS;
    public int cacheSize = DEFAULT_CACHE_SIZE;

    private LRUMessageCache msgCache;

    @Override
    public void start() {
        msgCache = new LRUMessageCache(cacheSize);
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

}
