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
package ch.qos.logback.core.util;

/**
 * A strategy for computing a delay.
 *
 * @author Carl Harris
 * @since 1.1.0
 */
public interface DelayStrategy {
    /**
     * The value computed by this {@code DelayStrategy} for the next delay.
     * @return a delay value
     */
    long nextDelay();
}
