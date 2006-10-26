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

import java.util.HashMap;
import java.util.Map;


import org.xml.sax.Attributes;

import ch.qos.logback.core.CoreGlobal;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;



public class ConversionRuleAction extends Action {
  boolean inError = false;
  
  /**
   * Instantiates an layout of the given class and sets its name.
   *
   */
  public void begin(InterpretationContext ec, String localName, Attributes attributes) {
    // Let us forget about previous errors (in this object)
    inError = false;

    String errorMsg;
    String conversionWord =
      attributes.getValue(ActionConst.CONVERSION_WORD_ATTRIBUTE);
    String converterClass =
      attributes.getValue(ActionConst.CONVERTER_CLASS_ATTRIBUTE);

    if (OptionHelper.isEmpty(conversionWord)) {
      inError = true;
      errorMsg = "No 'conversionWord' attribute in <conversionRule>";
      addError(errorMsg);

      return;
    }

    if (OptionHelper.isEmpty(converterClass)) {
      inError = true;
      errorMsg = "No 'converterClass' attribute in <conversionRule>";
      ec.addError(errorMsg);

      return;
    }

    try {
      //getLogger().debug(
      //  "About to add conversion rule [{}, {}] to layout", conversionWord, converterClass);

      //LoggerRepository repository = (LoggerRepository) ec.getObjectStack().get(0);

      Map<String, String> ruleRegistry = (Map) context.getObject(CoreGlobal.PATTERN_RULE_REGISTRY);
      if(ruleRegistry == null) {
        ruleRegistry = new HashMap<String, String>();
        context.putObject(CoreGlobal.PATTERN_RULE_REGISTRY, ruleRegistry);
      }
      // put the new rule into the rule registry
      ruleRegistry.put(conversionWord, converterClass);
      
    } catch (Exception oops) {
      inError = true;
      errorMsg = "Could not add conversion rule to PatternLayout.";
      addError(errorMsg);
    }
  }

  /**
   * Once the children elements are also parsed, now is the time to activate
   * the appender options.
   */
  public void end(InterpretationContext ec, String n) {
  }

  public void finish(InterpretationContext ec) {
  }
}
