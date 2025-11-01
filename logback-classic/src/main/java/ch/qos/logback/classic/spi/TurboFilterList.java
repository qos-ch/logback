/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.spi;

import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Implementation of TurboFilterAttachable.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
final public class TurboFilterList extends CopyOnWriteArrayList<TurboFilter> {

    private static final long serialVersionUID = 1L;

    /**
     * Loop through the filters in the chain. As soon as a filter decides on ACCEPT
     * or DENY, then that value is returned. If all turbo filters return NEUTRAL,
     * then NEUTRAL is returned.
     */
    public FilterReply getTurboFilterChainDecision(final Marker marker, final Logger logger, final Level level,
                                                   final String format, final Object[] params, final Throwable t) {

        final int size = size();
        // caller may have already performed this check, but we do it here as well to be sure
        if (size == 0) {
            return FilterReply.NEUTRAL;
        }

        if (size == 1) {
            try {
                TurboFilter tf = get(0);
                return tf.decide(marker, logger, level, format, params, t);
            } catch (IndexOutOfBoundsException iobe) {
                // concurrent modification detected, fall through to the general case
                return FilterReply.NEUTRAL;
            }
        }


        for (TurboFilter tf : this) {
            final FilterReply r = tf.decide(marker, logger, level, format, params, t);
            if (r == FilterReply.DENY || r == FilterReply.ACCEPT) {
                return r;
            }
        }

        return FilterReply.NEUTRAL;
    }


    /**
     * Loop through the filters in the chain. As soon as a filter decides on ACCEPT
     * or DENY, then that value is returned. If all turbo filters return NEUTRAL,
     * then NEUTRAL is returned.
     *
     * @param logger  the logger requesting a decision
     * @param slf4jEvent the SLF4J logging event
     * @return the decision of the turbo filter chain
     * @since 1.5.21
     */
    public FilterReply getTurboFilterChainDecision(Logger logger, org.slf4j.event.LoggingEvent slf4jEvent) {

        final int size = size();
        // caller may have already performed this check, but we do it here as well to be sure
        if (size == 0) {
            return FilterReply.NEUTRAL;
        }

        if (size == 1) {
            try {
                TurboFilter tf = get(0);
                return tf.decide(logger, slf4jEvent);
            } catch (IndexOutOfBoundsException iobe) {
                // concurrent modification detected, fall through to the general case
                return FilterReply.NEUTRAL;
            }
        }


        for (TurboFilter tf : this) {
            final FilterReply r = tf.decide(logger, slf4jEvent);
            if (r == FilterReply.DENY || r == FilterReply.ACCEPT) {
                return r;
            }
        }

        return FilterReply.NEUTRAL;
    }

}
