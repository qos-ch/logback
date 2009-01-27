/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran.spi;

import java.util.List;

import ch.qos.logback.core.joran.action.Action;

/**
 * 
 * As its name indicates, a RuleStore contains 2-tuples consists of a Pattern
 * and an Action.
 * 
 * <p>As a joran configurator goes through the elements in a document, it asks
 * the rule store whether there are rules matching the current pattern by
 * invoking the {@link #matchActions(Pattern)} method.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public interface RuleStore {
  public void addRule(Pattern pattern, String actionClassStr)
      throws ClassNotFoundException;

  public void addRule(Pattern pattern, Action action);

  public List matchActions(Pattern currentPatern);
}
