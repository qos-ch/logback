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

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;

import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ExecutionContext;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.SimpleRuleStore;


import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
      fail("Wring type");
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

    // jp.parse(doc);
  }

  public void test2() throws Exception {
    SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
    srs.addRule(new Pattern("*/b"), new XAction());

    List r = srs.matchActions(new Pattern("a/b"));
    assertNotNull(r);

    // System.out.println(r);
    assertEquals(1, r.size());

    if (!(r.get(0) instanceof XAction)) {
      fail("Wring type");
    }
  }

  public void test3() throws Exception {
    SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
    srs.addRule(new Pattern("*/b"), new XAction());
    srs.addRule(new Pattern("*/a/b"), new YAction());

    List r = srs.matchActions(new Pattern("a/b"));
    assertNotNull(r);

    // System.out.println("restulg list is: "+r);
    assertEquals(1, r.size());

    if (!(r.get(0) instanceof YAction)) {
      fail("Wring type");
    }
  }

  public void test4() throws Exception {
    SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
    srs.addRule(new Pattern("*/b"), new XAction());
    srs.addRule(new Pattern("*/a/b"), new YAction());
    srs.addRule(new Pattern("a/b"), new ZAction());

    List r = srs.matchActions(new Pattern("a/b"));
    assertNotNull(r);

    // System.out.println("result list is: "+r);
    assertEquals(1, r.size());

    if (!(r.get(0) instanceof ZAction)) {
      fail("Wring type");
    }
  }

  Document getW3Document(String file) throws Exception {
    DocumentBuilderFactory dbf = null;
    dbf = DocumentBuilderFactory.newInstance();

    DocumentBuilder docBuilder = dbf.newDocumentBuilder();

    // inputSource.setSystemId("dummy://log4j.dtd");
    return docBuilder.parse(file);
  }

  class XAction extends Action {
    public void begin(ExecutionContext ec, String name, Attributes attributes) {
    }

    public void end(ExecutionContext ec, String name) {
    }

    public void finish(ExecutionContext ec) {
    }
  }

  class YAction extends Action {
    public void begin(ExecutionContext ec, String name, Attributes attributes) {
    }

    public void end(ExecutionContext ec, String name) {
    }

    public void finish(ExecutionContext ec) {
    }
  }

  class ZAction extends Action {
    public void begin(ExecutionContext ec, String name, Attributes attributes) {
    }

    public void end(ExecutionContext ec, String name) {
    }

    public void finish(ExecutionContext ec) {
    }
  }
}
