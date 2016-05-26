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

public class IncAction extends Action {

    static public int beginCount;
    static public int endCount;
    static public int errorCount;

    static public void reset() {
        beginCount = 0;
        endCount = 0;
        errorCount = 0;
    }

    /**
     * Instantiates an layout of the given class and sets its name.
     *
     */
    public void begin(InterpretationContext ec, String name, Attributes attributes) throws ActionException {
        // System.out.println("IncAction Begin called");
        beginCount++;
        String val = attributes.getValue("increment");
        if (!"1".equals(val)) {
            errorCount++;
            throw new ActionException();
        }
    }

    /**
     * Once the children elements are also parsed, now is the time to activate
     * the appender options.
     */
    public void end(InterpretationContext ec, String name) {
        endCount++;
    }
}
