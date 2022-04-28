/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        srs.addRule(new ElementSelector("a/b"), new XAction());

        // test for all possible case combinations of "a/b"
        for (String s : cc.combinations("a/b")) {
            System.out.println("s=" + s);
            Action r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);

            if (!(r instanceof XAction)) {
                fail("Wrong type");
            }
        }
    }

    @Test
    public void smokeII() throws Exception {
        srs.addRule(new ElementSelector("a/b"), new XAction());
        
        Exception e = assertThrows(IllegalStateException.class, () -> {
           srs.addRule(new ElementSelector("a/b"), new YAction());
        });
        assertEquals("[a][b] already has an associated action", e.getMessage());
    }
    @Test
    public void testSlashSuffix() throws Exception {
        ElementSelector pa = new ElementSelector("a/");
        srs.addRule(pa, new XAction());

        for (String s : cc.combinations("a")) {
            Action r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);
            
            if (!(r instanceof XAction)) {
                fail("Wrong type");
            }
        }

    }

    @Test
    public void testTail1() throws Exception {
        srs.addRule(new ElementSelector("*/b"), new XAction());

        for (String s : cc.combinations("a/b")) {
            Action r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);

            if (!(r instanceof XAction)) {
                fail("Wrong type");
            }
        }
    }

    @Test
    public void testTail2() throws Exception {
        SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
        srs.addRule(new ElementSelector("*/c"), new XAction());

        for (String s : cc.combinations("a/b/c")) {
            Action r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);
            if (!(r instanceof XAction)) {
                fail("Wrong type");
            }
        }
    }

    @Test
    public void testTail3() throws Exception {
        srs.addRule(new ElementSelector("*/b"), new XAction());
        srs.addRule(new ElementSelector("*/a/b"), new YAction());

        for (String s : cc.combinations("a/b")) {
            Action r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);

            if (!(r instanceof YAction)) {
                fail("Wrong type");
            }
        }
    }

    @Test
    public void testTail4() throws Exception {
        srs.addRule(new ElementSelector("*/b"), new XAction());
        srs.addRule(new ElementSelector("*/a/b"), new YAction());
        srs.addRule(new ElementSelector("a/b"), new ZAction());

        for (String s : cc.combinations("a/b")) {
            Action r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);

            if (!(r instanceof ZAction)) {
                fail("Wrong type");
            }
        }
    }

    @Test
    public void testSuffix() throws Exception {
        srs.addRule(new ElementSelector("a"), new XAction());
        srs.addRule(new ElementSelector("a/*"), new YAction());

        for (String s : cc.combinations("a/b")) {
            Action r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);
            assertTrue(r instanceof YAction);
        }
    }

    @Test
    public void testDeepSuffix() throws Exception {
        srs.addRule(new ElementSelector("a"), new XAction(1));
        srs.addRule(new ElementSelector("a/b/*"), new XAction(2));

        for (String s : cc.combinations("a/other")) {
            Action r = srs.matchActions(new ElementPath(s));
            assertNull(r);
        }
    }

    @Test
    public void testPrefixSuffixInteraction1() throws Exception {
        srs.addRule(new ElementSelector("a"), new ZAction());
        srs.addRule(new ElementSelector("a/*"), new YAction());
        srs.addRule(new ElementSelector("*/a/b"), new XAction(3));

        for (String s : cc.combinations("a/b")) {
            Action r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);


            assertTrue(r instanceof XAction);
            XAction xaction = (XAction) r;
            assertEquals(3, xaction.id);
        }
    }

    @Test
    public void testPrefixSuffixInteraction2() throws Exception {
        srs.addRule(new ElementSelector("tG"), new XAction());
        srs.addRule(new ElementSelector("tG/tS"), new YAction());
        srs.addRule(new ElementSelector("tG/tS/test"), new ZAction());
        srs.addRule(new ElementSelector("tG/tS/test/*"), new XAction(9));

        for (String s : cc.combinations("tG/tS/toto")) {
            Action r = srs.matchActions(new ElementPath(s));
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

        public void begin(SaxEventInterpretationContext ec, String name, Attributes attributes) {
        }

        public void end(SaxEventInterpretationContext ec, String name) {
        }

        public void finish(SaxEventInterpretationContext ec) {
        }

        public String toString() {
            return "XAction(" + id + ")";
        }
    }

    class YAction extends Action {
        public void begin(SaxEventInterpretationContext ec, String name, Attributes attributes) {
        }

        public void end(SaxEventInterpretationContext ec, String name) {
        }

        public void finish(SaxEventInterpretationContext ec) {
        }
    }

    class ZAction extends Action {
        public void begin(SaxEventInterpretationContext ec, String name, Attributes attributes) {
        }

        public void end(SaxEventInterpretationContext ec, String name) {
        }

        public void finish(SaxEventInterpretationContext ec) {
        }
    }

}
