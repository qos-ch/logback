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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulator {

    Random random;

    int worldSize;
    int get2PutRatio;
    boolean multiThreaded;

    public Simulator(int worldSize, int get2PutRatio, boolean multiThreaded) {
        this.worldSize = worldSize;
        this.get2PutRatio = get2PutRatio;
        long seed = System.nanoTime();
        // System.out.println("seed is "+seed);
        random = new Random(seed);
        this.multiThreaded = multiThreaded;
    }

    public List<Event> generateScenario(int len) {
        List<Event> scenario = new ArrayList<Event>();

        for (int i = 0; i < len; i++) {

            int r = random.nextInt(get2PutRatio);
            boolean put = false;
            if (r == 0) {
                put = true;
            }
            r = random.nextInt(worldSize);
            Event<String> e = new Event<String>(put, String.valueOf(r));
            scenario.add(e);
        }
        return scenario;
    }

    public void simulate(List<Event> scenario, LRUCache<String, String> lruCache, T_LRUCache<String> tlruCache) {
        for (Event<String> e : scenario) {
            if (e.put) {
                lruCache.put(e.k, e.k);
                tlruCache.put(e.k);
            } else {
                String r0 = lruCache.get(e.k);
                String r1 = tlruCache.get(e.k);
                if (!multiThreaded) {
                    // if the simulation is used in a multi-threaded
                    // context, then the state of lruCache may be different than
                    // that of tlruCache. In single threaded mode, they should
                    // return the same values all the time
                    if (r0 != null) {
                        assertEquals(r0, e.k);
                    }
                    assertEquals(r0, r1);
                }
            }
        }
    }

    // void compareAndDumpIfDifferent(LRUCache<String, String> lruCache,
    // T_LRUCache<String> tlruCache) {
    // lruCache.dump();
    // tlruCache.dump();
    // if(!lruCache.keyList().equals(tlruCache.ketList())) {
    // lruCache.dump();
    // tlruCache.dump();
    // throw new AssertionFailedError("s");
    // }
    // }
}
