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
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.classic.util.MockInitialContext;
import ch.qos.logback.classic.util.MockInitialContextFactory;
import ch.qos.logback.core.Context;

@Ignore
public class ContextJNDISelectorTest {

    static String INITIAL_CONTEXT_KEY = "java.naming.factory.initial";

    @Before
    public void setUp() throws Exception {

        System.setProperty(ClassicConstants.LOGBACK_CONTEXT_SELECTOR, "JNDI");
        LoggerFactoryFriend.reset();

        MockInitialContextFactory.initialize();
        final MockInitialContext mic = MockInitialContextFactory.getContext();
        mic.map.put(ClassicConstants.JNDI_CONTEXT_NAME, "toto");

        // The property must be set after we setup the Mock
        System.setProperty(INITIAL_CONTEXT_KEY, MockInitialContextFactory.class.getName());

        // this call will create the context "toto"
        LoggerFactory.getLogger(ContextDetachingSCLTest.class);
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(INITIAL_CONTEXT_KEY);
    }

    @Test
    public void testGetExistingContext() {
        final ContextSelector selector = ContextSelectorStaticBinder.getSingleton().getContextSelector();
        final Context context = selector.getLoggerContext();
        assertEquals("toto", context.getName());
    }

    @Test
    public void testCreateContext() {
        final MockInitialContext mic = MockInitialContextFactory.getContext();
        mic.map.put(ClassicConstants.JNDI_CONTEXT_NAME, "tata");

        LoggerFactory.getLogger(ContextDetachingSCLTest.class);

        final ContextJNDISelector selector = (ContextJNDISelector) ContextSelectorStaticBinder.getSingleton().getContextSelector();
        final Context context = selector.getLoggerContext();
        assertEquals("tata", context.getName());
        System.out.println(selector.getContextNames());
        assertEquals(2, selector.getCount());
    }

    @Test
    public void defaultContext() {
        final MockInitialContext mic = MockInitialContextFactory.getContext();
        mic.map.put(ClassicConstants.JNDI_CONTEXT_NAME, null);

        final ContextJNDISelector selector = (ContextJNDISelector) ContextSelectorStaticBinder.getSingleton().getContextSelector();
        final Context context = selector.getLoggerContext();

        assertEquals("default", context.getName());
    }

}
