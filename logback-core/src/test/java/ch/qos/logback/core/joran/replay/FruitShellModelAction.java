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
package ch.qos.logback.core.joran.replay;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

/** 
 * The Fruit* code is intended to test Joran's replay capability
 * */
public class FruitShellAction extends Action {

    FruitShell fruitShell;
    private boolean inError = false;

    @Override
    public void begin(InterpretationContext ec, String name, Attributes attributes) throws ActionException {

        // We are just beginning, reset variables
        fruitShell = new FruitShell();
        inError = false;

        try {

            fruitShell.setContext(context);

            String shellName = attributes.getValue(NAME_ATTRIBUTE);

            if (OptionHelper.isEmpty(shellName)) {
                addWarn("No appender name given for fruitShell].");
            } else {
                fruitShell.setName(shellName);
                addInfo("FruitShell named as [" + shellName + "]");
            }

            ec.pushObject(fruitShell);
        } catch (Exception oops) {
            inError = true;
            addError("Could not create an FruitShell", oops);
            throw new ActionException(oops);
        }
    }

    @Override
    public void end(InterpretationContext ec, String name) throws ActionException {
        if (inError) {
            return;
        }

        Object o = ec.peekObject();

        if (o != fruitShell) {
            addWarn("The object at the of the stack is not the fruitShell named [" + fruitShell.getName() + "] pushed earlier.");
        } else {
            addInfo("Popping fruitSHell named [" + fruitShell.getName() + "] from the object stack");
            ec.popObject();
            FruitContext fruitContext = (FruitContext) ec.getContext();
            fruitContext.addFruitShell(fruitShell);
        }
    }

}
