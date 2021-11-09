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
package ch.qos.logback.classic.net;

import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * A simple EventEvaluator implementation that triggers email transmission after
 * a given number of events occur, regardless of event level.
 *
 * <p>By default, the limit is 1024.
 */
public class CounterBasedEvaluator extends ContextAwareBase implements EventEvaluator<Object> {

    static int DEFAULT_LIMIT = 1024;
    int limit = DEFAULT_LIMIT;
    int counter = 0;
    String name;
    boolean started;

    @Override
    public boolean evaluate(final Object event) throws NullPointerException, EvaluationException {
        counter++;

        if (counter == limit) {
            counter = 0;
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }

}
