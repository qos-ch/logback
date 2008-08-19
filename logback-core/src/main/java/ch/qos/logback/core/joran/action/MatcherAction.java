/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.boolex.JaninoEventEvaluatorBase;
import ch.qos.logback.core.boolex.Matcher;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.ActionException.SkipCode;
import ch.qos.logback.core.util.OptionHelper;


public class MatcherAction extends Action {
  Matcher matcher;
  final static String REGEX = "regex";
  private boolean inError = false;
  
  /**
   * Instantiates an appender of the given class and sets its name.
   * 
   * The appender thus generated is placed in the ExecutionContext appender bag.
   */
  public void begin(InterpretationContext ec, String localName, Attributes attributes)
      throws ActionException {
    
    matcher = null;
    inError = false;
    
    try {
      matcher = new Matcher();
      matcher.setContext(context);
      
      String matcherName = attributes.getValue(NAME_ATTRIBUTE);
      if (OptionHelper.isEmpty(matcherName)) {
        inError = true;
        addError("No matcher name specified");
        return;
      } else {
        matcher.setName(matcherName);
        addInfo("matcher named as [" + matcherName + "]");
      }

      JaninoEventEvaluatorBase janinoEvaluator = (JaninoEventEvaluatorBase) ec
          .peekObject();
      janinoEvaluator.addMatcher(matcher);
      
      ec.pushObject(matcher);
    } catch (Exception oops) {
      inError = true;
      addError("Could not attach matcher to JaninoEventEvaluator",
          oops);
      throw new ActionException(SkipCode.SKIP_CHILDREN, oops);
    }
  }

  public void end(InterpretationContext ec, String name) {
    if (inError) {
      return;
    }

    if (OptionHelper.isEmpty(matcher.getRegex())) {
      addError("No regex specified for matcher named ["+matcher.getName()+"]");
      return;
    } else {
      matcher.start();  
    }
    
    
    Object o = ec.peekObject();
    if (o != matcher) {
      addWarn(
        "The object at the of the stack is not the matcher named ["
        + matcher.getName() + "] pushed earlier.");
    } else {
      
      addInfo(
        "Popping appender named [" + matcher.getName()
        + "] from the object stack");
      ec.popObject();
    }
  }

}
