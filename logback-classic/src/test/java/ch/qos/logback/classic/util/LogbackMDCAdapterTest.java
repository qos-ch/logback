/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.slf4j.MDC;

import ch.qos.logback.core.testUtil.RandomUtil;

public class LogbackMDCAdapterTest {

  final static String A_SUFFIX = "A_SUFFIX";
  final static String B_SUFFIX = "B_SUFFIX";

  int diff = RandomUtil.getPositiveInt();


  /**
   * Test that CopyOnInheritThreadLocal does not barf when the
   * MDC hashmap is null
   *
   * @throws InterruptedException
   */
  @Test
  public void lbclassic77Test() throws InterruptedException {
    LogbackMDCAdapter lma = new LogbackMDCAdapter();

    HashMap<String, String> parentHM = getHashMapFromMDCAdapter(lma);
    assertNull(parentHM);

    ChildThreadForMDCAdapter childThread = new ChildThreadForMDCAdapter(lma);
    childThread.start();
    childThread.join();
    assertTrue(childThread.successul);
    assertNull(childThread.childHM);
  }

  @Test
  public void removeForNullKeyTest() {
    LogbackMDCAdapter lma = new LogbackMDCAdapter();
    lma.remove(null);
  }

  @Test
  public void removeInexistentKey() {
    LogbackMDCAdapter lma = new LogbackMDCAdapter();
    lma.remove("abcdlw0");
  }


  @Test
  public void sequenceWithGet() {
    LogbackMDCAdapter lma = new LogbackMDCAdapter();
    lma.put("k0", "v0");
    Map<String, String> map0 = lma.copyOnInheritThreadLocal.get();
    lma.get("k0");  // point 0
    lma.put("k0", "v1");
    // verify that map0 is that in point 0
    assertEquals("v0", map0.get("k0"));
  }

  @Test
  public void sequenceWithGetPropertyMap() {
    LogbackMDCAdapter lma = new LogbackMDCAdapter();
    lma.put("k0", "v0");
    Map<String, String> map0 = lma.getPropertyMap();  // point 0
    lma.put("k0", "v1");
    // verify that map0 is that in point 0
    assertEquals("v0", map0.get("k0"));
  }


  class ChildThreadForMDCAdapter extends Thread {

    LogbackMDCAdapter logbackMDCAdapter;
    boolean successul;
    HashMap<String, String> childHM;

    ChildThreadForMDCAdapter(LogbackMDCAdapter logbackMDCAdapter) {
      this.logbackMDCAdapter = logbackMDCAdapter;
    }

    @Override
    public void run() {
      childHM = getHashMapFromMDCAdapter(logbackMDCAdapter);
      logbackMDCAdapter.get("");
      successul = true;
    }
  }

  // ================================================= 

  /**
   * Test that LogbackMDCAdapter copies its hashmap when a child
   * thread inherits it.
   *
   * @throws InterruptedException
   */
  @Test
  public void copyOnInheritenceTest() throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    String firstKey = "x" + diff;
    String secondKey = "o" + diff;
    MDC.put(firstKey, firstKey + A_SUFFIX);

    ChildThreadForMDC childThread = new ChildThreadForMDC(firstKey, secondKey, countDownLatch);
    childThread.start();
    countDownLatch.await();
    MDC.put(firstKey, firstKey + B_SUFFIX);
    childThread.join();

    assertNull(MDC.get(secondKey));
    assertTrue(childThread.successul);

    HashMap<String, String> parentHM = getHashMapFromMDC();
    assertTrue(parentHM != childThread.childHM);

    HashMap<String, String> parentHMWitness = new HashMap<String, String>();
    parentHMWitness.put(firstKey, firstKey + B_SUFFIX);
    assertEquals(parentHMWitness, parentHM);

    HashMap<String, String> childHMWitness = new HashMap<String, String>();
    childHMWitness.put(firstKey, firstKey + A_SUFFIX);
    childHMWitness.put(secondKey, secondKey + A_SUFFIX);
    assertEquals(childHMWitness, childThread.childHM);

  }


  class ChildThreadForMDC extends Thread {

    String firstKey;
    String secondKey;
    boolean successul;
    HashMap<String, String> childHM;
    CountDownLatch countDownLatch;

    ChildThreadForMDC(String firstKey, String secondKey, CountDownLatch countDownLatch) {
      this.firstKey = firstKey;
      this.secondKey = secondKey;
      this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
      MDC.put(secondKey, secondKey + A_SUFFIX);
      assertNotNull(MDC.get(firstKey));
      assertEquals(firstKey + A_SUFFIX, MDC.get(firstKey));
      countDownLatch.countDown();
      assertEquals(secondKey + A_SUFFIX, MDC.get(secondKey));
      successul = true;
      childHM = getHashMapFromMDC();
    }
  }

  HashMap<String, String> getHashMapFromMDCAdapter(LogbackMDCAdapter lma) {
    InheritableThreadLocal<HashMap<String, String>> copyOnInheritThreadLocal = lma.copyOnInheritThreadLocal;
    return copyOnInheritThreadLocal.get();
  }

  HashMap<String, String> getHashMapFromMDC() {
    LogbackMDCAdapter lma = (LogbackMDCAdapter) MDC.getMDCAdapter();
    InheritableThreadLocal<HashMap<String, String>> copyOnInheritThreadLocal = lma.copyOnInheritThreadLocal;
    return copyOnInheritThreadLocal.get();
  }
}
