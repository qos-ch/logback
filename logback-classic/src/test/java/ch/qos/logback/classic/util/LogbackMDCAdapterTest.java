/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.StatusPrinter;
import ch.qos.logback.core.util.StatusPrinter2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
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
    public void nullValueIsStoredAndWarned() {
        LoggerContext context = new LoggerContext();
        mdcAdapter.setContext(context);
        mdcAdapter.put("k", "v");

        StatusChecker checker = new StatusChecker(context);
        checker.assertNoMatch("Null value for MDC key");

        mdcAdapter.put("k", null);
        StatusPrinter2 s2 = new StatusPrinter2();
        s2.print(context);
        Assertions.assertTrue(mdcAdapter.getPropertyMap().containsKey("k"));
        Assertions.assertNull(mdcAdapter.get("k"));
        checker.assertContainsMatch(Status.WARN,
                "Null value for MDC key \\[k\\] is stored. Null values are deprecated; use MDC.remove\\(\"k\"\\) instead.");

        Status status = statusWithCallerData(context);
        StackTraceElement[] stack = status.getThrowable().getStackTrace();
        Assertions.assertInstanceOf(CallerDataComputingException.class, status.getThrowable());
        Assertions.assertTrue(stack.length > 0 && stack.length <= LogbackMDCAdapter.NULL_VALUE_CALLER_DATA_DEPTH);
        Assertions.assertEquals("nullValueIsStoredAndWarned", stack[0].getMethodName());
        assertCallerDataExcludesAdapter(stack);
    }

    @Test
    public void nullValueCallerDataIsCappedAtEightFrames() {
        LoggerContext context = new LoggerContext();
        mdcAdapter.setContext(context);
        putNullAtDepth(20);

        Status status = statusWithCallerData(context);
        StackTraceElement[] stack = status.getThrowable().getStackTrace();
        Assertions.assertEquals(LogbackMDCAdapter.NULL_VALUE_CALLER_DATA_DEPTH, stack.length);
        for (StackTraceElement frame : stack) {
            Assertions.assertEquals("putNullAtDepth", frame.getMethodName());
        }
        assertCallerDataExcludesAdapter(stack);
    }

    @Test
    public void nullValueWarningIsLimitedToABatchThenTwelveHours() {
        LoggerContext context = new LoggerContext();
        LogbackMDCAdapter adapter = new LogbackMDCAdapter();
        adapter.setContext(context);
        long start = 1_000L;
        adapter.setCurrentTime(start);

        int batch = LogbackMDCAdapter.DEFAULT_BATCH_SIZE;
        for (int i = 0; i < batch; i++) {
            adapter.put("k", null);
        }
        StatusChecker checker = new StatusChecker(context);
        checker.assertMatchCount("Null value for MDC key \\[k\\] is stored.", batch);

        adapter.put("k", null);
        checker.assertMatchCount("Null value for MDC key \\[k\\] is stored.", batch);

        long lull = Duration.buildByHours(LogbackMDCAdapter.DEFAULT_LULL_IN_HOURS).getMilliseconds();
        adapter.setCurrentTime(start + lull - 1);
        adapter.put("k", null);
        checker.assertMatchCount("Null value for MDC key \\[k\\] is stored.", batch);

        adapter.setCurrentTime(start + lull);
        adapter.put("k", null);
        checker.assertMatchCount("Null value for MDC key \\[k\\] is stored.", batch + 1);
    }

    @Test
    public void setContextMapReportsNullKeyAsErrorAndNullValueAsWarning() {
        LoggerContext context = new LoggerContext();
        LogbackMDCAdapter adapter = new LogbackMDCAdapter();
        adapter.setContext(context);

        Map<String, String> clean = new HashMap<>();
        clean.put("ok", "v");
        adapter.setContextMap(clean);
        StatusChecker checker = new StatusChecker(context);
        checker.assertNoMatch("Null ");

        Map<String, String> map = new HashMap<>();
        map.put("ok", "v");
        map.put("n", null);
        map.put(null, null);
        adapter.setContextMap(map);

        Assertions.assertEquals(1, context.getStatusManager().getCount());
        checker.assertContainsMatch(Status.ERROR,
                "Null key in MDC context map is stored. Null values for MDC keys \\[n\\] are also stored. "
                        + "Null values are deprecated; remove such entries instead.");
        checker.assertNoMatch("\\[null\\]");

        Map<String, String> stored = adapter.getPropertyMap();
        Assertions.assertEquals("v", stored.get("ok"));
        Assertions.assertTrue(stored.containsKey("n"));
        Assertions.assertNull(stored.get("n"));
        Assertions.assertTrue(stored.containsKey(null));
        Assertions.assertNull(stored.get(null));
    }

    @Test
    public void setContextMapReportsAllNullValuesInOneWarning() {
        LoggerContext context = new LoggerContext();
        LogbackMDCAdapter adapter = new LogbackMDCAdapter();
        adapter.setContext(context);

        Map<String, String> map = new HashMap<>();
        map.put("ok", "v");
        map.put("a", null);
        map.put("b", null);
        adapter.setContextMap(map);

        Assertions.assertEquals(1, context.getStatusManager().getCount());
        StatusChecker checker = new StatusChecker(context);
        checker.assertContainsMatch(Status.WARN, "Null values for MDC keys \\[(a, b|b, a)\\] are stored. "
                + "Null values are deprecated; remove such entries instead.");
        checker.assertNoMatch("Null key");
    }

    @Test
    public void setContextMapWithManyNullValuesConsumesOneGateToken() {
        LoggerContext context = new LoggerContext();
        LogbackMDCAdapter adapter = new LogbackMDCAdapter();
        adapter.setContext(context);
        adapter.setCurrentTime(1_000L);

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < 2 * LogbackMDCAdapter.DEFAULT_BATCH_SIZE; i++) {
            map.put("k" + i, null);
        }
        adapter.setContextMap(map);
        StatusChecker checker = new StatusChecker(context);
        checker.assertMatchCount("Null values for MDC keys", 1);

        map.put(null, "v");
        adapter.setContextMap(map);
        Assertions.assertEquals(2, context.getStatusManager().getCount());
        checker.assertContainsMatch(Status.ERROR, "Null key in MDC context map is stored.");
    }

    @Test
    public void setContextMapCapsNullValueKeysInMessage() {
        LoggerContext context = new LoggerContext();
        LogbackMDCAdapter adapter = new LogbackMDCAdapter();
        adapter.setContext(context);

        int cap = LogbackMDCAdapter.MAX_NULL_VALUE_KEYS_IN_MSG;
        Map<String, String> atCap = new HashMap<>();
        for (int i = 0; i < cap; i++) {
            atCap.put("k" + i, null);
        }
        adapter.setContextMap(atCap);

        Map<String, String> overCap = new HashMap<>();
        for (int i = 0; i < cap + 5; i++) {
            overCap.put("k" + i, null);
        }
        adapter.setContextMap(overCap);

        List<Status> statusList = context.getStatusManager().getCopyOfStatusList();
        Assertions.assertEquals(2, statusList.size());

        String atCapMsg = statusList.get(0).getMessage();
        Assertions.assertEquals(cap, countListedKeys(atCapMsg));
        Assertions.assertFalse(atCapMsg.contains("more)"));

        String overCapMsg = statusList.get(1).getMessage();
        Assertions.assertEquals(cap, countListedKeys(overCapMsg));
        Assertions.assertTrue(overCapMsg.startsWith("Null values for MDC keys ["));
        Assertions.assertTrue(overCapMsg.contains("] (and 5 more) are stored."), overCapMsg);
    }

    private static int countListedKeys(String msg) {
        String list = msg.substring(msg.indexOf('[') + 1, msg.indexOf(']'));
        return list.split(", ").length;
    }

    private void putNullAtDepth(int depth) {
        if (depth == 0) {
            mdcAdapter.put("deep", null);
        } else {
            putNullAtDepth(depth - 1);
        }
    }

    private static Status statusWithCallerData(LoggerContext context) {
        for (Status status : context.getStatusManager().getCopyOfStatusList()) {
            if (status.getThrowable() instanceof CallerDataComputingException) {
                return status;
            }
        }
        Assertions.fail("no status carried a CallerDataThrowable");
        return null;
    }

    private static void assertCallerDataExcludesAdapter(StackTraceElement[] stack) {
        for (StackTraceElement frame : stack) {
            Assertions.assertNotEquals(LogbackMDCAdapter.class.getName(), frame.getClassName());
            Assertions.assertNotEquals(CallerDataComputingException.class.getName(), frame.getClassName());
        }
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
