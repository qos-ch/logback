/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2021, QOS.ch. All rights reserved.
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
 * Outputs the number of microseconds of the timestamp.
 * 
 *  
 * @author ceki
 * @since 1.3.0
 */
public class MicrosecondConverter  extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		int nano = event.getNanoseconds();
		int micro = nano/1000;
		return Integer.toString(micro);
	}

}
