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
import ch.qos.logback.core.model.Model;

public class FruitContextAction extends Action {
    
    FruitContextModel parentModel;
    
    @Override
    public void begin(InterpretationContext ic, String name, Attributes attributes) throws ActionException {
        parentModel = new FruitContextModel();
        parentModel.setTag(name);
        ic.pushModel(parentModel);
    }

    @Override
    public void end(InterpretationContext ic, String name) throws ActionException {

        Model m = ic.peekModel();

        if (m != parentModel) {
            addWarn("The object at the of the stack is not the model named [" + parentModel.getTag() + "] pushed earlier.");
        }  
        // NOTE: top level model is NOT popped
    }

}
