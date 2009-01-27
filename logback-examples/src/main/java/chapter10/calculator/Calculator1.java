/**
 * Logback: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package chapter10.calculator;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.util.StatusPrinter;
import chapter10.SimpleConfigurator;

/**
 * This examples illustrates collaboration between multiple actions through the
 * common execution context stack.
 * 
 * The first and only argument of this application must be the path to the XML
 * file to interpret. There are sample XML files in the
 * <em>examples/src/joran/calculator/</em> directory.
 * 
 * For example,
 * 
 * <pre>
 *  java joran.calculator.Calculator1 examples/src/joran/calculator/calculator1.xml
 * </pre>
 * 
 * Please refer to the comments in the source code for more information.
 * 
 * @author Ceki G&uuml;ulc&uuml;
 */
public class Calculator1 {

  public static void main(String[] args) throws Exception {
    Context context = new ContextBase();

    Map<Pattern, Action> ruleMap = new HashMap<Pattern, Action>();

    // Associate "/computation" pattern with ComputationAction1
    ruleMap.put(new Pattern("/computation"), new ComputationAction1());

    // Other associations
    ruleMap.put(new Pattern("/computation/literal"), new LiteralAction());
    ruleMap.put(new Pattern("/computation/add"), new AddAction());
    ruleMap.put(new Pattern("/computation/multiply"), new MultiplyAction());

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
