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
package ch.qos.logback.classic.util;

import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class LogbackMDCAdapterTest {

    final static String A_SUFFIX = "A_SUFFIX";
    final static String B_SUFFIX = "B_SUFFIX";

    int diff = RandomUtil.getPositiveInt();

    private final LogbackMDCAdapter mdcAdapter = new LogbackMDCAdapter();

    /**
     * Test that CopyOnInheritThreadLocal does not barf when the MDC hashmap is null
     *
     * @throws InterruptedException
     */
    @Test
    public void LOGBACK_442() throws InterruptedException {
        Map<String, String> parentHM = getMapFromMDCAdapter(mdcAdapter);
        Assertions.assertNull(parentHM);

        ChildThreadForMDCAdapter childThread = new ChildThreadForMDCAdapter(mdcAdapter);
        childThread.start();
        childThread.join();
        Assertions.assertTrue(childThread.successul);
        Assertions.assertNull(childThread.childHM);
    }

    @Test
    public void removeForNullKeyTest() {
        mdcAdapter.remove(null);
    }

    @Test
    public void removeInexistentKey() {
        mdcAdapter.remove("abcdlw0");
    }

    @Test
    @Disabled
    public void sequenceWithGet() {
        mdcAdapter.put("k0", "v0");
        Map<String, String> map0 = mdcAdapter.getPropertyMap();
        mdcAdapter.get("k0");
        mdcAdapter.put("k1", "v1"); // no map copy required

        Map<String, String> witness = new HashMap<>();
        witness.put("k0", "v0");
        witness.put("k1", "v1");

        Assertions.assertEquals(witness, mdcAdapter.getPropertyMap());
    }

    @Test
    public void sequenceWithGetPropertyMap() {
        mdcAdapter.put("k0", "v0");
        Map<String, String> map0 = mdcAdapter.getPropertyMap(); // point 0
        mdcAdapter.put("k0", "v1"); // new map should be created
        // verify that map0 is that in point 0
        Assertions.assertEquals("v0", map0.get("k0"));
    }

    @Test
    public void basicGetPropertyMap() {
        mdcAdapter.put("k0", "v0");
        mdcAdapter.put("k1", "v1");

        Map<String, String> map0 = mdcAdapter.getPropertyMap(); // point 0
        mdcAdapter.put("k0", "v1"); // new map should be created
        // verify that map0 is that in point 0
        Assertions.assertEquals("v0", map0.get("k0"));
        Assertions.assertEquals("v1", map0.get("k1"));

    }

    @Test
    @Disabled
    public void sequenceWithCopyContextMap() {
        mdcAdapter.put("k0", "v0");
        Map<String, String> map0 = mdcAdapter.getPropertyMap();
        mdcAdapter.getCopyOfContextMap();
        mdcAdapter.put("k1", "v1"); // no map copy required

        // verify that map0 is the same instance and that value was updated
        Assertions.assertSame(map0, mdcAdapter.getPropertyMap());
    }

    // =================================================

    /**
     * Test that LogbackMDCAdapter does not copy its hashmap when a child thread
     * inherits it.
     *
     * @throws InterruptedException
     */
    @Test
    public void noCopyOnInheritenceTest() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String firstKey = "x" + diff;
        String secondKey = "o" + diff;
        mdcAdapter.put(firstKey, firstKey + A_SUFFIX);

        ChildThread childThread = new ChildThread(mdcAdapter, firstKey, secondKey, countDownLatch);
        childThread.start();
        countDownLatch.await();
        mdcAdapter.put(firstKey, firstKey + B_SUFFIX);
        childThread.join();

        Assertions.assertNull(mdcAdapter.get(secondKey));
        Assertions.assertTrue(childThread.successful);

        Map<String, String> parentHM = getMapFromMDCAdapter(mdcAdapter);
        Assertions.assertTrue(parentHM != childThread.childHM);

        HashMap<String, String> parentHMWitness = new HashMap<String, String>();
        parentHMWitness.put(firstKey, firstKey + B_SUFFIX);
        Assertions.assertEquals(parentHMWitness, parentHM);

        HashMap<String, String> childHMWitness = new HashMap<String, String>();
        childHMWitness.put(secondKey, secondKey + A_SUFFIX);
        Assertions.assertEquals(childHMWitness, childThread.childHM);

    }

    // see also https://jira.qos.ch/browse/LOGBACK-325
    @Test
    public void clearOnChildThreadShouldNotAffectParent() throws InterruptedException {
        String firstKey = "x" + diff;
        String secondKey = "o" + diff;

        mdcAdapter.put(firstKey, firstKey + A_SUFFIX);
        Assertions.assertEquals(firstKey + A_SUFFIX, mdcAdapter.get(firstKey));

        Thread clearer = new ChildThread(mdcAdapter, firstKey, secondKey) {
            @Override
            public void run() {
                mdcAdapter.clear();
                Assertions.assertNull(mdcAdapter.get(firstKey));
            }
        };

        clearer.start();
        clearer.join();

        Assertions.assertEquals(firstKey + A_SUFFIX, mdcAdapter.get(firstKey));
    }

    // see https://jira.qos.ch/browse/LOGBACK-434
    // this test used to fail without synchronization code in LogbackMDCAdapter
    @Test
    public void nearSimultaneousPutsShouldNotCauseConcurrentModificationException() throws InterruptedException {
        // For the weirdest reason, modifications to mdcAdapter must be done
        // before the definition anonymous ChildThread class below. Otherwise, the
        // map in the child thread, the one contained in
        // mdcAdapter.copyOnInheritThreadLocal,
        // is null. How strange is that?

        // let the map have lots of elements so that copying it takes time
        for (int i = 0; i < 2048; i++) {
            mdcAdapter.put("k" + i, "v" + i);
        }

        ChildThread childThread = new ChildThread(mdcAdapter, null, null) {
            @Override
            public void run() {
                for (int i = 0; i < 16; i++) {
                    mdcAdapter.put("ck" + i, "cv" + i);
                    Thread.yield();
                }
                successful = true;
            }
        };

        childThread.start();
        Thread.sleep(1);
        for (int i = 0; i < 16; i++) {
            mdcAdapter.put("K" + i, "V" + i);
        }
        childThread.join();
        Assertions.assertTrue(childThread.successful);
    }

    Map<String, String> getMapFromMDCAdapter(LogbackMDCAdapter lma) {
        ThreadLocal<Map<String, String>> tlMap = lma.readWriteThreadLocalMap;
        return tlMap.get();
    }

    // ========================== various thread classes
    class ChildThreadForMDCAdapter extends Thread {

        LogbackMDCAdapter logbackMDCAdapter;
        boolean successul;
        Map<String, String> childHM;

        ChildThreadForMDCAdapter(LogbackMDCAdapter logbackMDCAdapter) {
            this.logbackMDCAdapter = logbackMDCAdapter;
        }

        @Override
        public void run() {
            childHM = getMapFromMDCAdapter(logbackMDCAdapter);
            logbackMDCAdapter.get("");
            successul = true;
        }
    }

    class ChildThread extends Thread {

        LogbackMDCAdapter logbackMDCAdapter;
        String firstKey;
        String secondKey;
        boolean successful;
        Map<String, String> childHM;
        CountDownLatch countDownLatch;

        ChildThread(LogbackMDCAdapter logbackMDCAdapter) {
            this(logbackMDCAdapter, null, null);
        }

        ChildThread(LogbackMDCAdapter logbackMDCAdapter, String firstKey, String secondKey) {
            this(logbackMDCAdapter, firstKey, secondKey, null);
        }

        ChildThread(LogbackMDCAdapter logbackMDCAdapter, String firstKey, String secondKey,
                CountDownLatch countDownLatch) {
            super("chil");
            this.logbackMDCAdapter = logbackMDCAdapter;
            this.firstKey = firstKey;
            this.secondKey = secondKey;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            logbackMDCAdapter.put(secondKey, secondKey + A_SUFFIX);
            Assertions.assertNull(logbackMDCAdapter.get(firstKey));
            if (countDownLatch != null)
                countDownLatch.countDown();
            Assertions.assertNotNull(logbackMDCAdapter.get(secondKey));
            Assertions.assertEquals(secondKey + A_SUFFIX, logbackMDCAdapter.get(secondKey));

            successful = true;
            childHM = getMapFromMDCAdapter(logbackMDCAdapter);
        }
    }
}
