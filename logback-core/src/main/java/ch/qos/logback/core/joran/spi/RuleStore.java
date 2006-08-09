/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.joran.spi;

import java.util.List;

import ch.qos.logback.core.joran.action.Action;



public interface RuleStore {
  public void addRule(Pattern pattern, String actionClassStr) throws ClassNotFoundException;
  public void addRule(Pattern pattern, Action action);
  
  public List matchActions(Pattern pattern);
}
