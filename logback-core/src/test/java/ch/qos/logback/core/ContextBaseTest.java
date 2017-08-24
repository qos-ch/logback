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
package ch.qos.logback.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import ch.qos.logback.core.spi.LifeCycle;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class ContextBaseTest {

    private InstrumentedLifeCycleManager lifeCycleManager = new InstrumentedLifeCycleManager();

    private InstrumentedContextBase context = new InstrumentedContextBase(lifeCycleManager);

    @Test
    public void renameDefault() {
        context.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
        context.setName("hello");
    }

    @Test
    public void idempotentNameTest() {
        context.setName("hello");
        context.setName("hello");
    }

    @Test
    public void renameTest() {
        context.setName("hello");
        try {
            context.setName("x");
            fail("renaming is not allowed");
        } catch (IllegalStateException ise) {
        }
    }

    @Test
    public void resetTest() {
        context.setName("hello");
        context.putProperty("keyA", "valA");
        context.putObject("keyA", "valA");
        assertEquals("valA", context.getProperty("keyA"));
        assertEquals("valA", context.getObject("keyA"));
        MockLifeCycleComponent component = new MockLifeCycleComponent();
        context.register(component);
        assertSame(component, lifeCycleManager.getLastComponent());
        context.reset();
        assertNull(context.getProperty("keyA"));
        assertNull(context.getObject("keyA"));
        assertTrue(lifeCycleManager.isReset());
    }

    @Test
    public void contextNameProperty() {
        assertNull(context.getProperty(CoreConstants.CONTEXT_NAME_KEY));
        String HELLO = "hello";
        context.setName(HELLO);
        assertEquals(HELLO, context.getProperty(CoreConstants.CONTEXT_NAME_KEY));
        // good to have a raw reference to the "CONTEXT_NAME" as most clients would
        // not go through CoreConstants
        assertEquals(HELLO, context.getProperty("CONTEXT_NAME"));
    }

    private static class InstrumentedContextBase extends ContextBase {

        private final LifeCycleManager lifeCycleManager;

        public InstrumentedContextBase(LifeCycleManager lifeCycleManager) {
            this.lifeCycleManager = lifeCycleManager;
        }

        @Override
        protected LifeCycleManager getLifeCycleManager() {
            return lifeCycleManager;
        }

    }

    private static class InstrumentedLifeCycleManager extends LifeCycleManager {

        private LifeCycle lastComponent;
        private boolean reset;

        @Override
        public void register(LifeCycle component) {
            lastComponent = component;
            super.register(component);
        }

        @Override
        public void reset() {
            reset = true;
            super.reset();
        }

        public LifeCycle getLastComponent() {
            return lastComponent;
        }

        public boolean isReset() {
            return reset;
        }

    }

    @Test
    public void contextThreadpoolIsDaemonized() throws InterruptedException {
        ExecutorService execSvc = context.getExecutorService();
        final ArrayList<Thread> executingThreads = new ArrayList<Thread>();
        execSvc.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (executingThreads) {
                    executingThreads.add(Thread.currentThread());
                    executingThreads.notifyAll();
                }
            }
        });
        synchronized (executingThreads) {
            while (executingThreads.isEmpty()) {
                executingThreads.wait();
            }
        }
        assertTrue("executing thread should be a daemon thread.", executingThreads.get(0).isDaemon());
    }

}
