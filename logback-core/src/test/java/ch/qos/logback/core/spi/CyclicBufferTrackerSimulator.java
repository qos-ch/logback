/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.spi;

import java.util.*;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class CyclicBufferTrackerSimulator {

  CyclicBufferTrackerImpl<Object> realAppenderTracker = new CyclicBufferTrackerImpl<Object>();
  CyclicBufferTrackerImpl t_appenderTracker = new CyclicBufferTrackerImpl();

  List<SimulationEvent> scenario = new ArrayList<SimulationEvent>();
  List<String> keySpace = new ArrayList<String>();
  int maxTimestampInc;
  Random randomKeyGen = new Random(100);
  Random random = new Random(11234);

  CyclicBufferTrackerSimulator(int keySpaceLen, int maxTimestampInc) {
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
    long timestamp = 30000;
    int keySpaceLen = keySpace.size();
    for (int i = 0; i < simLen; i++) {
      int index = random.nextInt(keySpaceLen);
      timestamp += random.nextInt(maxTimestampInc);
      String key = keySpace.get(index);
      scenario.add(new SimulationEvent(key, timestamp));
    }
  }

  public void dump() {
    for (SimulationEvent simeEvent : scenario) {
      System.out.println(simeEvent);
    }
  }


  void play(SimulationEvent simulationEvent,
            CyclicBufferTracker<Object> tracker) {
    String key = simulationEvent.key;
    long timestamp = simulationEvent.timestamp;
    tracker.get(key, timestamp);
  }

  public void simulate() {
    for (SimulationEvent simeEvent : scenario) {
      play(simeEvent, realAppenderTracker);
      play(simeEvent, t_appenderTracker);
    }
  }

  // =========================================================================

  class SimulationEvent {
    final public String key;
    final public long timestamp;

    public SimulationEvent(String key, long timestamp) {
      this.key = key;
      this.timestamp = timestamp;
    }

    public String toString() {
      return "Event: k=" + key + ", timestamp=" + timestamp;
    }
  }
}
