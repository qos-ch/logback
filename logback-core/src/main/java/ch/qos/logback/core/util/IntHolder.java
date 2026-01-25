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

package ch.qos.logback.core.util;

/**
 * A simple mutable holder for an integer value, providing basic operations
 * like incrementing, setting, and retrieving the value. This class is not
 * thread-safe and should be used in single-threaded contexts or with external
 * synchronization.
 *
 * @since 1.5.24
 */
public class IntHolder {
    public int value;

    /**
     * Constructs an IntHolder with the specified initial value.
     *
     * @param value the initial integer value to hold
     */
    public IntHolder(int value) {
        this.value = value;
    }

    /**
     * Increments the held value by 1.
     */
    public void inc() {
        value++;
    }

    /**
     * Sets the held value to the specified new value.
     *
     * @param newValue the new integer value to set
     */
    public void set(int newValue) {
        value = newValue;
    }

    /**
     * Returns the current held value.
     *
     * @return the current integer value
     */
    public int get(){
        return value;
    }
}
