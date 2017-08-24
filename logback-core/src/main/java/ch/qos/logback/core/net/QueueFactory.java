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
package ch.qos.logback.core.net;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Factory for {@link java.util.Queue} instances.
 *
 * @author Sebastian Gr&ouml;bler
 */
public class QueueFactory {

    /**
     * Creates a new {@link LinkedBlockingDeque} with the given {@code capacity}.
     * In case the given capacity is smaller than one it will automatically be
     * converted to one.
     *
     * @param capacity the capacity to use for the queue
     * @param <E> the type of elements held in the queue
     * @return a new instance of {@link ArrayBlockingQueue}
     */
    public <E> LinkedBlockingDeque<E> newLinkedBlockingDeque(int capacity) {
        final int actualCapacity = capacity < 1 ? 1 : capacity;
        return new LinkedBlockingDeque<E>(actualCapacity);
    }
}
