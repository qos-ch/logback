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
package ch.qos.logback.core.hook;

import ch.qos.logback.core.util.Duration;

/**
 * ShutdownHook implementation that <b>stops</b> the Logback context after a specified
 * delay.  The default delay is 0 ms (zero).
 * 
 * <p>Stopping the logback context
 *
 * @author Mike Reinhold
 */
public class DefaultShutdownHook extends ShutdownHookBase {
    /**
     * The default is no delay before shutdown.
     */
    public static final Duration DEFAULT_DELAY = Duration.buildByMilliseconds(0);

    /**
     * The delay in milliseconds before the ShutdownHook stops the logback context
     */
    private Duration delay = DEFAULT_DELAY;

    public DefaultShutdownHook() {
    }

    public Duration getDelay() {
        return delay;
    }

    /**
     * The duration to wait before shutting down the current logback context.
     *
     * @param delay
     */
    public void setDelay(Duration delay) {
        this.delay = delay;
    }

    public void run() {
        if (delay.getMilliseconds() > 0) {
            addInfo("Sleeping for " + delay);
            try {
                Thread.sleep(delay.getMilliseconds());
            } catch (InterruptedException e) {
            }
        }
        super.stop();
    }
}
