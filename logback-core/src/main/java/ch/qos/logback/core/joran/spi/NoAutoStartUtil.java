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
package ch.qos.logback.core.joran.spi;

public class NoAutoStartUtil {

    /**
     * Returns true if the class of the object 'o' passed as parameter is *not*
     * marked with the NoAutoStart annotation. Return true otherwise.
     * 
     * @param o
     * @return true for classes not marked with the NoAutoStart annotation
     */
    static public boolean notMarkedWithNoAutoStart(Object o) {
        if (o == null) {
            return false;
        }
        Class<?> clazz = o.getClass();
        NoAutoStart a = clazz.getAnnotation(NoAutoStart.class);
        return a == null;
    }

}
