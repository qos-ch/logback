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
package ch.qos.logback.core.joran;

import java.util.HashMap;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ImplicitModelAction;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;

public class SimpleConfigurator extends GenericConfigurator {

    HashMap<ElementSelector, Action> rulesMap;

    public SimpleConfigurator(HashMap<ElementSelector, Action> rules) {
        this.rulesMap = rules;
    }

    @Override
    protected void addImplicitRules(SaxEventInterpreter interpreter) {
//    	BeanDescriptionCache bdc = interpreter.getInterpretationContext().getBeanDescriptionCache();
    	
//        NestedComplexPropertyIA nestedIA = new NestedComplexPropertyIA(bdc);
//        nestedIA.setContext(context);
//        interpreter.addImplicitAction(nestedIA);

//        NestedBasicPropertyIA nestedSimpleIA = new NestedBasicPropertyIA(bdc);
//        nestedSimpleIA.setContext(context);
//        interpreter.addImplicitAction(nestedSimpleIA);
        
        
        ImplicitModelAction implicitRuleModelAction = new  ImplicitModelAction();
        interpreter.addImplicitAction(implicitRuleModelAction);

    }

    public SaxEventInterpreter getInterpreter() {
        return interpreter;
    }

    @Override
    protected void addInstanceRules(RuleStore rs) {
        for (ElementSelector elementSelector : rulesMap.keySet()) {
            Action action = rulesMap.get(elementSelector);
            rs.addRule(elementSelector, action);
        }
    }
}
