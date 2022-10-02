/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.blackbox.joran;

import ch.qos.logback.core.joran.GenericXMLConfigurator;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ImplicitModelAction;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;

import java.util.HashMap;
import java.util.function.Supplier;

public class BlackboxSimpleConfigurator extends GenericXMLConfigurator  {


    HashMap<ElementSelector, Supplier<Action>> rulesMap;

    public BlackboxSimpleConfigurator(HashMap<ElementSelector, Supplier<Action>> rules) {
        this.rulesMap = rules;
    }

    @Override
    protected void setImplicitRuleSupplier(SaxEventInterpreter interpreter) {
        interpreter.setImplicitActionSupplier(() -> new ImplicitModelAction());
    }

    public SaxEventInterpreter getInterpreter() {
        return saxEventInterpreter;
    }

    @Override
    protected void addElementSelectorAndActionAssociations(RuleStore rs) {
        for (ElementSelector elementSelector : rulesMap.keySet()) {
            Supplier<Action> actionSupplier = rulesMap.get(elementSelector);
            rs.addRule(elementSelector, actionSupplier);
        }
    }
}
