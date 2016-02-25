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
package ch.qos.logback.classic.spi;

import java.util.Map;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.spi.DeferredProcessingAware;

/**
 * The central interface in logback-classic. In a nutshell, logback-classic is
 * nothing more than a processing chain built around this interface.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 0.9.16
 */
public interface ILoggingEvent extends DeferredProcessingAware {

    String getThreadName();

    Level getLevel();

    String getMessage();

    Object[] getArgumentArray();

    String getFormattedMessage();

    String getLoggerName();

    LoggerContextVO getLoggerContextVO();

    IThrowableProxy getThrowableProxy();

    /**
     * Return caller data associated with this event. Note that calling this event
     * may trigger the computation of caller data.
     * 
     * @return the caller data associated with this event.
     * 
     * @see #hasCallerData()
     */
    StackTraceElement[] getCallerData();

    /**
     * If this event has caller data, then true is returned. Otherwise the
     * returned value is null.
     * 
     * <p>Logback components wishing to use caller data if available without
     * causing it to be computed can invoke this method before invoking
     * {@link #getCallerData()}.
     * 
     * @return whether this event has caller data
     */
    boolean hasCallerData();

    Marker getMarker();

    /**
     * Returns the MDC map. The returned value can be an empty map but not null.
     */
    Map<String, String> getMDCPropertyMap();

    /**
     * Synonym for [@link #getMDCPropertyMap}.
     * @deprecated  Replaced by [@link #getMDCPropertyMap}
     */
    Map<String, String> getMdc();

    long getTimeStamp();

    void prepareForDeferredProcessing();

}
