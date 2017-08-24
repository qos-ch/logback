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
package chapters.onJoran.newRule;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.NewRuleAction;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.util.StatusPrinter;
import chapters.onJoran.SimpleConfigurator;
import chapters.onJoran.calculator.ComputationAction1;

/**
 * This example illustrates the usage of NewRuleAction which allows the Joran
 * interpreter to learn new rules on the fly.
 * 
 * <p>This example relies heavily on the code from the joran.calculator
 * package.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class NewRuleCalculator {
    public static void main(String[] args) throws Exception {

        Context context = new ContextBase();

        Map<ElementSelector, Action> ruleMap = new HashMap<ElementSelector, Action>();

        // we start with the rule for the top-most (root) element
        ruleMap.put(new ElementSelector("*/computation"), new ComputationAction1());

        // Associate "/new-rule" pattern with NewRuleAction from the
        // org.apache.joran.action package.
        //
        // We will let the XML file to teach the Joran interpreter about new rules
        ruleMap.put(new ElementSelector("/computation/newRule"), new NewRuleAction());

        SimpleConfigurator simpleConfigurator = new SimpleConfigurator(ruleMap);
        // link the configurator with its context
        simpleConfigurator.setContext(context);

        simpleConfigurator.doConfigure(args[0]);

        // Print any errors that might have occured.
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

}
