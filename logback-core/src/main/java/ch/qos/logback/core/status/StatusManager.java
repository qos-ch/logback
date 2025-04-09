/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.status;

import java.util.List;

/**
 * A component which accepts status events and notifies registered listeners.
 * Maintains event statistics, an event buffer, and a listener registry.
 *
 * @author Ceki Gülcü
 * @author Mark Chesney
 */
public interface StatusManager {

    /**
     * Notifies registered listeners of the specified status event and adds it to
     * the end of the event buffer.
     *
     * @param status a status event
     */
    void add(Status status);

    /**
     * Returns a point-in-time snapshot of the event buffer.
     *
     * @return a snapshot of the event buffer
     */
    List<Status> getCopyOfStatusList();

    /**
     * Returns the highest level of statuses seen since instantiation.
     *
     * @return the highest level of statuses seen
     */
    int getLevel();

    /**
     * Returns the number of events processed since instantiation or the last reset.
     *
     * @return the number of events processed
     */
    int getCount();

    /**
     * Registers the specified listener.
     * <p>
     * Returns {@code true} if the registered listeners changed, and {@code false}
     * if the specified listener is already registered, and the implementation does
     * not permit duplicates.
     *
     * @param listener the listener to register
     * @return {@code true} if the registered listeners changed, {@code false}
     *         otherwise
     */
    boolean add(StatusListener listener);

    /**
     * Deregisters the specified listener, if registered.
     * <p>
     * If the implementation permits duplicates, only the first occurrence is
     * deregistered.
     *
     * @param listener the listener to deregister
     */
    void remove(StatusListener listener);

    /**
     * Resets event statistics and empties the event buffer.
     */
    void clear();

    /**
     * Returns a point-in-time snapshot of the registered listeners.
     *
     * @return a snapshot of the registered listeners
     */
    List<StatusListener> getCopyOfStatusListenerList();

}
