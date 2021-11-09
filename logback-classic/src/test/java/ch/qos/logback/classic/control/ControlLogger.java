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
package ch.qos.logback.classic.control;

import org.slf4j.Marker;
import org.slf4j.helpers.LegacyAbstractLogger;

import ch.qos.logback.classic.Level;

/**
 * See javadoc for ControlLoggerContext.
 */
public class ControlLogger extends LegacyAbstractLogger {

	private static final long serialVersionUID = 1L;
	final ControlLogger parent;
	final String name;
	Level level;

	public ControlLogger(final String name, final ControlLogger parent) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null");
		}
		this.name = name;
		this.parent = parent;
	}

	@Override
	public String getName() {
		return name;
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(final Level level) {
		this.level = level;
	}

	public final Level getEffectiveLevel() {
		for (ControlLogger cl = this; cl != null; cl = cl.parent) {
			if (cl.level != null) {
				return cl.level;
			}
		}
		return null; // If reached will cause an NullPointerException.
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ControlLogger)) {
			return false;
		}

		final ControlLogger controlLogger = (ControlLogger) o;
		return name.equals(controlLogger.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}


	@Override
	public final void trace(final String o) {
		if (getEffectiveLevel().levelInt <= Level.TRACE_INT) {
			throw new UnsupportedOperationException("not yet implemented");
		}
	}

	@Override
	public final void debug(final String o) {
		if (getEffectiveLevel().levelInt <= Level.DEBUG_INT) {
			throw new UnsupportedOperationException("not yet implemented");
		}
	}



	@Override
	protected String getFullyQualifiedCallerName() {
		return ControlLogger.class.getName();
	}

	@Override
	protected void handleNormalizedLoggingCall(final org.slf4j.event.Level level, final Marker marker, final String msg,
			final Object[] arguments, final Throwable throwable) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isTraceEnabled() {
		return false;
	}

	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public boolean isInfoEnabled() {
		return false;
	}

	@Override
	public boolean isWarnEnabled() {
		return false;
	}

	@Override
	public boolean isErrorEnabled() {
		return false;
	}

}
