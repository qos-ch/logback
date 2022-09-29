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

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;

import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test SimpleRuleStore for various explicit rule combinations.
 * 
 * We also test that explicit patterns are case-sensitive.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class SimpleRuleStoreTest {

    SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
    CaseCombinator cc = new CaseCombinator();

    @Test
    public void smoke() throws Exception {
        srs.addRule(new ElementSelector("a/b"), () -> new XAction());

        // test for all possible case combinations of "a/b"
        for (String s : cc.combinations("a/b")) {
            System.out.println("s=" + s);
            Supplier<Action> r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);

            if (!(r.get() instanceof XAction)) {
                fail("Wrong type");
            }
        }
    }

    @Test
    public void smokeII() throws Exception {
        srs.addRule(new ElementSelector("a/b"), () -> new XAction());

        Exception e = assertThrows(IllegalStateException.class, () -> {
            srs.addRule(new ElementSelector("a/b"), () -> new YAction());
        });
        assertEquals("[a][b] already has an associated action supplier", e.getMessage());
    }

    @Test
    public void testSlashSuffix() throws Exception {
        ElementSelector pa = new ElementSelector("a/");
        srs.addRule(pa, () -> new XAction());

        for (String s : cc.combinations("a")) {
            Supplier<Action> r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);

            if (!(r.get() instanceof XAction)) {
                fail("Wrong type");
            }
        }

    }

    @Test
    public void testTail1() throws Exception {
        srs.addRule(new ElementSelector("*/b"), () -> new XAction());

        for (String s : cc.combinations("a/b")) {
            Supplier<Action> r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);

            if (!(r.get() instanceof XAction)) {
                fail("Wrong type");
            }
        }
    }

    @Test
    public void testTail2() throws Exception {
        SimpleRuleStore srs = new SimpleRuleStore(new ContextBase());
        srs.addRule(new ElementSelector("*/c"), () -> new XAction());

        for (String s : cc.combinations("a/b/c")) {
            Supplier<Action> r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);
            if (!(r.get() instanceof XAction)) {
                fail("Wrong type");
            }
        }
    }

    @Test
    public void testTail3() throws Exception {
        srs.addRule(new ElementSelector("*/b"), () -> new XAction());
        srs.addRule(new ElementSelector("*/a/b"), () -> new YAction());

        for (String s : cc.combinations("a/b")) {
            Supplier<Action> r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);
            Action ya = r.get();
            if (!(ya instanceof YAction)) {
                fail("Wrong type");
            }
        }
    }

    @Test
    public void testTail4() throws Exception {
        srs.addRule(new ElementSelector("*/b"), () -> new XAction());
        srs.addRule(new ElementSelector("*/a/b"), () -> new YAction());
        srs.addRule(new ElementSelector("a/b"), () -> new ZAction());

        for (String s : cc.combinations("a/b")) {
            Supplier<Action> r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);

            if (!(r.get() instanceof ZAction)) {
                fail("Wrong type");
            }
        }
    }

    @Test
    public void testSuffix() throws Exception {
        srs.addRule(new ElementSelector("a"), () -> new XAction());
        srs.addRule(new ElementSelector("a/*"), () -> new YAction());

        for (String s : cc.combinations("a/b")) {
            Supplier<Action> r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);
            assertTrue(r.get() instanceof YAction);
        }
    }

    @Test
    public void testDeepSuffix() throws Exception {
        srs.addRule(new ElementSelector("a"), () -> new XAction(1));
        srs.addRule(new ElementSelector("a/b/*"), () -> new XAction(2));

        for (String s : cc.combinations("a/other")) {
            Supplier<Action> r = srs.matchActions(new ElementPath(s));
            assertNull(r);
        }
    }

    @Test
    public void testPrefixSuffixInteraction1() throws Exception {
        srs.addRule(new ElementSelector("a"), () -> new ZAction());
        srs.addRule(new ElementSelector("a/*"), () -> new YAction());
        srs.addRule(new ElementSelector("*/a/b"), () -> new XAction(3));

        for (String s : cc.combinations("a/b")) {
            Supplier<Action> r = srs.matchActions(new ElementPath(s));
            assertNotNull(r);

            Action ra = r.get();
            
            assertTrue(ra instanceof XAction);
            XAction xaction = (XAction) ra;
            assertEquals(3, xaction.id);
        }
    }

    @Test
    public void testPrefixSuffixInteraction2() throws Exception {
        srs.addRule(new ElementSelector("tG"), () -> new XAction());
        srs.addRule(new ElementSelector("tG/tS"), () -> new YAction());
        srs.addRule(new ElementSelector("tG/tS/test"), () -> new ZAction());
        srs.addRule(new ElementSelector("tG/tS/test/*"), () -> new XAction(9));

        for (String s : cc.combinations("tG/tS/toto")) {
            Supplier<Action> r = srs.matchActions(new ElementPath(s));
            assertNull(r);
        }
    }

    @Test
    public void withTransparentParts() throws Exception {
//        srs.addRule(new ElementSelector("x"), new TopElementAction());
//        srs.addRule(new ElementSelector("x/stack"), new StackAction());
//        srs.addRule(new ElementSelector("x/property"), new PropertyAction());
//        srs.addRule(new ElementSelector("*/if"), new IfAction());
//        srs.addRule(new ElementSelector("*/if/then"), new ThenAction());
//        srs.addRule(new ElementSelector("*/if/else"), new ElseAction());

        srs.addTransparentPathPart("if");
        srs.addTransparentPathPart("then");
        srs.addTransparentPathPart("else");

        {
            ElementPath ep = new ElementPath("x/if/then/if");
            ElementPath witness = new ElementPath("x/");
            
            ElementPath cleanedEP = srs.removeTransparentPathParts(ep);
            assertEquals(witness, cleanedEP);
        }
        
        {
            ElementPath ep = new ElementPath("x/if/then/stack");
            ElementPath witness = new ElementPath("x/stack");
            
            ElementPath cleanedEP = srs.removeTransparentPathParts(ep);
            assertEquals(witness, cleanedEP);
        }
        
        {
            ElementPath ep = new ElementPath("x/if/then/if/else/stack");
            ElementPath witness = new ElementPath("x/stack");
            
            ElementPath cleanedEP = srs.removeTransparentPathParts(ep);
            assertEquals(witness, cleanedEP);
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
