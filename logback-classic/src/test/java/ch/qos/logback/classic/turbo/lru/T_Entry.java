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

public class T_Entry<K> implements Comparable {

    K k;
    long sequenceNumber;

    T_Entry(K k, long sn) {
        this.k = k;
        this.sequenceNumber = sn;
    }

    public int compareTo(Object o) {
        if (!(o instanceof T_Entry)) {
            throw new IllegalArgumentException("arguments must be of type " + T_Entry.class);
        }

        T_Entry other = (T_Entry) o;
        if (sequenceNumber > other.sequenceNumber) {
            return 1;
        }
        if (sequenceNumber == other.sequenceNumber) {
            return 0;
        }
        return -1;
    }

    @Override
    public String toString() {
        return "(" + k + "," + sequenceNumber + ")";
        // return "("+k+")";
    }
}
