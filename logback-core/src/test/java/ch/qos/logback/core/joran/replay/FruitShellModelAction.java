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

import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.FruitShellModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.util.OptionHelper;

/** 
 * The Fruit* code is intended to test Joran's replay capability
 * */
public class FruitShellModelAction extends BaseModelAction {

    FruitShellModel fruitShellModel;


    @Override
    protected boolean validPreconditions(InterpretationContext interpretationContext, String name, Attributes attributes) {
        String shellName = attributes.getValue(NAME_ATTRIBUTE);
        if (OptionHelper.isEmpty(shellName)) {
            addError("Missing name for fruitShell. Near [" + name + "] line " + getLineNumber(interpretationContext));
            return false;
        }
        return true;
    }
    
    @Override
    protected Model buildCurrentModel(InterpretationContext interpretationContext, String name, Attributes attributes) {
        fruitShellModel = new FruitShellModel();
        String shellName = attributes.getValue(NAME_ATTRIBUTE);
        fruitShellModel.setName(shellName);
        addInfo("FruitShell named as [" + shellName + "]");
        return fruitShellModel;
    }
    


}
