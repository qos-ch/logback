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

public class BadEndAction extends Action {

    static String EXCEPTION_TYPE = "type";
    static final int RUNTIME_EXCEPTION = 0;
    static final int ACTION_EXCEPTION = 1;

    int type;

    public void begin(InterpretationContext ec, String name, Attributes attributes) {
        String exType = attributes.getValue(EXCEPTION_TYPE);
        type = RUNTIME_EXCEPTION;
        if ("ActionException".equals(exType)) {
            type = ACTION_EXCEPTION;
        }
    }

    public void end(InterpretationContext ec, String name) throws ActionException {
        switch (type) {
        case ACTION_EXCEPTION:
            throw new ActionException();
        default:
            throw new IllegalStateException("bad end");
        }
    }
}
