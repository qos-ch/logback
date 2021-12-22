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

import static org.junit.Assert.assertEquals;

import java.time.Instant;

import org.junit.Test;

import ch.qos.logback.classic.spi.LoggingEvent;

public class MicrosecondConverterTest {

	MicrosecondConverter mc = new MicrosecondConverter();
	public long timeStamp;
	public int nanoseconds;
	@Test
	public void smoke() {
		LoggingEvent le = new LoggingEvent();
		Instant instant = Instant.parse("2011-12-03T10:15:30Z");
    	instant = instant.plusNanos(123_456_789);
    	le.setInstant(instant);
    	
		String result = mc.convert(le);
		assertEquals("456", result);
	}
	
}
