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

public class Scenario {

  private List<ScenarioAction> actionList = new Vector<ScenarioAction>();

  public void add(ScenarioAction action) {
    actionList.add(action);
  }

  public List<ScenarioAction> getActionList() {
    return new ArrayList<ScenarioAction>(actionList);
  }

  public int size() {
    return actionList.size();
  }

  public ScenarioAction get(int i) {
    return (ScenarioAction) actionList.get(i);
  }
}
