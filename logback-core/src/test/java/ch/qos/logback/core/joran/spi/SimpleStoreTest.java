/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.joran.spi;

import java.util.List;

import junit.framework.TestCase;

import org.xml.sax.Attributes;

import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;

/**
 * 
 * @author Ceki Gulcu
 */
public class SimpleStoreTest extends TestCase {

  public SimpleStoreTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void test1() throws Exception {
    // Document doc = getW3Document("file:input/joran/parser1.xml");
    SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
    srs.addRule(new Pattern("a/b"), new XAction());

    List r = srs.matchActions(new Pattern("a/b"));
    assertNotNull(r);
    assertEquals(1, r.size());

    if (!(r.get(0) instanceof XAction)) {
      fail("Wrong type");
    }

    srs = new SimpleRuleStore(new ContextBase());
    srs.addRule(new Pattern("a/b"), new XAction());
    srs.addRule(new Pattern("a/b"), new YAction());

    r = srs.matchActions(new Pattern("a/b"));
    assertNotNull(r);
    assertEquals(2, r.size());

    if (!(r.get(0) instanceof XAction)) {
      fail("Wrong type");
    }

    if (!(r.get(1) instanceof YAction)) {
      fail("Wrong type");
    }

  }

  public void testSlashSuffix() throws Exception {
    SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
    Pattern pa = new Pattern("a/");
    srs.addRule(pa, new XAction());
    
    List r = srs.matchActions(new Pattern("a"));
    assertNotNull(r);
    assertEquals(1, r.size());

    if (!(r.get(0) instanceof XAction)) {
      fail("Wrong type");
    }

 
  }
  public void testTail1() throws Exception {
    SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
    srs.addRule(new Pattern("*/b"), new XAction());

    List r = srs.matchActions(new Pattern("a/b"));
    assertNotNull(r);

    assertEquals(1, r.size());

    if (!(r.get(0) instanceof XAction)) {
      fail("Wrong type");
    }
  }

  
  public void testTail2() throws Exception {
    SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
    srs.addRule(new Pattern("*/c"), new XAction());

    List r = srs.matchActions(new Pattern("a/b/c"));
    assertNotNull(r);

    assertEquals(1, r.size());

    if (!(r.get(0) instanceof XAction)) {
      fail("Wrong type");
    }
  }
  
  public void testTail3() throws Exception {
    SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
    srs.addRule(new Pattern("*/b"), new XAction());
    srs.addRule(new Pattern("*/a/b"), new YAction());

    List r = srs.matchActions(new Pattern("a/b"));
    assertNotNull(r);

    // System.out.println("restulg list is: "+r);
    assertEquals(1, r.size());

    if (!(r.get(0) instanceof YAction)) {
      fail("Wrong type");
    }
  }

  public void testTail4() throws Exception {
    SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
    srs.addRule(new Pattern("*/b"), new XAction());
    srs.addRule(new Pattern("*/a/b"), new YAction());
    srs.addRule(new Pattern("a/b"), new ZAction());

    List r = srs.matchActions(new Pattern("a/b"));
    assertNotNull(r);

    // System.out.println("result list is: "+r);
    assertEquals(1, r.size());

    if (!(r.get(0) instanceof ZAction)) {
      fail("Wrong type");
    }
  }
  
  public void testSuffix() throws Exception {
    SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
    srs.addRule(new Pattern("a"), new XAction());
    srs.addRule(new Pattern("a/*"), new YAction());

    List r = srs.matchActions(new Pattern("a/b"));
    assertNotNull(r);
    assertEquals(1, r.size());
    assertTrue(r.get(0) instanceof YAction);
  }
  
  public void testDeepSuffix() throws Exception {
    SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
    srs.addRule(new Pattern("a"), new XAction(1));
    srs.addRule(new Pattern("a/b/*"), new XAction(2));

    List r = srs.matchActions(new Pattern("a/other"));
    assertNull(r);
  }

  public void testPrefixSuffixInteraction1() throws Exception {
    SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
    srs.addRule(new Pattern("a"), new ZAction());
    srs.addRule(new Pattern("a/*"), new YAction());
    srs.addRule(new Pattern("*/a/b"), new XAction(3));

    List r = srs.matchActions(new Pattern("a/b"));
    assertNotNull(r);

    assertEquals(1, r.size());
    
    assertTrue(r.get(0) instanceof XAction);
    XAction xaction = (XAction) r.get(0);
    assertEquals(3, xaction.id);
  }

  public void testPrefixSuffixInteraction2() throws Exception {
    SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
    srs.addRule(new Pattern("testGroup"), new XAction());
    srs.addRule(new Pattern("testGroup/testShell"), new YAction());
    srs.addRule(new Pattern("testGroup/testShell/test"), new ZAction());
    srs.addRule(new Pattern("testGroup/testShell/test/*"), new XAction(9));
    
    List r = srs.matchActions(new Pattern("testGroup/testShell/toto"));
    System.out.println(r);
    assertNull(r);
  }
  
  class XAction extends Action {
    int id = 0;
    XAction() {
    }
    XAction(int id) {
      this.id = id;
    }

    public void begin(InterpretationContext ec, String name, Attributes attributes) {
    }

    public void end(InterpretationContext ec, String name) {
    }

    public void finish(InterpretationContext ec) {
    }
    
    public String toString() {
     return "XAction("+id+")";
    }    
  }

  class YAction extends Action {
    public void begin(InterpretationContext ec, String name, Attributes attributes) {
    }

    public void end(InterpretationContext ec, String name) {
    }

    public void finish(InterpretationContext ec) {
    }
  }

  class ZAction extends Action {
    public void begin(InterpretationContext ec, String name, Attributes attributes) {
    }

    public void end(InterpretationContext ec, String name) {
    }

    public void finish(InterpretationContext ec) {
    }
  }

}
