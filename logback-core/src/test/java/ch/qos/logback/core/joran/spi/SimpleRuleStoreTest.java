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
package ch.qos.logback.core.joran.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.xml.sax.Attributes;

import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;

/**
 * Test SimpleRuleStore for various explicit rule combinations.
 * 
 * We also test that explicit patterns are case sensitive.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class SimpleRuleStoreTest {

  SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
  CaseCombinator cc = new CaseCombinator();
  
  @Test
  public void smoke() throws Exception {
    srs.addRule(new Pattern("a/b"), new XAction());

    // test for all possible case combinations of "a/b"
    for (String s : cc.combinations("a/b")) {
       List r = srs.matchActions(new Pattern(s));
      assertNotNull(r);
      assertEquals(1, r.size());

      if (!(r.get(0) instanceof XAction)) {
        fail("Wrong type");
      }
    }
  }

  @Test
  public void smokeII() throws Exception {
    srs.addRule(new Pattern("a/b"), new XAction());
    srs.addRule(new Pattern("a/b"), new YAction());

    for (String s : cc.combinations("a/b")) {
      List r = srs.matchActions(new Pattern(s));
      assertNotNull(r);
      assertEquals(2, r.size());

      if (!(r.get(0) instanceof XAction)) {
        fail("Wrong type");
      }

      if (!(r.get(1) instanceof YAction)) {
        fail("Wrong type");
      }
    }
  }

  @Test
  public void testSlashSuffix() throws Exception {
    Pattern pa = new Pattern("a/");
    srs.addRule(pa, new XAction());

    for (String s : cc.combinations("a")) {
      List r = srs.matchActions(new Pattern(s));
      assertNotNull(r);
      assertEquals(1, r.size());

      if (!(r.get(0) instanceof XAction)) {
        fail("Wrong type");
      }
    }

  }

  @Test
  public void testTail1() throws Exception {
    srs.addRule(new Pattern("*/b"), new XAction());

    for (String s : cc.combinations("a/b")) {
      List r = srs.matchActions(new Pattern(s));
      assertNotNull(r);

      assertEquals(1, r.size());

      if (!(r.get(0) instanceof XAction)) {
        fail("Wrong type");
      }
    }
  }

  @Test
  public void testTail2() throws Exception {
    SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
    srs.addRule(new Pattern("*/c"), new XAction());

    for (String s : cc.combinations("a/b/c")) {
      List r = srs.matchActions(new Pattern(s));
      assertNotNull(r);

      assertEquals(1, r.size());

      if (!(r.get(0) instanceof XAction)) {
        fail("Wrong type");
      }
    }
  }

  @Test
  public void testTail3() throws Exception {
    srs.addRule(new Pattern("*/b"), new XAction());
    srs.addRule(new Pattern("*/a/b"), new YAction());

    for (String s : cc.combinations("a/b")) {
      List r = srs.matchActions(new Pattern(s));
      assertNotNull(r);
      assertEquals(1, r.size());

      if (!(r.get(0) instanceof YAction)) {
        fail("Wrong type");
      }
    }
  }

  @Test
  public void testTail4() throws Exception {
    srs.addRule(new Pattern("*/b"), new XAction());
    srs.addRule(new Pattern("*/a/b"), new YAction());
    srs.addRule(new Pattern("a/b"), new ZAction());

    for (String s : cc.combinations("a/b")) {
      List r = srs.matchActions(new Pattern(s));
      assertNotNull(r);
      assertEquals(1, r.size());

      if (!(r.get(0) instanceof ZAction)) {
        fail("Wrong type");
      }
    }
  }

  @Test
  public void testSuffix() throws Exception {
    srs.addRule(new Pattern("a"), new XAction());
    srs.addRule(new Pattern("a/*"), new YAction());

    for (String s : cc.combinations("a/b")) {
      List r = srs.matchActions(new Pattern(s));
      assertNotNull(r);
      assertEquals(1, r.size());
      assertTrue(r.get(0) instanceof YAction);
    }
  }

  @Test
  public void testDeepSuffix() throws Exception {
    srs.addRule(new Pattern("a"), new XAction(1));
    srs.addRule(new Pattern("a/b/*"), new XAction(2));

    for (String s : cc.combinations("a/other")) {
      List r = srs.matchActions(new Pattern(s));
      assertNull(r);
    }
  }

  @Test
  public void testPrefixSuffixInteraction1() throws Exception {
    srs.addRule(new Pattern("a"), new ZAction());
    srs.addRule(new Pattern("a/*"), new YAction());
    srs.addRule(new Pattern("*/a/b"), new XAction(3));

    for (String s : cc.combinations("a/b")) {
      List r = srs.matchActions(new Pattern(s));
      assertNotNull(r);

      assertEquals(1, r.size());

      assertTrue(r.get(0) instanceof XAction);
      XAction xaction = (XAction) r.get(0);
      assertEquals(3, xaction.id);
    }
  }

  @Test
  public void testPrefixSuffixInteraction2() throws Exception {
    srs.addRule(new Pattern("tG"), new XAction());
    srs.addRule(new Pattern("tG/tS"), new YAction());
    srs.addRule(new Pattern("tG/tS/test"), new ZAction());
    srs.addRule(new Pattern("tG/tS/test/*"), new XAction(9));

    for (String s : cc.combinations("tG/tS/toto")) {
      List r = srs.matchActions(new Pattern(s));
      assertNull(r);
    }
  }

  class XAction extends Action {
    int id = 0;

    XAction() {
    }

    XAction(int id) {
      this.id = id;
    }

    public void begin(InterpretationContext ec, String name,
        Attributes attributes) {
    }

    public void end(InterpretationContext ec, String name) {
    }

    public void finish(InterpretationContext ec) {
    }

    public String toString() {
      return "XAction(" + id + ")";
    }
  }

  class YAction extends Action {
    public void begin(InterpretationContext ec, String name,
        Attributes attributes) {
    }

    public void end(InterpretationContext ec, String name) {
    }

    public void finish(InterpretationContext ec) {
    }
  }

  class ZAction extends Action {
    public void begin(InterpretationContext ec, String name,
        Attributes attributes) {
    }

    public void end(InterpretationContext ec, String name) {
    }

    public void finish(InterpretationContext ec) {
    }
  }

}
