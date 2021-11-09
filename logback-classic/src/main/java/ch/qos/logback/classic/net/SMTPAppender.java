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
package ch.qos.logback.classic.net;

import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Marker;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.boolex.OnErrorEvaluator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.helpers.CyclicBuffer;
import ch.qos.logback.core.net.SMTPAppenderBase;

/**
 * Send an e-mail when a specific logging event occurs, typically on errors or
 * fatal errors.
 *
 * For more information about this appender, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#SMTPAppender
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 *
 */
public class SMTPAppender extends SMTPAppenderBase<ILoggingEvent> {

	// value "%logger{20} - %m" is referenced in the docs!
	static final String DEFAULT_SUBJECT_PATTERN = "%logger{20} - %m";

	private boolean includeCallerData = false;

	/**
	 * The default constructor will instantiate the appender with a
	 * {@link EventEvaluator} that will trigger on events with level
	 * ERROR or higher.
	 */
	public SMTPAppender() {

	}

	@Override
	public void start() {
		if (eventEvaluator == null) {
			final OnErrorEvaluator onError = new OnErrorEvaluator();
			onError.setContext(getContext());
			onError.setName("onError");
			onError.start();
			eventEvaluator = onError;
		}
		super.start();
	}

	/**
	 * Use the parameter as the {@link
	 * EventEvaluator} for this SMTPAppender.
	 */
	public SMTPAppender(final EventEvaluator<ILoggingEvent> eventEvaluator) {
		this.eventEvaluator = eventEvaluator;
	}

	/**
	 * Perform SMTPAppender specific appending actions, mainly adding the event to
	 * a cyclic buffer.
	 */
	@Override
	protected void subAppend(final CyclicBuffer<ILoggingEvent> cb, final ILoggingEvent event) {
		if (includeCallerData) {
			event.getCallerData();
		}
		event.prepareForDeferredProcessing();
		cb.add(event);
	}

	@Override
	protected void fillBuffer(final CyclicBuffer<ILoggingEvent> cb, final StringBuffer sbuf) {
		final int len = cb.length();
		for (int i = 0; i < len; i++) {
			final ILoggingEvent event = cb.get();
			sbuf.append(layout.doLayout(event));
		}
	}

	@Override
	protected boolean eventMarksEndOfLife(final ILoggingEvent eventObject) {
		final List<Marker> markers = eventObject.getMarkerList();
		if (markers == null || markers.isEmpty()) {
			return false;
		}

		for(final Marker marker: markers) {
			if(marker.contains(ClassicConstants.FINALIZE_SESSION_MARKER)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected Layout<ILoggingEvent> makeSubjectLayout(String subjectStr) {
		if (subjectStr == null) {
			subjectStr = DEFAULT_SUBJECT_PATTERN;
		}
		final PatternLayout pl = new PatternLayout();
		pl.setContext(getContext());
		pl.setPattern(subjectStr);
		// we don't want a ThrowableInformationConverter appended
		// to the end of the converter chain
		// This fixes issue LBCLASSIC-67
		pl.setPostCompileProcessor(null);
		pl.start();
		return pl;
	}

	@Override
	protected PatternLayout makeNewToPatternLayout(final String toPattern) {
		final PatternLayout pl = new PatternLayout();
		pl.setPattern(toPattern + "%nopex");
		return pl;
	}

	public boolean isIncludeCallerData() {
		return includeCallerData;
	}

	public void setIncludeCallerData(final boolean includeCallerData) {
		this.includeCallerData = includeCallerData;
	}

	Future<?> getAsynchronousSendingFuture() {
		return asynchronousSendingFuture;
	}
}
