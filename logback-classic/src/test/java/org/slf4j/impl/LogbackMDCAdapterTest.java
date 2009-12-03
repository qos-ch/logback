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
package org.slf4j.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Random;

import org.junit.Test;
import org.slf4j.MDC;

public class LogbackMDCAdapterTest {

  final static String A_SUFFIX = "A_SUFFIX";

  int diff = new Random().nextInt();

  /**
   * Test that CopyOnInheritThreadLocal does not barf when the 
   * MDC hashmap is null
   * 
   * @throws InterruptedException
   */
  @Test
  public void lbclassic77() throws InterruptedException {
    LogbackMDCAdapter lma = new LogbackMDCAdapter();

    HashMap<String, String> parentHM = getHashMapFromMDCAdapter(lma);
    assertNull(parentHM);
    
    ChildThreadForMDCAdapter childThread = new ChildThreadForMDCAdapter(lma);
    childThread.start();
    childThread.join();
    assertTrue(childThread.successul);
    assertNull(childThread.childHM);
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
  public void copyOnInheritence() throws InterruptedException {
    String mdcKey = "x" + diff;
    String otherMDCKey = "o" + diff;
    MDC.put(mdcKey, mdcKey + A_SUFFIX);

    HashMap<String, String> parentHM = getHashMapFromMDC();

    ChildThreadForMDC childThread = new ChildThreadForMDC(mdcKey, otherMDCKey);
    childThread.start();
    childThread.join();

    assertNull(MDC.get(otherMDCKey));
    assertTrue(childThread.successul);
    assertTrue(parentHM != childThread.childHM);
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
      childHM = getHashMapFromMDC();

      MDC.put(otherMDCKey, otherMDCKey + A_SUFFIX);
      assertNotNull(MDC.get(mdcKey));
      assertEquals(mdcKey + A_SUFFIX, MDC.get(mdcKey));
      assertEquals(otherMDCKey + A_SUFFIX, MDC.get(otherMDCKey));
      successul = true;
    }
  }

  HashMap<String, String> getHashMapFromMDCAdapter(LogbackMDCAdapter lma) {
    CopyOnInheritThreadLocal copyOnInheritThreadLocal = lma.copyOnInheritThreadLocal;
    return copyOnInheritThreadLocal.get();
  }

  HashMap<String, String> getHashMapFromMDC() {
    LogbackMDCAdapter lma = (LogbackMDCAdapter) MDC.getMDCAdapter();
    CopyOnInheritThreadLocal copyOnInheritThreadLocal = lma.copyOnInheritThreadLocal;
    return copyOnInheritThreadLocal.get();
  }
}
