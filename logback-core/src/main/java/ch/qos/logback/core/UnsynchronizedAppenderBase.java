/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
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
import ch.qos.logback.core.util.ReentryGuard;
import ch.qos.logback.core.util.ReentryGuardFactory;
import ch.qos.logback.core.util.SimpleTimeBasedGuard;

/**
 * Similar to {@link AppenderBase} except that derived appenders need to handle thread
 * synchronization on their own.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Ralph Goers
 */
abstract public class UnsynchronizedAppenderBase<E> extends ContextAwareBase implements Appender<E> {

    protected volatile boolean started = false;
    /**
     * The guard prevents an appender from repeatedly calling its own doAppend
     * method.
     *
     * @since 1.5.21
     */
    private ReentryGuard  reentryGuard;

    /**
     * Appenders are named.
     */
    protected String name;

    private FilterAttachableImpl<E> fai = new FilterAttachableImpl<E>();

    public String getName() {
        return name;
    }

    private SimpleTimeBasedGuard notStartedGuard = new SimpleTimeBasedGuard();
    private SimpleTimeBasedGuard exceptionGuard = new SimpleTimeBasedGuard();


    public void doAppend(E eventObject) {
        if (!this.started) {

            if (notStartedGuard.allow()) {
                addStatus(new WarnStatus("Attempted to append to non started appender [" + name + "].", this));
            }
            return;
        }

        // prevent re-entry.
        if (reentryGuard.isLocked()) {
            return;
        }

        try {
            reentryGuard.lock();

            if (getFilterChainDecision(eventObject) == FilterReply.DENY) {
                return;
            }

            // ok, we now invoke derived class' implementation of append
            this.append(eventObject);

        } catch (Exception e) {
            if (exceptionGuard.allow()) {
                addError("Appender [" + name + "] failed to append.", e);
            }
        } finally {
            reentryGuard.unlock();
        }
    }

    abstract protected void append(E eventObject);

    /**
     * Set the name of this appender.
     */
    public void setName(String name) {
        this.name = name;
    }

    public void start() {
        this.reentryGuard = buildReentryGuard();
        started = true;
    }

    /**
     * Create a {@link ReentryGuard} instance used by this appender to prevent
     * recursive/re-entrant calls to {@link #doAppend(Object)}.
     *
     * <p>The default implementation returns a no-op guard produced by
     * {@link ReentryGuardFactory#makeGuard(ch.qos.logback.core.util.ReentryGuardFactory.GuardType)}
     * using {@code GuardType.NOP}. Subclasses that require actual re-entry
     * protection (for example using a thread-local or lock-based guard) should
     * override this method to return an appropriate {@link ReentryGuard}
     * implementation.</p>
     *
     * <p>Contract/expectations:
     * <ul>
     *   <li>Called from {@link #start()} to initialize the appender's guard.</li>
     *   <li>Implementations should be lightweight and thread-safe.</li>
     *   <li>Return value must not be {@code null}.</li>
     * </ul>
     * </p>
     *
     * @return a non-null {@link ReentryGuard} used to detect and prevent
     *         re-entrant appends. By default, this is a no-op guard.
     * @since 1.5.21
     */
    protected ReentryGuard buildReentryGuard() {
        return ReentryGuardFactory.makeGuard(ReentryGuardFactory.GuardType.NOP);
    }

    public void stop() {
        started = false;
    }

    public boolean isStarted() {
        return started;
    }

    public String toString() {
        return this.getClass().getName() + "[" + name + "]";
    }

    public void addFilter(Filter<E> newFilter) {
        fai.addFilter(newFilter);
    }

    public void clearAllFilters() {
        fai.clearAllFilters();
    }

    public List<Filter<E>> getCopyOfAttachedFiltersList() {
        return fai.getCopyOfAttachedFiltersList();
    }

    public FilterReply getFilterChainDecision(E event) {
        return fai.getFilterChainDecision(event);
    }
}
