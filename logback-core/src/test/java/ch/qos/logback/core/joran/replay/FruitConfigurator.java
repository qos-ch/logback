/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran.replay;

import java.util.List;

import ch.qos.logback.core.joran.GenericConfigurator;
import ch.qos.logback.core.joran.NOPAction;
import ch.qos.logback.core.joran.action.NestedComponentIA;
import ch.qos.logback.core.joran.action.NestedSimplePropertyIA;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.EventPlayer;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;

public class FruitConfigurator extends GenericConfigurator {

  FruitFactory ff;
  public FruitConfigurator(FruitFactory ff) {
    this.ff = ff;
  }

  @Override
  final public void doConfigure(final List<SaxEvent> eventList)
      throws JoranException {
    buildInterpreter();
    interpreter.getExecutionContext().pushObject(ff);
    EventPlayer player = new EventPlayer(interpreter);
    player.play(eventList);
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
    rs.addRule(new Pattern("fruitShell"), new NOPAction());
  }

}
