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

package ch.qos.logback.classic.boolex;

import org.slf4j.Marker;

import java.util.List;

/**
 * A helper class to be used in conjunction with <code>JaninoEventEvaluator</code> (removed in 1.5.13).
 *
 * @since 1.5.4
 */
public class MarkerList {

    List<Marker> markers;

    public MarkerList(List<Marker> markers) {
        this.markers = markers;
    }

    /**
     * Check whether this list contains a given marker.
     *
     * @param markerName
     * @return
     */
    public boolean contains(String markerName) {
        if(markerName == null || markerName.trim().length() == 0)
            return false;

        if(markers == null || markers.isEmpty())
            return false;

        final boolean result = markers.stream().anyMatch( m -> m.contains(markerName));
        return  result;
    }

    /**
     * Return the first marker on the list, can be null.
     *
     *
     * @return the first marker on the list, can be null
     */
    public Marker getFirstMarker() {
        if(markers == null || markers.isEmpty()) {
            return null;
        } else {
            return markers.get(0);
        }
    }
}
