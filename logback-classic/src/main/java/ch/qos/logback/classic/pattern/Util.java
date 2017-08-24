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
package ch.qos.logback.classic.pattern;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Marker;

import ch.qos.logback.classic.spi.ClassPackagingData;

/**
 * 
 * @author Ceki Gulcu
 */
public class Util {

    static Map<String, ClassPackagingData> cache = new HashMap<String, ClassPackagingData>();

    static public boolean match(Marker marker, Marker[] markerArray) {
        if (markerArray == null) {
            throw new IllegalArgumentException("markerArray should not be null");
        }

        // System.out.println("event marker="+marker);

        final int size = markerArray.length;
        for (int i = 0; i < size; i++) {
            // System.out.println("other:"+markerArray[i]);

            if (marker.contains(markerArray[i])) {
                return true;
            }
        }
        return false;
    }

}
