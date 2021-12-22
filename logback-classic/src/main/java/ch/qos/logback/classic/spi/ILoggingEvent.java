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

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

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

    
    /**
     * Returns the first marker is the marker list or null if no markers are available.
     * 
     * This method is deprecated and exists solely for backward compatibility reasons.
     * Logback components should use {@link #getMarkerList()} and cater for all available markers 
     * and not the only the first one. 
     * 
     * @deprecated Replaced by {@link #getMarkerList()}
     * @return the first marker in the marker list or null if no markers are available
     */
    default Marker getMarker() {
    	List<Marker> markers = getMarkerList();
    	if(markers == null || markers.isEmpty())
    		return null;

    	// return the first marker. Assuming that only the first marker is useful
    	// is obviously incorrect. However, we have no other choice if we wish
    	// to preserve binary compatibility.
    	return markers.get(0);
    }

    
    /**
     * Since SLF4J 2.0.0, the slf4j logging API assumes the possibility of multiple
     * Marker instances in a logging event. Consequently, ILoggingEvent needs to cater 
     * for this possibility. 
     * 
     * @return the marker list, may be null
     * @since 1.3.0
     */
    List<Marker> getMarkerList();

    /**
     * Returns the MDC map. The returned value can be an empty map but not null.
     */
    Map<String, String> getMDCPropertyMap();

    /**
     * Synonym for [@link #getMDCPropertyMap}.
     * @deprecated  Replaced by [@link #getMDCPropertyMap}
     */
    Map<String, String> getMdc();

    /**
     * Return the number of elapsed milliseconds since epoch.
     * 
     * @return the number of elapsed milliseconds since epoch
     * @since 1.3
     */
    long getTimeStamp();
    
    /**
     * Return the number of elapsed nanoseconds found in {@link #getInstant()} 
     * 
     * Will return 0 if getInstant() returns null;
     * 
     * @return the number of elapsed nanoseconds since epoch
     * @since 1.3
     */
    default int getNanoseconds() {
    	Instant instant = getInstant(); 
    	if(instant == null)
    		return 0;
    	int nanoseconds = instant.getNano();
    	return nanoseconds;
    }

    /**
     * Return the Instant the event was created.
     * 
     * Default implementation returns null.
     * 
     * @return the Instant the event was created.
     * @since 1.3
     */
    default Instant getInstant() {
    	return null;
    }

	/**
     * The sequence number associated with this event. 
     * 
     * <p>Sequence numbers, if present, should be increasing monotonically.
     *  
     * @since 1.3.0
     */
    long getSequenceNumber();

    /**
     * A list of {@link KeyValuePair} objects. The returned list may be null.
     * 
     * @return may be null
     * @since 1.3.0
     */
    List<KeyValuePair> getKeyValuePairs();
    
    void prepareForDeferredProcessing();

}
