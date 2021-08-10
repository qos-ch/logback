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
package ch.qos.logback.classic.spi;

import java.util.List;

import org.slf4j.Marker;
import org.slf4j.spi.DefaultLoggingEventBuilder;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * A trivial implementation LoggingEventBuilder which build on
 * DefaultLoggingEventBuilder.
 * 
 * @author Ceki Gulcu
 *
 */
public class LogbackLoggingEventBuilder extends DefaultLoggingEventBuilder {

	LoggingEvent loggingEvent;

    public static final String FQCN = LogbackLoggingEventBuilder.class.getName();

	public LogbackLoggingEventBuilder(Logger logger, org.slf4j.event.Level level) {
		super(logger, level);
	}

	@Override
	protected void log(org.slf4j.event.LoggingEvent sle) {
		org.slf4j.event.Level slf4jLevel = sle.getLevel();
		Level logbackLevel = Level.convertAnSLF4JLevel(slf4jLevel);
		Logger logbackLogger = (Logger) this.logger;
		LoggingEvent lle = new LoggingEvent(FQCN, logbackLogger, logbackLevel,  sle.getMessage(), sle.getThrowable(), sle.getArgumentArray());
		List<Marker> markers = sle.getMarkers();
		
		if(markers != null) {
			markers.forEach(m -> lle.addMarker(m));
		}
		
		lle.setKeyValuePairs(sle.getKeyValuePairs());
		

		// Note that at this point, any calls made with a logger disabled 
		// for a given level, will be already filtered out/in. TurboFilters cannot 
		// act at this point in the process.
		logbackLogger.callAppenders(lle);
	}

}
