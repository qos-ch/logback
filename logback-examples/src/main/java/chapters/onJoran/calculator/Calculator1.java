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
package chapters.onJoran.calculator;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.util.StatusPrinter;
import chapters.onJoran.SimpleConfigurator;

/**
 * This examples illustrates collaboration between multiple actions through the
 * common execution context stack.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class Calculator1 {

    public static void main(String[] args) throws Exception {
        Context context = new ContextBase();

        Map<ElementSelector, Action> ruleMap = new HashMap<ElementSelector, Action>();

        // Associate "/computation" pattern with ComputationAction1
        ruleMap.put(new ElementSelector("/computation"), new ComputationAction1());

        // Other associations
        ruleMap.put(new ElementSelector("/computation/literal"), new LiteralAction());
        ruleMap.put(new ElementSelector("/computation/add"), new AddAction());
        ruleMap.put(new ElementSelector("/computation/multiply"), new MultiplyAction());

        SimpleConfigurator simpleConfigurator = new SimpleConfigurator(ruleMap);
        // link the configurator with its context
        simpleConfigurator.setContext(context);

        simpleConfigurator.doConfigure(args[0]);
        // Print any errors that might have occured.
        StatusPrinter.print(context);
    }
}
