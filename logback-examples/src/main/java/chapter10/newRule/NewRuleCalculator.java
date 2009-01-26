/**
 * Logback: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package chapter10.newRule;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.NewRuleAction;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.util.StatusPrinter;
import chapter3.SimpleConfigurator;
import chapter10.calculator.ComputationAction2;


/**
 * This example illustrates the usage of NewRuleAction which allows the Joran
 * interpreter to learn new rules on the fly from the XML file being
 * interpreted.
 *
 * This example relies heavily on the code from the joran.calculator package.
 *
 * @author Ceki G&uuml;ulc&uuml;
 */
public class NewRuleCalculator {
  public static void main(String[] args) throws Exception {
  
    Context context = new ContextBase();
  
    
    Map<Pattern, Action> ruleMap = new HashMap<Pattern, Action>();

    // we start with the rule for the top-most (root) element
    ruleMap.put(new Pattern("*/computation"), new ComputationAction2());

    // Associate "/new-rule" pattern with NewRuleAction from the 
    // org.apache.joran.action package.
    // 
    // We will let the XML file to teach the Joran interpreter about new rules 
    ruleMap.put(
      new Pattern("/computation/new-rule"), new NewRuleAction());

    SimpleConfigurator simpleConfigurator = new SimpleConfigurator(ruleMap);
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
