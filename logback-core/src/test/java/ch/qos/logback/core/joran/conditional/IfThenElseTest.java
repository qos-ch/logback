/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
 * 
 * This program and the accompanying materials are dual-licensed under either
 * the terms of the Eclipse Public License v1.0 as published by the Eclipse
 * Foundation
 * 
 * or (per the licensee's choosing)
 * 
 * under the terms of the GNU Lesser General Public License version 2.1 as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.joran.conditional;


import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.TrivialConfigurator;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.action.ext.IncAction;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.StatusPrinter;

public class IfThenElseTest {

  Context context = new ContextBase();
  TrivialConfigurator tc;
  int diff = RandomUtil.getPositiveInt();
  static final String CONDITIONAL_DIR_PREFIX = CoreTestConstants.JORAN_INPUT_PREFIX
  + "conditional/";

  
  @Before
  public void setUp() throws Exception {
    HashMap<Pattern, Action> rulesMap = new HashMap<Pattern, Action>();
    rulesMap.put(new Pattern("x"), new NOPAction());
    rulesMap.put(new Pattern("x/inc"), new IncAction());
    rulesMap.put(new Pattern("*/if"), new IfAction());
    rulesMap.put(new Pattern("*/if/then"), new ThenAction());
    rulesMap.put(new Pattern("*/if/then/*"), new NOPAction());
    rulesMap.put(new Pattern("*/if/else"), new ElseAction());
    rulesMap.put(new Pattern("*/if/else/*"), new NOPAction());    
    
    tc = new TrivialConfigurator(rulesMap);
    tc.setContext(context);
  }

  @After
  public void tearDown() throws Exception {
    IncAction.reset();
  }

  @Test
  public void if0_Then() throws JoranException {
    context.putProperty("Ki1", "Val1");
    tc.doConfigure(CONDITIONAL_DIR_PREFIX+"if0.xml");
    verifyConfig(1);
  }

  @Test
  public void if0_Else() throws JoranException {
    tc.doConfigure(CONDITIONAL_DIR_PREFIX+"if0.xml");
    StatusPrinter.print(context);
    verifyConfig(2);
  }

  
  void verifyConfig(int expected) {
    assertEquals(expected, IncAction.beginCount);
    assertEquals(expected, IncAction.endCount);
  }
  
  
  
}
