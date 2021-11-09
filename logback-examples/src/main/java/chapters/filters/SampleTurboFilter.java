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
package chapters.filters;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

public class SampleTurboFilter extends TurboFilter {

    String marker;
    Marker markerToAccept;

    @Override
    public FilterReply decide(final Marker marker, final Logger logger, final Level level, final String format, final Object[] params, final Throwable t) {

        if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }

        if (markerToAccept.equals(marker)) {
            return FilterReply.ACCEPT;
        }
        return FilterReply.NEUTRAL;
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(final String markerStr) {
        marker = markerStr;
    }

    @Override
    public void start() {
        if (marker != null && marker.trim().length() > 0) {
            markerToAccept = MarkerFactory.getMarker(marker);
            super.start();
        }
    }
}
