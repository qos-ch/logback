/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package chapter10.implicit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ImplicitAction;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.util.StatusPrinter;
import chapter10.SimpleConfigurator;

/**
 * This example illustrates the usage of implicit actions.
 * 
 * <p>Keep in mind that implicit actions are not associated with any specific
 * pattern. Moreover, they are added directly to a Joran Interpreter instead of
 * a rule store.
 * 
 * @author Ceki G&uuml;ulc&uuml;
 */
public class PrintMe {

  public static void main(String[] args) throws Exception {
    Context context = new ContextBase();

    Map<Pattern, Action> ruleMap = new HashMap<Pattern, Action>();

    // we start with the rule for the top-most (root) element
    ruleMap.put(new Pattern("*/foo"), new NOPAction());

    // Add an implicit action. 
    List<ImplicitAction> iaList = new ArrayList<ImplicitAction>();
    iaList.add(new PrintMeImplicitAction());
    SimpleConfigurator simpleConfigurator = new SimpleConfigurator(ruleMap,
        iaList); 

    // link the configurator with its context
    simpleConfigurator.setContext(context);

    simpleConfigurator.doConfigure(args[0]);
    StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    
  }
}
