/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.joran;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ext.IncAction;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.TrivialStatusListener;
import ch.qos.logback.core.util.Constants;
import ch.qos.logback.core.util.StatusPrinter;

public class TrivialcConfiguratorTest {

  Context context = new ContextBase();

  public void doTest(String filename) throws Exception {

    HashMap<Pattern, Action> rulesMap = new HashMap<Pattern, Action>();
    rulesMap.put(new Pattern("x/inc"), new IncAction());

    TrivialConfigurator gc = new TrivialConfigurator(rulesMap);

    gc.setContext(context);
    gc.doConfigure(Constants.TEST_DIR_PREFIX + "input/joran/" + filename);
  }

  @Test
  public void smokeTezt() throws Exception {
    int oldBeginCount = IncAction.beginCount;
    int oldEndCount = IncAction.endCount;
    int oldErrorCount = IncAction.errorCount;
    doTest("inc.xml");
    assertEquals(oldErrorCount, IncAction.errorCount);
    assertEquals(oldBeginCount + 1, IncAction.beginCount);
    assertEquals(oldEndCount + 1, IncAction.endCount);
  }

  @Test
  public void teztInexistentFile() {
    TrivialStatusListener tsl = new TrivialStatusListener();
    String filename = "nothereBLAH.xml";
    context.getStatusManager().add(tsl);
    try {
      doTest(filename);
    } catch (Exception e) {
    }
    assertTrue(tsl.list.size() + " should be greater than or equal to 1",
        tsl.list.size() >= 1);
    Status s0 = tsl.list.get(0);
    assertTrue(s0.getMessage().startsWith("Could not open [" + filename + "]"));
  }

  @Test
  public void teztIllFormedXML() {
    TrivialStatusListener tsl = new TrivialStatusListener();
    String filename = "illformed.xml";
    context.getStatusManager().add(tsl);
    try {
      doTest(filename);
    } catch (Exception e) {
    }
    assertEquals(2, tsl.list.size());
    Status s0 = tsl.list.get(0);
    assertTrue(s0.getMessage().startsWith(
        "Parsing fatal error on line 5 and column 3"));
    Status s1 = tsl.list.get(1);
    assertTrue(s1
        .getMessage()
        .startsWith(
            "Problem parsing XML document. See previously reported errors. Abandoning all further processing."));
  }
}
