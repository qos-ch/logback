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

        LRUCache<String, String> cache = new LRUCache<String, String>(2);
        cache.put("a", "a");
        cache.put("b", "b");
        cache.put("c", "c");
        List<String> witness = new LinkedList<String>();
        witness.add("b");
        witness.add("c");
        assertEquals(witness, cache.keyList());
    }

    @Test
    public void typicalScenarioTest() {
        int simulationLen = 1000 * 10;
        int cacheSize = 100;
        int worldSize = 1000;
        doScenario(simulationLen, cacheSize, worldSize);
    }

    @Test
    public void scenarioCoverageTest() {
        int simulationLen = 1000 * 10;

        int[] cacheSizes = new int[] { 1, 10, 100 };
        // tests with large worldSizes are slow because with a large
        // world size the probability of a cache miss is high.
        int[] worldSizes = new int[] { 1, 10, 100 };

        for (int i = 0; i < cacheSizes.length; i++) {
            for (int j = 0; j < worldSizes.length; j++) {
                doScenario(simulationLen, cacheSizes[i], worldSizes[j]);
            }
        }
    }

    void doScenario(int simulationLen, int cacheSize, int worldSize) {
        int get2PutRatio = 10;
        Simulator simulator = new Simulator(worldSize, get2PutRatio, false);
        List<Event<String>> scenario = simulator.generateScenario(simulationLen);
        LRUCache<String, String> lruCache = new LRUCache<String, String>(cacheSize);
        T_LRUCache<String> tlruCache = new T_LRUCache<String>(cacheSize);
        long start = System.nanoTime();
        simulator.simulate(scenario, lruCache, tlruCache);
        // assertEquals(tlruCache.keyList(), lruCache.keyList());
        long end = System.nanoTime();
        System.out.println("cacheSize=" + cacheSize + ", worldSize=" + worldSize + ", elapsed time=" + ((end - start) / (1000 * 1000)) + " in millis");
    }

    @Test
    @Ignore
    // slow test that is known to pass
    public void multiThreadedScenario() throws InterruptedException {
        int cacheSize = 100;
        int worldSize = cacheSize * 2;
        LRUCache<String, String> lruCache = new LRUCache<String, String>(cacheSize);
        T_LRUCache<String> tlruCache = new T_LRUCache<String>(cacheSize);
        SimulatorRunnable[] simulatorArray = new SimulatorRunnable[5];
        for (int i = 0; i < simulatorArray.length; i++) {
            simulatorArray[i] = new SimulatorRunnable(lruCache, tlruCache, worldSize);
        }
        for (int i = 0; i < simulatorArray.length; i++) {
            simulatorArray[i].start();
        }
        for (int i = 0; i < simulatorArray.length; i++) {
            simulatorArray[i].join();
        }
        assertEquals(tlruCache.keyList(), lruCache.keyList());
    }

    private class SimulatorRunnable extends Thread {

        LRUCache<String, String> lruCache;
        T_LRUCache<String> tlruCache;
        int worldSize;

        SimulatorRunnable(LRUCache<String, String> lruCache, T_LRUCache<String> tlruCache, int worldSize) {
            this.lruCache = lruCache;
            this.tlruCache = tlruCache;
            this.worldSize = worldSize;
        }

        public void run() {
            int get2PutRatio = 10;
            int simulationLen = 1000 * 50;
            Simulator simulator = new Simulator(worldSize, get2PutRatio, true);
            List<Event<String>> scenario = simulator.generateScenario(simulationLen);
            simulator.simulate(scenario, lruCache, tlruCache);
            System.out.println("done");
        }
    }

}
