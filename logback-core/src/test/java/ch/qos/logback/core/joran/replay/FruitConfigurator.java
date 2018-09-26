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

import java.util.List;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.GenericConfigurator;
import ch.qos.logback.core.joran.action.ImplicitModelAction;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.EventPlayer;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.DefaultProcessor;

public class FruitConfigurator extends GenericConfigurator {

    FruitFactory ff;

    public FruitConfigurator(FruitFactory ff) {
        this.ff = ff;
    }


    @Override
    final public void doConfigure(Model model) {
        buildInterpreter();
        interpreter.getInterpretationContext().pushObject(ff);
        
    }

    public InterpretationContext getInterpretationContext() {
        return interpreter.getInterpretationContext();
    }
    
    @Override
    protected DefaultProcessor buildDefaultProcessor(Context context, InterpretationContext interpretationContext) {
        DefaultProcessor defaultProcessor = super.buildDefaultProcessor(context, interpretationContext);

        return defaultProcessor;
    }

    @Override
    protected void addInstanceRules(RuleStore rs) {
        rs.addRule(new ElementSelector("fruitShell"), new NOPAction());
    }

    @Override
    protected void addImplicitRules(Interpreter interpreter) {
        ImplicitModelAction implicitRuleModelAction = new ImplicitModelAction();
        interpreter.addImplicitAction(implicitRuleModelAction);
    }

}
