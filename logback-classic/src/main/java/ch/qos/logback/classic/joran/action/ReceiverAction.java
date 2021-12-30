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
package ch.qos.logback.classic.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.classic.net.ReceiverBase;
import ch.qos.logback.classic.net.SocketReceiver;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

/**
 * A Joran {@link Action} for a {@link SocketReceiver} configuration.
 *
 * @author Carl Harris
 */
public class ReceiverAction extends Action {

    private ReceiverBase receiver;
    private boolean inError;

    @Override
    public void begin(InterpretationContext ic, String name, Attributes attributes) throws ActionException {

        String className = attributes.getValue(CLASS_ATTRIBUTE);
        if (OptionHelper.isNullOrEmpty(className)) {
            addError("Missing class name for receiver. Near [" + name + "] line " + getLineNumber(ic));
            inError = true;
            return;
        } else {
        	className = ic.getImport(className);
        }

        try {
            addInfo("About to instantiate receiver of type [" + className + "]");

            receiver = (ReceiverBase) OptionHelper.instantiateByClassName(className, ReceiverBase.class, context);
            receiver.setContext(context);

            ic.pushObject(receiver);
        } catch (Exception ex) {
            inError = true;
            addError("Could not create a receiver of type [" + className + "].", ex);
            throw new ActionException(ex);
        }
    }

    @Override
    public void end(InterpretationContext ic, String name) throws ActionException {

        if (inError)
            return;

        ic.getContext().register(receiver);
        receiver.start();

        Object o = ic.peekObject();
        if (o != receiver) {
            addWarn("The object at the of the stack is not the remote " + "pushed earlier.");
        } else {
            ic.popObject();
        }
    }

}
