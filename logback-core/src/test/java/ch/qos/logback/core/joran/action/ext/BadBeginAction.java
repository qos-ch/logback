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
package ch.qos.logback.core.joran.action.ext;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;

public class BadBeginAction extends Action {

	static String EXCEPTION_TYPE = "type";
	static final int RUNTIME_EDXCEPTION = 0;
	static final int ACTION_EXCEPTION = 1;

	int type;

	@Override
	public void begin(final InterpretationContext ec, final String name, final Attributes attributes) throws ActionException {

		final String exType = attributes.getValue(EXCEPTION_TYPE);
		type = RUNTIME_EDXCEPTION;
		if ("ActionException".equals(exType)) {
			type = ACTION_EXCEPTION;
		}

		switch (type) {
		case ACTION_EXCEPTION:
			throw new ActionException();
		default:
			throw new IllegalStateException("bad begin");
		}

	}

	@Override
	public void end(final InterpretationContext ec, final String name) {
	}
}
