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
package ch.qos.logback.core;

import java.util.List;

import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterAttachableImpl;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.WarnStatus;

/**
 * Sets a skeleton implementation for appenders.
 *
 * <p> For more information about this appender, please refer to the online
 * manual at http://logback.qos.ch/manual/appenders.html#AppenderBase
 *
 * @author Ceki G&uuml;lc&uuml;
 */
abstract public class AppenderBase<E> extends ContextAwareBase implements Appender<E> {

    protected volatile boolean started = false;

    /**
     * The guard prevents an appender from repeatedly calling its own doAppend
     * method.
     */
    private boolean guard = false;

    /**
     * Appenders are named.
     */
    protected String name;

    private FilterAttachableImpl<E> fai = new FilterAttachableImpl<E>();

    @Override
    public String getName() {
        return name;
    }

    private int statusRepeatCount = 0;
    private int exceptionCount = 0;

    static final int ALLOWED_REPEATS = 5;

    @Override
    public synchronized void doAppend(E eventObject) {
        // WARNING: The guard check MUST be the first statement in the
        // doAppend() method.

        // prevent re-entry.
        if (guard) {
            return;
        }

        try {
            guard = true;

            if (!this.started) {
                if (statusRepeatCount++ < ALLOWED_REPEATS) {
                    addStatus(new WarnStatus("Attempted to append to non started appender [" + name + "].", this));
                }
                return;
            }

            if (getFilterChainDecision(eventObject) == FilterReply.DENY) {
                return;
            }

            // ok, we now invoke derived class' implementation of append
            this.append(eventObject);

        } catch (Exception e) {
            if (exceptionCount++ < ALLOWED_REPEATS) {
                addError("Appender [" + name + "] failed to append.", e);
            }
        } finally {
            guard = false;
        }
    }

    abstract protected void append(E eventObject);

    /**
     * Set the name of this appender.
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + name + "]";
    }

    @Override
    public void addFilter(Filter<E> newFilter) {
        fai.addFilter(newFilter);
    }

    @Override
    public void clearAllFilters() {
        fai.clearAllFilters();
    }

    @Override
    public List<Filter<E>> getCopyOfAttachedFiltersList() {
        return fai.getCopyOfAttachedFiltersList();
    }

    @Override
    public FilterReply getFilterChainDecision(E event) {
        return fai.getFilterChainDecision(event);
    }

}
