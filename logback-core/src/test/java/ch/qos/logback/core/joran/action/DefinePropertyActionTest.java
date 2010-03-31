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
package ch.qos.logback.core.joran.action;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.SimpleConfigurator;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.util.CoreTestConstants;

/**
 * Test {@link DefinePropertyAction}.
 * 
 * @author Aleksey Didik
 */
public class DefinePropertyActionTest {

  private static final String DEFINE_XML_DIR = CoreTestConstants.JORAN_INPUT_PREFIX
      + "define/";
  private static final String GOOD_XML = "good.xml";
  private static final String NONAME_XML = "noname.xml";
  private static final String NOCLASS_XML = "noclass.xml";
  private static final String BADCLASS_XML = "badclass.xml";

  SimpleConfigurator sc;
  Context context;
  DefinePropertyAction definerAction;
  InterpretationContext ic;

  @Before
  public void setUp() throws Exception {
    context = new ContextBase();
    HashMap<Pattern, Action> rulesMap = new HashMap<Pattern, Action>();
    rulesMap.put(new Pattern("define"), new DefinePropertyAction());
    sc = new SimpleConfigurator(rulesMap);
    sc.setContext(context);

  }

  @Test
  public void testAllRight() throws JoranException {
    sc.doConfigure(DEFINE_XML_DIR + GOOD_XML);
    // get from context
    String inContextFoo = context.getProperty("foo");
    // get from real class
    FooPropertyDefiner fooDefiner = new FooPropertyDefiner();
    fooDefiner.setFooName("Monster");
    String fromRealClassFoo = fooDefiner.getPropertyValue();
    assertEquals(inContextFoo, fromRealClassFoo);
  }

  @Test
  public void testNoName() throws JoranException {
    sc.doConfigure(DEFINE_XML_DIR + NONAME_XML);
    // get from context
    String inContextFoo = context.getProperty("foo");
    assertNull(inContextFoo);
    // check context errors
    assertTrue(checkError("Missing property name for property definer. Near [define] line 1"));
  }

  @Test
  public void testNoClass() throws JoranException {
    sc.doConfigure(DEFINE_XML_DIR + NOCLASS_XML);
    // get from context
    String inContextFoo = context.getProperty("foo");
    assertNull(inContextFoo);
    // check context errors
    assertTrue(checkError("Missing class name for property definer. Near [define] line 1"));
  }

  @Test
  public void testBadClass() throws JoranException {
    sc.doConfigure(DEFINE_XML_DIR + BADCLASS_XML);
    // get from context
    String inContextFoo = context.getProperty("foo");
    assertNull(inContextFoo);
    // check context errors
    assertTrue(checkBadClassError());
  }

  @After
  public void tearDown() throws Exception {
    context = null;
    sc = null;
  }

  private boolean checkError(String waitedMsg) {
    Iterator it = context.getStatusManager().getCopyOfStatusList().iterator();
    ErrorStatus es1 = (ErrorStatus) it.next();
    return waitedMsg.equals(es1.getMessage());
  }

  private boolean checkBadClassError() {
    Iterator it = context.getStatusManager().getCopyOfStatusList().iterator();
    // skip info about class instantiating
    it.next();
    // check error status
    ErrorStatus es1 = (ErrorStatus) it.next();
    return "Could not create an PropertyDefiner of type [a.b.c.Foo]."
        .equals(es1.getMessage());
  }

}
