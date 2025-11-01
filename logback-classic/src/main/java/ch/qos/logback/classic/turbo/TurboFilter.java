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
package ch.qos.logback.classic.turbo;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.spi.LifeCycle;

import java.util.List;

/**
 * TurboFilter is a specialized filter with a decide method that takes a bunch
 * of parameters instead of a single event object. The latter is cleaner but the
 * first is much more performant.
 * <p>
 * For more information about turbo filters, please refer to the online manual
 * at https://logback.qos.ch/manual/filters.html#TurboFilter
 *
 * @author Ceki Gulcu
 */
public abstract class TurboFilter extends ContextAwareBase implements LifeCycle {

    private String name;
    boolean start = false;

    /**
     * Make a decision based on the multiple parameters passed as arguments. The
     * returned value should be one of <code>{@link FilterReply#DENY}</code>,
     * <code>{@link FilterReply#NEUTRAL}</code>, or
     * <code>{@link FilterReply#ACCEPT}</code>.
     * 
     * @param marker
     * @param logger
     * @param level
     * @param format
     * @param params
     * @param t
     * @return
     */
    public abstract FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params,
            Throwable t);


    /**
     * <p>This method is intended to be called via SLF4J's fluent API and more specifically by
     * {@link Logger#log(org.slf4j.event.LoggingEvent slf4jEvent)}. Derived classes are strongly
     * encouraged to override this method with a better suited and more specialized
     * implementation.
     * </p>
     *
     * <p>The present default implementation translates the given SLF4J {@code LoggingEvent} into the
     * set of parameters required by {@link #decide(Marker, Logger, Level, String, Object[], Throwable)}
     * and delegate the decision to that method.
     * </p>
     *
     * <p>Concretely, this method:
     * <ul>
     *   <li>extracts the first marker (if any) from the event's marker list,</li>
     *   <li>maps the SLF4J level to Logback's {@link Level},</li>
     *   <li>and forwards the event message, arguments and throwable.</li>
     * </ul>
     *
     * <p>Returns the {@link ch.qos.logback.core.spi.FilterReply} produced by
     * {@code decide(...)}, which should be one of DENY, NEUTRAL or ACCEPT.
     *
     * <p>Derived classes are strongly encouraged to override this method with a
     * better suited and more specialized implementation.</p>
     *
     * @param logger the Logger that is logging the event; non-null
     * @param slf4jEvent the SLF4J logging event to translate and evaluate; may be non-null
     * @return the filter decision ({@code DENY}, {@code NEUTRAL} or {@code ACCEPT})
     *
     * @since 1.5.21
     */
    public FilterReply decide(Logger logger, org.slf4j.event.LoggingEvent slf4jEvent) {
        List<Marker> markers = slf4jEvent.getMarkers();
        Marker firstMarker = (markers != null && !markers.isEmpty()) ? markers.get(0) : null;

        Level logbackLevel = Level.convertAnSLF4JLevel(slf4jEvent.getLevel());
        String format = slf4jEvent.getMessage();
        Object[] params = slf4jEvent.getArgumentArray();
        Throwable t = slf4jEvent.getThrowable();

        return decide(firstMarker, logger, logbackLevel, format, params, t);
    }

    public void start() {
        this.start = true;
    }

    public boolean isStarted() {
        return this.start;
    }

    public void stop() {
        this.start = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
