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
package ch.qos.logback.classic.turbo.lru;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class LRUCacheTest {

	@Test
	public void smoke() {

		final LRUCache<String, String> cache = new LRUCache<>(2);
		cache.put("a", "a");
		cache.put("b", "b");
		cache.put("c", "c");
		final List<String> witness = new LinkedList<>();
		witness.add("b");
		witness.add("c");
		assertEquals(witness, cache.keyList());
	}

	@Test
	public void typicalScenarioTest() {
		final int simulationLen = 1000 * 10;
		final int cacheSize = 100;
		final int worldSize = 1000;
		doScenario(simulationLen, cacheSize, worldSize);
	}

	@Test
	public void scenarioCoverageTest() {
		final int simulationLen = 1000 * 10;

		final int[] cacheSizes = { 1, 10, 100 };
		// tests with large worldSizes are slow because with a large
		// world size the probability of a cache miss is high.
		final int[] worldSizes = { 1, 10, 100 };

		for (final int element : cacheSizes) {
			for (final int element2 : worldSizes) {
				doScenario(simulationLen, element, element2);
			}
		}
	}

	void doScenario(final int simulationLen, final int cacheSize, final int worldSize) {
		final int get2PutRatio = 10;
		final Simulator simulator = new Simulator(worldSize, get2PutRatio, false);
		final List<Event<String>> scenario = simulator.generateScenario(simulationLen);
		final LRUCache<String, String> lruCache = new LRUCache<>(cacheSize);
		final T_LRUCache<String> tlruCache = new T_LRUCache<>(cacheSize);
		final long start = System.nanoTime();
		simulator.simulate(scenario, lruCache, tlruCache);
		// assertEquals(tlruCache.keyList(), lruCache.keyList());
		final long end = System.nanoTime();
		System.out.println("cacheSize=" + cacheSize + ", worldSize=" + worldSize + ", elapsed time=" + (end - start) / (1000 * 1000) + " in millis");
	}

	@Test
	@Ignore
	// slow test that is known to pass
	public void multiThreadedScenario() throws InterruptedException {
		final int cacheSize = 100;
		final int worldSize = cacheSize * 2;
		final LRUCache<String, String> lruCache = new LRUCache<>(cacheSize);
		final T_LRUCache<String> tlruCache = new T_LRUCache<>(cacheSize);
		final SimulatorRunnable[] simulatorArray = new SimulatorRunnable[5];
		for (int i = 0; i < simulatorArray.length; i++) {
			simulatorArray[i] = new SimulatorRunnable(lruCache, tlruCache, worldSize);
		}
		for (final SimulatorRunnable element : simulatorArray) {
			element.start();
		}
		for (final SimulatorRunnable element : simulatorArray) {
			element.join();
		}
		assertEquals(tlruCache.keyList(), lruCache.keyList());
	}

	private class SimulatorRunnable extends Thread {

		LRUCache<String, String> lruCache;
		T_LRUCache<String> tlruCache;
		int worldSize;

		SimulatorRunnable(final LRUCache<String, String> lruCache, final T_LRUCache<String> tlruCache, final int worldSize) {
			this.lruCache = lruCache;
			this.tlruCache = tlruCache;
			this.worldSize = worldSize;
		}

		@Override
		public void run() {
			final int get2PutRatio = 10;
			final int simulationLen = 1000 * 50;
			final Simulator simulator = new Simulator(worldSize, get2PutRatio, true);
			final List<Event<String>> scenario = simulator.generateScenario(simulationLen);
			simulator.simulate(scenario, lruCache, tlruCache);
			System.out.println("done");
		}
	}

}
