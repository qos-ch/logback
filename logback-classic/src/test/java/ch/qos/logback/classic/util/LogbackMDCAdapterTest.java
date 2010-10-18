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
  public void removeInexistnetKey() {
    LogbackMDCAdapter lma = new LogbackMDCAdapter();
    lma.remove("abcdlw0");
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
    String mdcKey = "x" + diff;
    String otherMDCKey = "o" + diff;
    MDC.put(mdcKey, mdcKey + A_SUFFIX);


    ChildThreadForMDC childThread = new ChildThreadForMDC(mdcKey, otherMDCKey);
    childThread.start();
    MDC.put(mdcKey, mdcKey + B_SUFFIX);
    childThread.join();

    assertNull(MDC.get(otherMDCKey));
    assertTrue(childThread.successul);

    HashMap<String, String> parentHM = getHashMapFromMDC();
    assertTrue(parentHM != childThread.childHM);

    HashMap<String, String> parentHMWitness = new  HashMap<String, String>();
    parentHMWitness.put(mdcKey, mdcKey + B_SUFFIX);
    assertEquals(parentHMWitness, parentHM);

    HashMap<String, String> childHMWitness = new  HashMap<String, String>();
    childHMWitness.put(mdcKey, mdcKey + A_SUFFIX);
    childHMWitness.put(otherMDCKey, otherMDCKey + A_SUFFIX);
    assertEquals(childHMWitness, childThread.childHM);

  }


  class ChildThreadForMDC extends Thread {

    String mdcKey;
    String otherMDCKey;
    boolean successul;
    HashMap<String, String> childHM;

    ChildThreadForMDC(String mdcKey, String otherMDCKey) {
      this.mdcKey = mdcKey;
      this.otherMDCKey = otherMDCKey;
    }

    @Override
    public void run() {
      MDC.put(otherMDCKey, otherMDCKey + A_SUFFIX);
      assertNotNull(MDC.get(mdcKey));
      assertEquals(mdcKey + A_SUFFIX, MDC.get(mdcKey));
      assertEquals(otherMDCKey + A_SUFFIX, MDC.get(otherMDCKey));
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
