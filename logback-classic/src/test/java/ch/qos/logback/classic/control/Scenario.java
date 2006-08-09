/** 
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2005, QOS.ch, LOGBack.com
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import ch.qos.logback.classic.Level;

public class Scenario {
  // the frequency of a set levelInt event for every create logger event
  private final static int CREATE_LOGGER_TO_SET_LEVEL_FREQUENCY = 5;

  private List<TestAction> actionList = new Vector<TestAction>();

  public void addAction(CreateLogger action) {
    actionList.add(action);
    if(RandomUtil.oneInFreq(CREATE_LOGGER_TO_SET_LEVEL_FREQUENCY)) {
      Level l = RandomUtil.randomLevel();
      actionList.add(new SetLevel(l, action.getLoggerName()));
    }
  }

  public List<TestAction> getActionList() {
    return new ArrayList<TestAction>(actionList);
  }

  public int size() {
    return actionList.size();
  }

  public TestAction get(int i) {
    return (TestAction) actionList.get(i);
  }
}
