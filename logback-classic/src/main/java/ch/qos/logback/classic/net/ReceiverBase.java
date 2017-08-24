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

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * An abstract base for components that receive logging events from a remote
 * peer and log according to local policy
 *
 * @author Carl Harris
 */
public abstract class ReceiverBase extends ContextAwareBase implements LifeCycle {

    private boolean started;

    /**
     * {@inheritDoc}
     */
    public final void start() {
        if (isStarted())
            return;
        if (getContext() == null) {
            throw new IllegalStateException("context not set");
        }
        if (shouldStart()) {
            getContext().getScheduledExecutorService().execute(getRunnableTask());
            started = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void stop() {
        if (!isStarted())
            return;
        try {
            onStop();
        } catch (RuntimeException ex) {
            addError("on stop: " + ex, ex);
        }
        started = false;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isStarted() {
        return started;
    }

    /**
     * Determines whether this receiver should start.
     * <p>
     * Subclasses will implement this method to do any subclass-specific
     * validation.  The subclass's {@link #getRunnableTask()} method will be 
     * invoked (and the task returned will be submitted to the executor)
     * if and only if this method returns {@code true} 
     * @return flag indicating whether this receiver should start
     */
    protected abstract boolean shouldStart();

    /**
     * Allows a subclass to participate in receiver shutdown.
     */
    protected abstract void onStop();

    /**
     * Provides the runnable task this receiver will execute.
     * @return runnable task
     */
    protected abstract Runnable getRunnableTask();

}
