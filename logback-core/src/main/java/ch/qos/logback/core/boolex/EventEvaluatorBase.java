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
package ch.qos.logback.core.boolex;

import ch.qos.logback.core.spi.ContextAwareBase;

abstract public class EventEvaluatorBase<E> extends ContextAwareBase implements EventEvaluator<E> {

    String name;
    boolean started;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if (this.name != null) {
            throw new IllegalStateException("name has been already set");
        }
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

}
