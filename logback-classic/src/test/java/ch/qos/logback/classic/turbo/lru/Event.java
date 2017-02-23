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
package ch.qos.logback.classic.turbo.lru;

public class Event<K> {

    final public boolean put;
    final public K k;

    public Event(boolean put, K k) {
        this.put = put;
        this.k = k;
    }

    public String toString() {
        if (put) {
            return "Event: put, " + k;
        } else {
            return "Event: get, " + k;
        }
    }
}
