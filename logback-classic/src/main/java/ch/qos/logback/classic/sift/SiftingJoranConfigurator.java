/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.sift;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.DefaultNestedComponentRules;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.action.ActionConst;
import ch.qos.logback.core.joran.action.AppenderAction;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.sift.SiftingJoranConfiguratorBase;

public class SiftingJoranConfigurator  extends SiftingJoranConfiguratorBase<ILoggingEvent> {

  String key;
  String value;
  
  SiftingJoranConfigurator(String key, String value) {
    this.key = key;
    this.value = value;
  }
  
  @Override
  protected Pattern initialPattern() {
    return new Pattern("configuration");
  }
  
  @Override
  protected void addInstanceRules(RuleStore rs) {
    rs.addRule(new Pattern("configuration/appender"), new AppenderAction());
  }


  @Override
  protected void addDefaultNestedComponentRegistryRules(
      DefaultNestedComponentRegistry registry) {
    DefaultNestedComponentRules.addDefaultNestedComponentRegistryRules(registry);
  }
    
  @Override
  protected void buildInterpreter() {
    super.buildInterpreter();
    Map<String, Object> omap = interpreter.getInterpretationContext().getObjectMap();
    omap.put(ActionConst.APPENDER_BAG, new HashMap());
    omap.put(ActionConst.FILTER_CHAIN_BAG, new HashMap());
    Map<String, String> propertiesMap = new HashMap<String, String>();
    propertiesMap.put(key, value);
    interpreter.setInterpretationContextPropertiesMap(propertiesMap);
  }

  @SuppressWarnings("unchecked")
  public Appender<ILoggingEvent> getAppender() {
    Map<String, Object> omap = interpreter.getInterpretationContext().getObjectMap();
    HashMap map = (HashMap) omap.get(ActionConst.APPENDER_BAG);
    Collection values = map.values();
    return (Appender<ILoggingEvent>) values.iterator().next();
  }
}
