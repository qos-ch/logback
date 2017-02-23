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
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * TurboFilter is a specialized filter with a decide method that takes a bunch 
 * of parameters instead of a single event object. The latter is cleaner but 
 * the first is much more performant.
 * <p>
 * For more information about turbo filters, please refer to the online manual at
 * http://logback.qos.ch/manual/filters.html#TurboFilter
 * 
 * @author Ceki Gulcu
 */
public abstract class TurboFilter extends ContextAwareBase implements LifeCycle {

    private String name;
    boolean start = false;

    /**
     * Make a decision based on the multiple parameters passed as arguments.
     * The returned value should be one of <code>{@link FilterReply#DENY}</code>, 
     * <code>{@link FilterReply#NEUTRAL}</code>, or <code>{@link FilterReply#ACCEPT}</code>.
    
     * @param marker
     * @param logger
     * @param level
     * @param format
     * @param params
     * @param t
     * @return
     */
    public abstract FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t);

    public void start() {
        this.start = true;
    }

    public boolean isStarted() {
        return this.start;
    }

    public void stop() {
        this.start = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
