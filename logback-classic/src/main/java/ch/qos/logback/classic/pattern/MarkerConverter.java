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

import java.util.List;

import org.slf4j.Marker;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Return the event's marker value(s).
 * 
 * @author S&eacute;bastien Pennec
 */
public class MarkerConverter extends ClassicConverter {

    private static String EMPTY = "";

    public String convert(ILoggingEvent le) {
        List<Marker> markers = le.getMarkerList();
        if (markers == null || markers.isEmpty()) {
            return EMPTY;
        } 
        int size = markers.size() ;
        
        if(size == 1)
            return markers.get(0).toString();
        
        StringBuffer buf = new StringBuffer(32);
        for(int i = 0; i < size; i++) {
        	if(i != 0)
        		buf.append(' ');
        	Marker m = markers.get(i);
        	buf.append(m.toString());
        }
        return buf.toString();
        
    }

}
