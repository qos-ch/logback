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

    public ControlLogger(String name, ControlLogger parent) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public final Level getEffectiveLevel() {
        for (ControlLogger cl = this; cl != null; cl = cl.parent) {
            if (cl.level != null)
                return cl.level;
        }
        return null; // If reached will cause an NullPointerException.
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ControlLogger))
            return false;

        final ControlLogger controlLogger = (ControlLogger) o;
        return name.equals(controlLogger.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    
    public final void trace(String o) {
        if (getEffectiveLevel().levelInt <= Level.TRACE_INT) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    public final void debug(String o) {
        if (getEffectiveLevel().levelInt <= Level.DEBUG_INT) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    

	@Override
	protected String getFullyQualifiedCallerName() {
		return ControlLogger.class.getName();
	}

	@Override
	protected void handleNormalizedLoggingCall(org.slf4j.event.Level level, Marker marker, String msg,
			Object[] arguments, Throwable throwable) {
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
