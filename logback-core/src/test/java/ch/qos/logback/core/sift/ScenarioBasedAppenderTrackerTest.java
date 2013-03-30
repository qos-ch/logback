/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.sift;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

public class ScenarioBasedAppenderTrackerTest {

  Simulator simulator;

  int INVERSE_OF_NO_REMOVE = Integer.MAX_VALUE;
  
  
  void verify() {
    AppenderTracker at = simulator.realAppenderTracker;
    AppenderTracker t_at = simulator.t_appenderTracker;
    assertEquals(t_at.keyList(), at.keyList());
  }
  
  @Test
  public void shortTest() {
    simulator = new Simulator(20, AppenderTracker.DEFAULT_TIMEOUT / 2, INVERSE_OF_NO_REMOVE);
    simulator.buildScenario(200);
    simulator.simulate();
    verify();
  }


  @Test
  public void shortTestWithRemovals() {
    simulator = new Simulator(10, AppenderTracker.DEFAULT_TIMEOUT / 10, 2);
    simulator.buildScenario(200);
    simulator.simulate();
    verify();
  }
  
  @Test
  public void mediumTest() {
    simulator = new Simulator(100, AppenderTracker.DEFAULT_TIMEOUT / 2, INVERSE_OF_NO_REMOVE);
    simulator.buildScenario(20000);
    simulator.simulate();
    verify();
  }

  @Test
  public void mediumTestWithRemovals() {
    simulator = new Simulator(10, AppenderTracker.DEFAULT_TIMEOUT / 100, 2);
    simulator.buildScenario(20000);
    simulator.simulate();
    verify();
  }
  
  @Test
  @Ignore
  public void longTest() {
    simulator = new Simulator(100, AppenderTracker.DEFAULT_TIMEOUT / 200, 10);
    simulator.buildScenario(2000000);
    simulator.simulate();
    verify();
  }
}
