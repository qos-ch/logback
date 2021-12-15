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
package ch.qos.logback.classic.selector;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.selector.servlet.ContextDetachingSCL;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.classic.util.MockInitialContext;
import ch.qos.logback.classic.util.MockInitialContextFactory;
import org.slf4j.LoggerFactoryFriend;
import org.slf4j.impl.StaticLoggerBinderFriend;

public class ContextDetachingSCLTest {

    static String INITIAL_CONTEXT_KEY = "java.naming.factory.initial";

    ContextDetachingSCL contextDetachingSCL;

    @Before
    public void setUp() throws Exception {

        System.setProperty(ClassicConstants.LOGBACK_CONTEXT_SELECTOR, "JNDI");

        contextDetachingSCL = new ContextDetachingSCL();

        MockInitialContextFactory.initialize();
        MockInitialContext mic = MockInitialContextFactory.getContext();
        mic.map.put(ClassicConstants.JNDI_CONTEXT_NAME, "toto");

        // The property must be set after we setup the Mock
        System.setProperty(INITIAL_CONTEXT_KEY, MockInitialContextFactory.class.getName());

        // reinitialize the LoggerFactory, These reset methods are reserved for internal use
        StaticLoggerBinderFriend.reset();
        LoggerFactoryFriend.reset();

        // this call will create the context "toto"
        LoggerFactory.getLogger(ContextDetachingSCLTest.class);
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(INITIAL_CONTEXT_KEY);
        // reinitialize the LoggerFactory, These resets method are reserved for internal use
        StaticLoggerBinderFriend.reset();
        LoggerFactoryFriend.reset();
    }

    @Test
    public void testDetach() {
        ContextJNDISelector selector = (ContextJNDISelector) ContextSelectorStaticBinder.getSingleton().getContextSelector();
        contextDetachingSCL.contextDestroyed(null);
        assertEquals(0, selector.getCount());
    }

    @Test
    public void testDetachWithMissingContext() {
        MockInitialContext mic = MockInitialContextFactory.getContext();
        mic.map.put(ClassicConstants.JNDI_CONTEXT_NAME, "tata");
        ContextJNDISelector selector = (ContextJNDISelector) ContextSelectorStaticBinder.getSingleton().getContextSelector();
        assertEquals("tata", selector.getLoggerContext().getName());

        mic.map.put(ClassicConstants.JNDI_CONTEXT_NAME, "titi");
        assertEquals("titi", selector.getLoggerContext().getName());
        contextDetachingSCL.contextDestroyed(null);

        assertEquals(2, selector.getCount());
    }

}
