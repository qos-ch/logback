/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.qos.logback.core.helpers.CyclicBuffer;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class CyclicBufferTrackerSimulator {

	static class Parameters {
		public int keySpaceLen;
		public int maxTimestampInc;
		public int simulationLength;
	}

	CyclicBufferTracker<Object> realCBTracker = new CyclicBufferTracker<>();
	CyclicBufferTrackerT<Object> t_CBTracker = new CyclicBufferTrackerT<>();

	List<SimulationEvent> scenario = new ArrayList<>();
	List<String> keySpace = new ArrayList<>();
	Random randomKeyGen = new Random(100);
	Random simulatorRandom = new Random(11234);
	Parameters params;

	int getToEndOfLifeRatio = 10;

	CyclicBufferTrackerSimulator(final Parameters params) {
		this.params = params;
		final Map<String, String> checkMap = new HashMap<>();
		for (int i = 0; i < params.keySpaceLen; i++) {
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
		final int ri = randomKeyGen.nextInt();
		return String.format("%X", ri);
	}

	void buildScenario() {
		long timestamp = 30000;
		final int keySpaceLen = keySpace.size();
		for (int i = 0; i < params.simulationLength; i++) {
			final int keyIndex = simulatorRandom.nextInt(keySpaceLen);
			timestamp += simulatorRandom.nextInt(params.maxTimestampInc);
			final String key = keySpace.get(keyIndex);
			scenario.add(new SimulationEvent(EventType.INSERT, key, timestamp));
			if (simulatorRandom.nextInt(getToEndOfLifeRatio) == 0) {
				scenario.add(new SimulationEvent(EventType.END_OF_LIFE, key, timestamp));
			}
			scenario.add(new SimulationEvent(EventType.REMOVE_STALE, key, timestamp));
		}
	}

	public void dump() {
		for (final SimulationEvent simeEvent : scenario) {
			System.out.println(simeEvent);
		}
	}

	void play(final SimulationEvent simulationEvent, final ComponentTracker<CyclicBuffer<Object>> tracker) {
		final String key = simulationEvent.key;
		final long timestamp = simulationEvent.timestamp;
		final EventType eventType = simulationEvent.eventType;
		switch (eventType) {
		case INSERT:
			tracker.getOrCreate(key, timestamp);
			break;
		case END_OF_LIFE:
			tracker.endOfLife(key);
			break;
		case REMOVE_STALE:
			tracker.removeStaleComponents(timestamp);
			break;
		}
	}

	public void simulate() {
		for (final SimulationEvent simeEvent : scenario) {
			play(simeEvent, realCBTracker);
			play(simeEvent, t_CBTracker);
		}
	}

	// =========================================================================
	enum EventType {
		INSERT, END_OF_LIFE, REMOVE_STALE;
	}

	class SimulationEvent {
		final public String key;
		final public long timestamp;
		final EventType eventType;

		public SimulationEvent(final EventType eventType, final String key, final long timestamp) {
			this.eventType = eventType;
			this.key = key;
			this.timestamp = timestamp;
		}

		@Override
		public String toString() {
			return "SimulationEvent{" + "eventType=" + eventType + ", key='" + key + '\'' + ", timestamp=" + timestamp + '}';
		}
	}
}
