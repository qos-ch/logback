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



import java.util.Map;

import org.xml.sax.Attributes;

import ch.qos.logback.core.CoreGlobal;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.OptionHelper;


abstract public class AbstractEventEvaluatorAction extends Action {
  
  EventEvaluator evaluator;
  boolean inError = false;

  /**
   * Instantiates an evaluator of the given class and sets its name.
   */
  public void begin(InterpretationContext ec, String name, Attributes attributes) {
    // Let us forget about previous errors (in this instance)
    inError = false;
    evaluator = null;
    
    String className = attributes.getValue(CLASS_ATTRIBUTE);
    if(OptionHelper.isEmpty(className)) {
      className = defaultClassName();
      addWarn("Assuming default evaluator class ["+className+"]");
    }

    if(OptionHelper.isEmpty(className)) {
      className = defaultClassName();
      inError = true;
      addError("Mandatory \""+CLASS_ATTRIBUTE+"\" attribute not set for <evaluator>");
      return;
    }
    
    String evaluatorName = attributes.getValue(Action.NAME_ATTRIBUTE);
    if(OptionHelper.isEmpty(evaluatorName)) {
      inError = true;
      addError("Mandatory \""+NAME_ATTRIBUTE+"\" attribute not set for <evaluator>");
      return;
    }
    try {
      evaluator = (EventEvaluator)
        OptionHelper.instantiateByClassName(
          className, ch.qos.logback.core.boolex.EventEvaluator.class, context);
      
      if(isOfCorrectType(evaluator)) {
        evaluator.setContext(this.context);
        evaluator.setName(evaluatorName);
        
        if (ec.getObjectStack().size() > 0 && ec.peekObject() instanceof EvaluatorFilter) {
          ((EvaluatorFilter)ec.peekObject()).setEvaluator(evaluator);
        }
        
        ec.pushObject(evaluator);        
        addInfo("Adding evaluator named ["+evaluatorName+"] to the object stack");
      } else {
        inError = true;
        addError("Evaluator of type ["+className+"] is not of the desired type");
      }

    } catch (Exception oops) {
      inError = true;
      addError("Could not create evaluator of type " + className + "].", oops);
    }
  }

  /**
   * Is the layout of the desired type?
   * @param layout
   * @return true if the layout is of the correct type
   */
  abstract protected boolean isOfCorrectType(EventEvaluator ee);
  
  /**
   * Returns a default class name in case the class attribute is not specified
   * @return
   */
  abstract protected String defaultClassName();
  
  
  /**
   * Once the children elements are also parsed, now is the time to activate
   * the evaluator options.
   */
  @SuppressWarnings("unchecked")
  public void end(InterpretationContext ec, String e) {
    if (inError) {
      return;
    }

    if (evaluator instanceof LifeCycle) {
      ((LifeCycle) evaluator).start();
      addInfo("Starting evaluator named ["+evaluator.getName()+"]");
    }

    Object o = ec.peekObject();

    if (o != evaluator) {
      addWarn(
        "The object on the top the of the stack is not the evaluator pushed earlier.");
    } else {
      ec.popObject();

      try {
        Map<String, EventEvaluator> evaluatorMap = (Map<String, EventEvaluator>) context.getObject(CoreGlobal.EVALUATOR_MAP);
        evaluatorMap.put(evaluator.getName(), evaluator);
      } catch (Exception ex) {
        addError(
          "Could not set evaluator named ["+evaluator+"].", ex);
      }
    }
  }

  public void finish(InterpretationContext ec) {
  }
}
