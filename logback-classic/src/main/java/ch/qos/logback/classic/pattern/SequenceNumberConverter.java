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

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Return the event's sequence number.
 * 
 * @author Bertrand Renuart
 */
public class SequenceNumberConverter extends ClassicConverter {

	@Override
	public void start() {
		if (getContext() == null || getContext().getSequenceNumberGenerator() == null) {
			addWarn("No <sequenceNumberGenerator> defined in Logback configuration - event sequence numbers will not be incremented.");
		}
		super.start();
	}
	
	
	@Override
    public String convert(ILoggingEvent event) {
        return Long.toString(event.getSequenceNumber());
    }

}
