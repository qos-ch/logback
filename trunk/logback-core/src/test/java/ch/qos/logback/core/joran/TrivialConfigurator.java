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

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;

public class TrivialConfigurator extends GenericConfigurator {

  HashMap<Pattern, Action> rulesMap;
  
  public TrivialConfigurator(HashMap<Pattern, Action> rules) {
    this.rulesMap = rules;
  }
  
  @Override
  protected void addImplicitRules(Interpreter interpreter) {
  }

  @Override
  protected void addInstanceRules(RuleStore rs) {
    for(Pattern pattern : rulesMap.keySet()) {
      Action action = rulesMap.get(pattern);
      rs.addRule(pattern, action);
    }
  }

}
