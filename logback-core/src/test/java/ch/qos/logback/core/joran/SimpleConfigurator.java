/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran;

import java.util.HashMap;

import ch.qos.logback.core.joran.GenericConfigurator;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.NestedComponentIA;
import ch.qos.logback.core.joran.action.NestedSimplePropertyIA;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;

public class SimpleConfigurator extends GenericConfigurator {

  HashMap<Pattern, Action> rulesMap;
  
  public SimpleConfigurator(HashMap<Pattern, Action> rules) {
    this.rulesMap = rules;
  }
  
  @Override
  protected void addImplicitRules(Interpreter interpreter) {
    NestedComponentIA nestedIA = new NestedComponentIA();
    nestedIA.setContext(context);
    interpreter.addImplicitAction(nestedIA);

    NestedSimplePropertyIA nestedSimpleIA = new NestedSimplePropertyIA();
    nestedIA.setContext(context);
    interpreter.addImplicitAction(nestedSimpleIA);
  }

  @Override
  protected void addInstanceRules(RuleStore rs) {
    for(Pattern pattern : rulesMap.keySet()) {
      Action action = rulesMap.get(pattern);
      System.out.println("Adding "+pattern +" "+action);
      rs.addRule(pattern, action);
    }
  }

}
