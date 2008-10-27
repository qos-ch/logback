/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
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

    HashMap<String, String> parentHM = getHM();

    MyThread myThread = new MyThread(mdcKey, otherMDCKey);
    myThread.start();
    myThread.join();

    assertNull(MDC.get(otherMDCKey));
    assertTrue(myThread.successul);
    assertTrue(parentHM != myThread.childHM);
  }

  class MyThread extends Thread {

    String mdcKey;
    String otherMDCKey;
    boolean successul;
    HashMap<String, String> childHM;

    MyThread(String mdcKey, String otherMDCKey) {
      this.mdcKey = mdcKey;
      this.otherMDCKey = otherMDCKey;
    }

    @Override
    public void run() {
      childHM = getHM();

      MDC.put(otherMDCKey, otherMDCKey + A_SUFFIX);
      assertNotNull(MDC.get(mdcKey));
      assertEquals(mdcKey + A_SUFFIX, MDC.get(mdcKey));
      assertEquals(otherMDCKey + A_SUFFIX, MDC.get(otherMDCKey));
      successul = true;
    }
  }

  HashMap<String, String> getHM() {
    LogbackMDCAdapter lma = (LogbackMDCAdapter) MDC.getMDCAdapter();
    CopyOnInheritThreadLocal copyOnInheritThreadLocal = lma.copyOnInheritThreadLocal;
    return copyOnInheritThreadLocal.get();

  }
}
