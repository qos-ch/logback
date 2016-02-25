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
package ch.qos.logback.core.status;

import java.util.List;

/**
 * Internal error messages (statii) are managed by instances of this interface.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public interface StatusManager {

    /**
     * Add a new status message.
     * 
     * @param status
     */
    void add(Status status);

    /**
     * Obtain a copy of the status list maintained by this StatusManager.
     * 
     * @return
     */
    List<Status> getCopyOfStatusList();

    /**
     * Return the highest level of all the statii.
     * 
     * @return
     */
    // int getLevel();

    /**
     * Return the number of status entries.
     * 
     * @return
     */
    int getCount();

    /**
     * Add a status listener.
     * @param listener
     */

    /**
     * Add a status listener. The StatusManager may decide to skip installation if an 
     * earlier instance was already installed.
     * 
     * @param listener
     * @return true if actually added, false if skipped
     */
    boolean add(StatusListener listener);

    /**);
     * Remove a status listener.
     * 
     * @param listener
     */
    void remove(StatusListener listener);

    /**
     * Clear the list of status messages.
     */
    void clear();

    /**
     * Obtain a copy of the status listener list maintained by this StatusManager
     * 
     * @return
     */
    List<StatusListener> getCopyOfStatusListenerList();

}
