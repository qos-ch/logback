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
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;
import ch.qos.logback.core.joran.spi.RuleStore;

public class TrivialConfigurator extends GenericConfigurator {

    HashMap<ElementSelector, Action> rulesMap;

    public TrivialConfigurator(HashMap<ElementSelector, Action> rules) {
        this.rulesMap = rules;
    }

    @Override
    protected void addImplicitRules(SaxEventInterpreter interpreter) {
    }

    @Override
    protected void addInstanceRules(RuleStore rs) {
        for (ElementSelector elementSelector : rulesMap.keySet()) {
            Action action = rulesMap.get(elementSelector);
            rs.addRule(elementSelector, action);
        }
    }

}
