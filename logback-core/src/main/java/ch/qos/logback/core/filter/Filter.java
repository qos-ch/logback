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
package ch.qos.logback.core.filter;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * Users should extend this class to implement customized event filtering.
 * 
 * <p>We suggest that you first try to use the built-in rules before rushing to
 * write your own custom filters.
 * 
 * <p>For more information about filters, please refer to the online manual at
 * http://logback.qos.ch/manual/filters.html
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public abstract class Filter<E> extends ContextAwareBase implements LifeCycle {

    private String name;

    boolean start = false;

    public void start() {
        this.start = true;
    }

    public boolean isStarted() {
        return this.start;
    }

    public void stop() {
        this.start = false;
    }

    /**
     * If the decision is <code>{@link FilterReply#DENY}</code>, then the event will be
     * dropped. If the decision is <code>{@link FilterReply#NEUTRAL}</code>, then the next
     * filter, if any, will be invoked. If the decision is
     * <code>{@link FilterReply#ACCEPT}</code> then the event will be logged without
     * consulting with other filters in the chain.
     * 
     * @param event
     *                The event to decide upon.
     */
    public abstract FilterReply decide(E event);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
