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
public class MicrosecondConverter extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		int nanos = event.getNanoseconds();
		int millis_and_micros = nanos / 1000;
		int micros = millis_and_micros % 1000;

		if (micros >= 100)
			return Integer.toString(micros);
		else if (micros >= 10)
			return "0" + Integer.toString(micros);
		else
			return "00" + Integer.toString(micros);
	}

}
