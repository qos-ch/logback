/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.util;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.boolex.JaninoEventEvaluator;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;

/**
 * Contains mappings for the default type of nested components in
 * logback-classic.
 * 
 * @author Ceki Gulcu
 * 
 */
public class DefaultNestedComponentRules {

  static public void addDefaultNestedComponentRegistryRules(
      DefaultNestedComponentRegistry registry) {
    // if you modify the rules here, then do not forget to modify
    // SiftingJoranConfigurator as well.
    registry.add(AppenderBase.class, "layout", PatternLayout.class);
    registry
        .add(EvaluatorFilter.class, "evaluator", JaninoEventEvaluator.class);

  }

}
