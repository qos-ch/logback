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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import ch.qos.logback.core.testUtil.RandomUtil;

public class LogbackMDCAdapterTest {

    final static String A_SUFFIX = "A_SUFFIX";
    final static String B_SUFFIX = "B_SUFFIX";

    int diff = RandomUtil.getPositiveInt();

    private final LogbackMDCAdapter mdcAdapter = new LogbackMDCAdapter();

    /**
     * Test that CopyOnInheritThreadLocal does not barf when the
     * MDC hashmap is null
     *
     * @throws InterruptedException
     */
    @Test
    public void LOGBACK_442() throws InterruptedException {
        final Map<String, String> parentHM = getMapFromMDCAdapter(mdcAdapter);
        assertNull(parentHM);

        final ChildThreadForMDCAdapter childThread = new ChildThreadForMDCAdapter(mdcAdapter);
        childThread.start();
        childThread.join();
        assertTrue(childThread.successul);
        assertNull(childThread.childHM);
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
    public void sequenceWithGet() {
        mdcAdapter.put("k0", "v0");
        final Map<String, String> map0 = mdcAdapter.copyOnThreadLocal.get();
        mdcAdapter.get("k0");
        mdcAdapter.put("k1", "v1"); // no map copy required

        // verify that map0 is the same instance and that value was updated
        assertSame(map0, mdcAdapter.copyOnThreadLocal.get());
    }

    @Test
    public void sequenceWithGetPropertyMap() {
        mdcAdapter.put("k0", "v0");
        final Map<String, String> map0 = mdcAdapter.getPropertyMap(); // point 0
        mdcAdapter.put("k0", "v1"); // new map should be created
        // verify that map0 is that in point 0
        assertEquals("v0", map0.get("k0"));
    }

    @Test
    public void sequenceWithCopyContextMap() {
        mdcAdapter.put("k0", "v0");
        final Map<String, String> map0 = mdcAdapter.copyOnThreadLocal.get();
        mdcAdapter.getCopyOfContextMap();
        mdcAdapter.put("k1", "v1"); // no map copy required

        // verify that map0 is the same instance and that value was updated
        assertSame(map0, mdcAdapter.copyOnThreadLocal.get());
    }

    // =================================================

    /**
     * Test that LogbackMDCAdapter does not copy its hashmap when a child
     * thread inherits it.
     *
     * @throws InterruptedException
     */
    @Test
    public void noCopyOnInheritenceTest() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final String firstKey = "x" + diff;
        final String secondKey = "o" + diff;
        mdcAdapter.put(firstKey, firstKey + A_SUFFIX);

        final ChildThread childThread = new ChildThread(mdcAdapter, firstKey, secondKey, countDownLatch);
        childThread.start();
        countDownLatch.await();
        mdcAdapter.put(firstKey, firstKey + B_SUFFIX);
        childThread.join();

        assertNull(mdcAdapter.get(secondKey));
        assertTrue(childThread.successful);

        final Map<String, String> parentHM = getMapFromMDCAdapter(mdcAdapter);
        assertTrue(parentHM != childThread.childHM);

        final HashMap<String, String> parentHMWitness = new HashMap<>();
        parentHMWitness.put(firstKey, firstKey + B_SUFFIX);
        assertEquals(parentHMWitness, parentHM);

        final HashMap<String, String> childHMWitness = new HashMap<>();
        childHMWitness.put(secondKey, secondKey + A_SUFFIX);
        assertEquals(childHMWitness, childThread.childHM);

    }

    // see also http://jira.qos.ch/browse/LBCLASSIC-253
    @Test
    public void clearOnChildThreadShouldNotAffectParent() throws InterruptedException {
        final String firstKey = "x" + diff;
        final String secondKey = "o" + diff;

        mdcAdapter.put(firstKey, firstKey + A_SUFFIX);
        assertEquals(firstKey + A_SUFFIX, mdcAdapter.get(firstKey));

        final Thread clearer = new ChildThread(mdcAdapter, firstKey, secondKey) {
            @Override
            public void run() {
                mdcAdapter.clear();
                assertNull(mdcAdapter.get(firstKey));
            }
        };

        clearer.start();
        clearer.join();

        assertEquals(firstKey + A_SUFFIX, mdcAdapter.get(firstKey));
    }

    // see http://jira.qos.ch/browse/LBCLASSIC-289
    // this test used to fail without synchronization code in LogbackMDCAdapter
    @Test
    public void nearSimultaneousPutsShouldNotCauseConcurrentModificationException() throws InterruptedException {
        // For the weirdest reason, modifications to mdcAdapter must be done
        // before the definition anonymous ChildThread class below. Otherwise, the
        // map in the child thread, the one contained in mdcAdapter.copyOnInheritThreadLocal,
        // is null. How strange is that?

        // let the map have lots of elements so that copying it takes time
        for (int i = 0; i < 2048; i++) {
            mdcAdapter.put("k" + i, "v" + i);
        }

        final ChildThread childThread = new ChildThread(mdcAdapter, null, null) {
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
        assertTrue(childThread.successful);
    }

    Map<String, String> getMapFromMDCAdapter(final LogbackMDCAdapter lma) {
        final ThreadLocal<Map<String, String>> copyOnThreadLocal = lma.copyOnThreadLocal;
        return copyOnThreadLocal.get();
    }

    // ========================== various thread classes
    class ChildThreadForMDCAdapter extends Thread {

        LogbackMDCAdapter logbackMDCAdapter;
        boolean successul;
        Map<String, String> childHM;

        ChildThreadForMDCAdapter(final LogbackMDCAdapter logbackMDCAdapter) {
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

        ChildThread(final LogbackMDCAdapter logbackMDCAdapter) {
            this(logbackMDCAdapter, null, null);
        }

        ChildThread(final LogbackMDCAdapter logbackMDCAdapter, final String firstKey, final String secondKey) {
            this(logbackMDCAdapter, firstKey, secondKey, null);
        }

        ChildThread(final LogbackMDCAdapter logbackMDCAdapter, final String firstKey, final String secondKey, final CountDownLatch countDownLatch) {
            super("chil");
            this.logbackMDCAdapter = logbackMDCAdapter;
            this.firstKey = firstKey;
            this.secondKey = secondKey;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            logbackMDCAdapter.put(secondKey, secondKey + A_SUFFIX);
            assertNull(logbackMDCAdapter.get(firstKey));
            if (countDownLatch != null) {
                countDownLatch.countDown();
            }
            assertNotNull(logbackMDCAdapter.get(secondKey));
            assertEquals(secondKey + A_SUFFIX, logbackMDCAdapter.get(secondKey));

            successful = true;
            childHM = getMapFromMDCAdapter(logbackMDCAdapter);
        }
    }
}
