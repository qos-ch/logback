/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package joran.implicit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import joran.SimpleConfigurator;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ImplicitAction;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.util.StatusPrinter;


/**
 * This example illustrates the usage of implcit actions.
 * 
 * The crucial point to remember about implicit actions is that they
 * are not associated with a pattern. Moreover, they are added directly to
 * a Joran Interpreter instead of a rule store.
 * 
 * @author Ceki G&uuml;ulc&uuml;
 */
public class PrintMe {
  
  
  public static void main(String[] args) throws Exception {
    Context context = new ContextBase();
    
    Map<Pattern, Action> ruleMap = new HashMap<Pattern, Action>();

    
    // we start with the rule for the top-most (root) element
    ruleMap.put(new Pattern("*/foo"), new NOPAction());


    List<ImplicitAction> iaList = new ArrayList<ImplicitAction>();
    // --------------------------+
    // Add an implicit action.   |
    // --------------------------+
    iaList.add(new PrintMeImplicitAction());
    
    SimpleConfigurator simpleConfigurator = new SimpleConfigurator(ruleMap, iaList);
    // link the configurator with its context
    simpleConfigurator.setContext(context);

    try {
      simpleConfigurator.doConfigure(args[0]);
    } catch (JoranException e) {
      // Print any errors that might have occured.
      StatusPrinter.print(context);
    }
    
  }
}
