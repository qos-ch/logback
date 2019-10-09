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
package chapters.onJoran.implicit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ImplicitModelAction;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.util.StatusPrinter;
import chapters.onJoran.SimpleConfigurator;

/**
 * BEWARE: This examples are outdated with version 1.3. They need to be rewritten.
 * 
 * This example illustrates the usage of implicit actions.
 * 
 * <p>Keep in mind that implicit actions are not associated with any specific
 * pattern. Moreover, they are added directly to a Joran Interpreter instead of
 * a rule store.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class PrintMe {

    public static void main(String[] args) throws Exception {
        Context context = new ContextBase();

        Map<ElementSelector, Action> ruleMap = new HashMap<ElementSelector, Action>();

        // we start with the rule for the top-most (root) element
        ruleMap.put(new ElementSelector("*/foo"), new NOPAction());

        // Add an implicit action.
        List<ImplicitModelAction> iaList = new ArrayList<ImplicitModelAction>();
        iaList.add(new PrintMeImplicitAction());
        SimpleConfigurator simpleConfigurator = new SimpleConfigurator(ruleMap, iaList);

        // link the configurator with its context
        simpleConfigurator.setContext(context);

        simpleConfigurator.doConfigure(args[0]);
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);

    }
}
