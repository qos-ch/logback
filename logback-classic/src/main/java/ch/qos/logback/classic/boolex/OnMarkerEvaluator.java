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
package ch.qos.logback.classic.boolex;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Marker;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluatorBase;

/**
 * Evaluates to true when the logging event passed as parameter contains one of
 * the user-specified markers.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class OnMarkerEvaluator extends EventEvaluatorBase<ILoggingEvent> {

	List<String> markerList = new ArrayList<String>();

	public void addMarker(String markerStr) {
		markerList.add(markerStr);
	}

	/**
	 * Return true if event passed as parameter contains one of the specified
	 * user-markers.
	 */
	public boolean evaluate(ILoggingEvent event) throws NullPointerException, EvaluationException {

		List<Marker> markerListInEvent = event.getMarkerList();
		if (markerListInEvent == null || markerListInEvent.isEmpty()) {
			return false;
		}

		for (String markerStr : markerList) {
			for (Marker markerInEvent : markerListInEvent) {
				if (markerInEvent.contains(markerStr)) {
					return true;
				}
			}
		}
		return false;
	}
}
