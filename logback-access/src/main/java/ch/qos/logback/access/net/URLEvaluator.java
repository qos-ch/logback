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
package ch.qos.logback.access.net;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

public class URLEvaluator extends ContextAwareBase implements EventEvaluator<IAccessEvent>, LifeCycle {

    boolean started;
    String name;
    private final List<String> URLList = new ArrayList<>();

    public void addURL(final String url) {
        URLList.add(url);
    }

    @Override
    public void start() {
        if (URLList.isEmpty()) {
            addWarn("No URL was given to URLEvaluator");
        } else {
            started = true;
        }
    }

    @Override
    public boolean evaluate(final IAccessEvent event) throws NullPointerException, EvaluationException {
        return URLList.stream().anyMatch(expected -> event.getRequestURL().contains(expected));
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
    public void stop() {
        started = false;
    }
}
