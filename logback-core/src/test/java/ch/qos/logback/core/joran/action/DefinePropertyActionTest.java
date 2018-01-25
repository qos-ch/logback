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
package ch.qos.logback.core.joran.action;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.SimpleConfigurator;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.StatusChecker;

/**
 * Test {@link DefinePropertyAction}.
 * 
 * @author Aleksey Didik
 */
public class DefinePropertyActionTest {

    private static final String DEFINE_INPUT_DIR = CoreTestConstants.JORAN_INPUT_PREFIX + "define/";
    private static final String GOOD_XML = "good.xml";
    private static final String NONAME_XML = "noname.xml";
    private static final String NOCLASS_XML = "noclass.xml";
    private static final String BADCLASS_XML = "badclass.xml";

    SimpleConfigurator simpleConfigurator;
    Context context = new ContextBase();
    DefinePropertyAction definerAction;
    InterpretationContext ic;
    StatusChecker checker = new StatusChecker(context);

    @Before
    public void setUp() throws Exception {

        HashMap<ElementSelector, Action> rulesMap = new HashMap<ElementSelector, Action>();
        rulesMap.put(new ElementSelector("define"), new DefinePropertyAction());
        simpleConfigurator = new SimpleConfigurator(rulesMap);
        simpleConfigurator.setContext(context);
    }

    @After
    public void tearDown() throws Exception {
        // StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    @Test
    public void good() throws JoranException {
        simpleConfigurator.doConfigure(DEFINE_INPUT_DIR + GOOD_XML);
        InterpretationContext ic = simpleConfigurator.getInterpreter().getInterpretationContext();
        String inContextFoo = ic.getProperty("foo");
        assertEquals("monster", inContextFoo);
    }

    @Test
    public void noName() throws JoranException {
        simpleConfigurator.doConfigure(DEFINE_INPUT_DIR + NONAME_XML);
        // get from context
        String inContextFoo = context.getProperty("foo");
        assertNull(inContextFoo);
        // check context errors
        checker.assertContainsMatch(Status.ERROR, "Missing property name for property definer. Near \\[define\\] line 1");
    }

    @Test
    public void noClass() throws JoranException {
        simpleConfigurator.doConfigure(DEFINE_INPUT_DIR + NOCLASS_XML);
        String inContextFoo = context.getProperty("foo");
        assertNull(inContextFoo);
        checker.assertContainsMatch(Status.ERROR, "Missing class name for property definer. Near \\[define\\] line 1");
    }

    @Test
    public void testBadClass() throws JoranException {
        simpleConfigurator.doConfigure(DEFINE_INPUT_DIR + BADCLASS_XML);
        // get from context
        String inContextFoo = context.getProperty("foo");
        assertNull(inContextFoo);
        // check context errors
        checker.assertContainsMatch(Status.ERROR, "Could not create an PropertyDefiner of type");
    }

    @Ignore // on certain hosts this test takes 5 seconds to complete
    @Test
    public void canonicalHostNameProperty() throws JoranException {
        String configFileAsStr = DEFINE_INPUT_DIR + "canonicalHostname.xml";
        simpleConfigurator.doConfigure(configFileAsStr);
        assertNotNull(context.getProperty("CANONICAL_HOST_NAME"));
    }

}
