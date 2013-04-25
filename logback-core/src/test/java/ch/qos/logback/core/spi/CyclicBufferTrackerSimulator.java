/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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

import ch.qos.logback.core.helpers.CyclicBuffer;

import java.util.*;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class CyclicBufferTrackerSimulator {

  CyclicBufferTracker<Object> realCBTracker = new CyclicBufferTracker<Object>();
  CyclicBufferTracker_TImpl<Object> t_CBTracker = new CyclicBufferTracker_TImpl<Object>();

  List<SimulationEvent> scenario = new ArrayList<SimulationEvent>();
  List<String> keySpace = new ArrayList<String>();
  int maxTimestampInc;
  Random randomKeyGen = new Random(100);
  Random simulatorRandom = new Random(11234);

  int deleteToInsertRatio = 10;

  CyclicBufferTrackerSimulator(int keySpaceLen, int maxTimestampInc) {
    this.maxTimestampInc = maxTimestampInc;
    Map<String, String> checkMap = new HashMap<String, String>();
    for (int i = 0; i < keySpaceLen; i++) {
      String k = getRandomKeyStr();
      if (checkMap.containsKey(k)) {
        System.out.println("random key collision occurred");
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
      int index = simulatorRandom.nextInt(keySpaceLen);
      timestamp += simulatorRandom.nextInt(maxTimestampInc);
      EventType eventType = EventType.INSERT;
      if (simulatorRandom.nextInt(deleteToInsertRatio) == 0) {
        eventType = EventType.DELETE;
      }

      String key = keySpace.get(index);
      scenario.add(new SimulationEvent(eventType, key, timestamp));
    }
  }

  public void dump() {
    for (SimulationEvent simeEvent : scenario) {
      System.out.println(simeEvent);
    }
  }


  void play(SimulationEvent simulationEvent,
            ComponentTracker<CyclicBuffer<Object>> tracker) {
    String key = simulationEvent.key;
    long timestamp = simulationEvent.timestamp;
    EventType eventType = simulationEvent.eventType;
    switch (eventType) {
      case INSERT:
        tracker.getOrCreate(key, timestamp);
        break;
      case DELETE:
        tracker.endOfLife(key);
        break;
    }
  }

  public void simulate() {
    for (SimulationEvent simeEvent : scenario) {
      play(simeEvent, realCBTracker);
      play(simeEvent, t_CBTracker);
    }
  }

  // =========================================================================
  enum EventType {
    INSERT, DELETE;
  }

  class SimulationEvent {
    final public String key;
    final public long timestamp;
    final EventType eventType;

    public SimulationEvent(EventType eventType, String key, long timestamp) {
      this.eventType = eventType;
      this.key = key;
      this.timestamp = timestamp;
    }

    public String toString() {
      return "Event: k=" + key + ", timestamp=" + timestamp;
    }
  }
}
