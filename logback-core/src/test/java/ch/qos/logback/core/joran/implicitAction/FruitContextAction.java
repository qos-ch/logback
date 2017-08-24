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
package ch.qos.logback.core.joran.implicitAction;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;

public class FruitContextAction extends Action {

    private boolean inError = false;

    @Override
    public void begin(InterpretationContext ec, String name, Attributes attributes) throws ActionException {

        inError = false;

        try {
            ec.pushObject(context);
        } catch (Exception oops) {
            inError = true;
            addError("Could not push context", oops);
            throw new ActionException(oops);
        }
    }

    @Override
    public void end(InterpretationContext ec, String name) throws ActionException {
        if (inError) {
            return;
        }

        Object o = ec.peekObject();

        if (o != context) {
            addWarn("The object at the of the stack is not the context named [" + context.getName() + "] pushed earlier.");
        } else {
            addInfo("Popping context named [" + context.getName() + "] from the object stack");
            ec.popObject();
        }
    }

}
