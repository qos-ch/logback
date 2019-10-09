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
package chapters.onJoran;

import java.util.List;
import java.util.Map;

import ch.qos.logback.core.joran.GenericConfigurator;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ImplicitModelAction;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;

/**
 * A minimal configurator extending GenericConfigurator.
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class SimpleConfigurator extends GenericConfigurator {

    final Map<ElementSelector, Action> ruleMap;
    final List<ImplicitModelAction> iaList;

    public SimpleConfigurator(Map<ElementSelector, Action> ruleMap) {
        this(ruleMap, null);
    }

    public SimpleConfigurator(Map<ElementSelector, Action> ruleMap, List<ImplicitModelAction> iaList) {
        this.ruleMap = ruleMap;
        this.iaList = iaList;
    }

    @Override
    protected void addInstanceRules(RuleStore rs) {
        for (ElementSelector elementSelector : ruleMap.keySet()) {
            Action action = ruleMap.get(elementSelector);
            rs.addRule(elementSelector, action);
        }
    }

    @Override
    protected void addImplicitRules(SaxEventInterpreter interpreter) {
        if (iaList == null) {
            return;
        }
        for (ImplicitModelAction ia : iaList) {
            interpreter.addImplicitAction(ia);
        }
    }

	
}
