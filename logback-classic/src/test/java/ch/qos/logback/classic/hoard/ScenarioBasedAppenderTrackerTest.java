/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.hoard;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

public class ScenarioBasedAppenderTrackerTest {

  Simulator simulator;

  void verify() {
    AppenderTracker at = simulator.appenderTracker;
    AppenderTracker t_at = simulator.t_appenderTracker;
    //List<String> resultKeys = at.keyList();
    //List<String> witnessKeys = t_at.keyList();
    assertEquals(t_at.keyList(), at.keyList());
  }

  @Test
  public void shortTest() {
    simulator = new Simulator(20, AppenderTracker.THRESHOLD / 2);
    simulator.buildScenario(200);
    simulator.simulate();
    verify();
  }

  @Test
  public void mediumTest() {
    simulator = new Simulator(100, AppenderTracker.THRESHOLD / 2);
    simulator.buildScenario(20000);
    simulator.simulate();
    verify();
  }

  @Test
  @Ignore
  public void longetTest() {
    simulator = new Simulator(100, AppenderTracker.THRESHOLD / 200);
    simulator.buildScenario(2000000);
    simulator.simulate();
    verify();
  }
}
