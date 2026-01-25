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

import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.FilterAttachable;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * Contract for components responsible for delivering logging events to their
 * final destination (console, file, remote server, etc.).
 *
 * <p>Implementations are typically configured and managed by a LoggerContext.
 * The type parameter E represents the event type the appender consumes (for
 * example a log event object). Implementations should honor lifecycle methods
 * from {@link LifeCycle} and may be {@link ContextAware} and
 * {@link FilterAttachable} to support contextual information and filtering.</p>
 *
 * <p>Concurrency: appenders are generally invoked by multiple threads. Implementations
 * must ensure thread-safety where applicable (for example when writing to shared
 * resources). The {@link #doAppend(Object)} method may be called concurrently.</p>
 *
 * @param <E> the event type accepted by this appender
 */
public interface Appender<E> extends LifeCycle, ContextAware, FilterAttachable<E> {

    /**
     * Get the name of this appender. The name uniquely identifies the appender
     * within its context and is used for configuration and lookup.
     *
     * @return the appender name, or {@code null} if not set
     */
    String getName();

    /**
     * This is where an appender accomplishes its work: format and deliver the
     * provided event to the appender's destination.
     *
     * <p>Implementations should apply any configured filters before outputting
     * the event. Implementations should avoid throwing runtime exceptions;
     * if an error occurs that cannot be handled internally, a {@link LogbackException}
     * (or a subtype) may be thrown to indicate a failure during append.</p>
     *
     * @param event the event to append; may not be {@code null}
     * @throws LogbackException if the append fails in a way that needs to be
     *                          propagated to the caller
     */
    void doAppend(E event) throws LogbackException;

    /**
     * Set the name of this appender. The name is used by other components to
     * identify and reference this appender (for example in configuration or for
     * status messages).
     *
     * @param name the new name for this appender; may be {@code null} to unset
     */
    void setName(String name);

}
