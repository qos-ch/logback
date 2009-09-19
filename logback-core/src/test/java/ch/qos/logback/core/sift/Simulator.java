/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.appender.NOPAppender;
import ch.qos.logback.core.sift.tracker.AppenderTrackerTImpl;
import ch.qos.logback.core.sift.tracker.SimulationEvent;

/**
 * Simulate use of AppenderTracker by HoardAppender.
 * 
 * @author ceki
 *
 */
public class Simulator {

  AppenderTrackerImpl<Object> appenderTracker = new AppenderTrackerImpl<Object>();
  AppenderTrackerTImpl t_appenderTracker = new AppenderTrackerTImpl();

  List<String> keySpace = new ArrayList<String>();
  List<SimulationEvent> scenario = new ArrayList<SimulationEvent>();
  Random randomKeyGen = new Random(100);

  Random random = new Random(11234);

  final int maxTimestampInc;
  long timestamp = 30000;

  Simulator(int keySpaceLen, int maxTimestampInc) {
    this.maxTimestampInc = maxTimestampInc;
    Map<String, String> checkMap = new HashMap<String, String>();
    for (int i = 0; i < keySpaceLen; i++) {
      String k = getRandomKeyStr();
      if (checkMap.containsKey(k)) {
        System.out.println("random key collision occured");
        k += "" + i;
      }
      keySpace.add(k);
      checkMap.put(k, k);
    }

  }

  private String getRandomKeyStr() {
    int ri = randomKeyGen.nextInt();
    String s = String.format("%X", ri);
    return s;
  }

  void buildScenario(int simLen) {
    int keySpaceLen = keySpace.size();
    for (int i = 0; i < simLen; i++) {
      int index = random.nextInt(keySpaceLen);
      timestamp += random.nextInt(maxTimestampInc);
      String key = keySpace.get(index);
      scenario.add(new SimulationEvent(key, timestamp));
    }
  }

  public void simulate() {
    for (SimulationEvent simeEvent : scenario) {
      play(simeEvent, appenderTracker);
      play(simeEvent, t_appenderTracker);
    }
  }

  void play(SimulationEvent simulationEvent,
      AppenderTracker<Object> appenderTracker) {
    String mdcValue = simulationEvent.key;
    long timestamp = simulationEvent.timestamp;
    Appender<Object> appender = appenderTracker.get(mdcValue, timestamp);
    if (appender == null) {
      appender = new NOPAppender<Object>();
      appenderTracker.put(mdcValue, appender, timestamp);
    }
    appenderTracker.stopStaleAppenders(timestamp);
  }
}
