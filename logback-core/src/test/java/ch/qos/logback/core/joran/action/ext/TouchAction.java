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
import ch.qos.logback.core.joran.spi.InterpretationContext;

public class TouchAction extends Action {

    public static final String KEY = "touched";

    /**
     * Instantiates an layout of the given class and sets its name.
     *
     */
    public void begin(InterpretationContext ec, String name, Attributes attributes) {
        Integer i = (Integer) ec.getContext().getObject(KEY);
        if (i == null) {
            ec.getContext().putObject(KEY, new Integer(1));
        } else {
            ec.getContext().putObject(KEY, new Integer(i.intValue() + 1));
        }
    }

    /**
     * Once the children elements are also parsed, now is the time to activate
     * the appender options.
     */
    public void end(InterpretationContext ec, String name) {
    }
}
