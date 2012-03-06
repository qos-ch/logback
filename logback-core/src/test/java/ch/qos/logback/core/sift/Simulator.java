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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.helpers.NOPAppender;
import ch.qos.logback.core.sift.tracker.AppenderTrackerTImpl;
import ch.qos.logback.core.sift.tracker.SimulationEvent;
import ch.qos.logback.core.sift.tracker.SimulationEvent.SimEventType;

/**
 * Simulate use of AppenderTracker by SiftingAppender.
 * 
 * @author ceki
 * 
 */
public class Simulator {

  AppenderTrackerImpl<Object> realAppenderTracker = new AppenderTrackerImpl<Object>();
  AppenderTrackerTImpl t_appenderTracker = new AppenderTrackerTImpl();

  List<String> keySpace = new ArrayList<String>();
  List<SimulationEvent> scenario = new ArrayList<SimulationEvent>();
  Random randomKeyGen = new Random(100);

  Random random = new Random(11234);

  final int maxTimestampInc;
  final int inverseOfRemoveProbability;

  Simulator(int keySpaceLen, int maxTimestampInc, int inverseOfRemoveProbability) {
    this.maxTimestampInc = maxTimestampInc;
    this.inverseOfRemoveProbability = inverseOfRemoveProbability;
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
    return String.format("%X", ri);
  }

  void buildScenario(int simLen) {
    long timestamp = 30000;
    int keySpaceLen = keySpace.size();
    for (int i = 0; i < simLen; i++) {
      int index = random.nextInt(keySpaceLen);
      timestamp += random.nextInt(maxTimestampInc);
      String key = keySpace.get(index);
      SimEventType eventType = SimEventType.PUT;
      
      int removeNow = random.nextInt(inverseOfRemoveProbability);
      if (removeNow == 0) {
        eventType = SimEventType.REMOVE_NOW;
      }
      scenario.add(new SimulationEvent(eventType, key, timestamp));
    }
  }

  void dump() {
    for (SimulationEvent simeEvent : scenario) {
      System.out.println(simeEvent);
    }
  }

  public void simulate() {
    for (SimulationEvent simeEvent : scenario) {
      play(simeEvent, realAppenderTracker);
      play(simeEvent, t_appenderTracker);
    }
  }

  void play(SimulationEvent simulationEvent,
      AppenderTracker<Object> appenderTracker) {
    String key = simulationEvent.key;
    long timestamp = simulationEvent.timestamp;

    switch (simulationEvent.simEventType) {
    case PUT:
      doPut(appenderTracker, key, timestamp);
      break;
    case REMOVE_NOW:
      doRemoveNow(appenderTracker, key);
      break;
    }

  }

  void doPut(AppenderTracker<Object> appenderTracker, String key, long timestamp) {
    Appender<Object> appender = appenderTracker.get(key, timestamp);
    if (appender == null) {
      appender = new NOPAppender<Object>();
      appender.start();
      appenderTracker.put(key, appender, timestamp);
    }
    appenderTracker.stopStaleAppenders(timestamp);
  }

  int i = 0;

  void doRemoveNow(AppenderTracker<Object> appenderTracker, String key) {
    // System.out.println("doRemoveNow "+(i++));
    appenderTracker.stopAndRemoveNow(key);
  }

}
