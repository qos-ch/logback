/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic;

import ch.qos.logback.classic.turbo.NOPTurboFilter;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.status.StatusManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

public class LoggerContextTest {
    LoggerContext lc;

    @BeforeEach
    public void setUp() throws Exception {
        lc = new LoggerContext();
        lc.setName("x");
    }

    @Test
    public void testRootGetLogger() {
        Logger root = lc.getLogger(Logger.ROOT_LOGGER_NAME);
        assertEquals(Level.DEBUG, root.getLevel());
        assertEquals(Level.DEBUG, root.getEffectiveLevel());
    }

    @Test
    public void testLoggerX() {
        Logger x = lc.getLogger("x");
        assertNotNull(x);
        assertEquals("x", x.getName());
        assertNull(x.getLevel());
        assertEquals(Level.DEBUG, x.getEffectiveLevel());
    }

    @Test
    public void testNull() {
        try {
            lc.getLogger((String) null);
            fail("null should cause an exception");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testEmpty() {
        Logger empty = lc.getLogger("");
        LoggerTestHelper.assertNameEquals(empty, "");
        LoggerTestHelper.assertLevels(null, empty, Level.DEBUG);

        Logger dot = lc.getLogger(".");
        LoggerTestHelper.assertNameEquals(dot, ".");
        // LoggerTestHelper.assertNameEquals(dot.parent, "");
        // LoggerTestHelper.assertNameEquals(dot.parent.parent, "root");

        // assertNull(dot.parent.parent.parent);
        LoggerTestHelper.assertLevels(null, dot, Level.DEBUG);

        assertEquals(3, lc.getLoggerList().size());
    }

    @Test
    public void testDotDot() {
        Logger dotdot = lc.getLogger("..");
        assertEquals(4, lc.getLoggerList().size());
        LoggerTestHelper.assertNameEquals(dotdot, "..");
        // LoggerTestHelper.assertNameEquals(dotdot.parent, ".");
        // LoggerTestHelper.assertNameEquals(dotdot.parent.parent, "");
        // LoggerTestHelper.assertNameEquals(dotdot.parent.parent.parent, "root");
    }

    int instanceCount() {
        return lc.getLoggerList().size();
    }

    @Test
    public void testLoggerXY() {
        assertEquals(1, lc.getLoggerList().size());

        Logger xy = lc.getLogger("x.y");
        assertEquals(3, instanceCount());
        LoggerTestHelper.assertNameEquals(xy, "x.y");
        LoggerTestHelper.assertLevels(null, xy, Level.DEBUG);

        Logger x = lc.getLogger("x");
        assertEquals(3, instanceCount());

        Logger xy2 = lc.getLogger("x.y");
        assertEquals(xy, xy2);

        Logger x2 = lc.getLogger("x");
        assertEquals(x, x2);
        assertEquals(3, instanceCount());
    }

    @Test
    public void testLoggerMultipleChildren() {
        assertEquals(1, instanceCount());
        Logger xy0 = lc.getLogger("x.y0");
        LoggerTestHelper.assertNameEquals(xy0, "x.y0");

        Logger xy1 = lc.getLogger("x.y1");
        LoggerTestHelper.assertNameEquals(xy1, "x.y1");

        LoggerTestHelper.assertLevels(null, xy0, Level.DEBUG);
        LoggerTestHelper.assertLevels(null, xy1, Level.DEBUG);
        assertEquals(4, instanceCount());

        for (int i = 0; i < 100; i++) {
            Logger xy_i = lc.getLogger("x.y" + i);
            LoggerTestHelper.assertNameEquals(xy_i, "x.y" + i);
            LoggerTestHelper.assertLevels(null, xy_i, Level.DEBUG);
        }
        assertEquals(102, instanceCount());
    }

    @Test
    public void testMultiLevel() {
        Logger wxyz = lc.getLogger("w.x.y.z");
        LoggerTestHelper.assertNameEquals(wxyz, "w.x.y.z");
        LoggerTestHelper.assertLevels(null, wxyz, Level.DEBUG);

        Logger wx = lc.getLogger("w.x");
        wx.setLevel(Level.INFO);
        LoggerTestHelper.assertNameEquals(wx, "w.x");
        LoggerTestHelper.assertLevels(Level.INFO, wx, Level.INFO);
        LoggerTestHelper.assertLevels(null, lc.getLogger("w.x.y"), Level.INFO);
        LoggerTestHelper.assertLevels(null, wxyz, Level.INFO);
    }

    @Test
    public void testStatusWithUnconfiguredContext() {
        Logger logger = lc.getLogger(LoggerContextTest.class);

        for (int i = 0; i < 3; i++) {
            logger.debug("test");
        }

        logger = lc.getLogger("x.y.z");

        for (int i = 0; i < 3; i++) {
            logger.debug("test");
        }

        StatusManager sm = lc.getStatusManager();
        assertTrue(sm.getCount() == 1, "StatusManager has recieved too many messages");
    }

    @Test
    public void resetTest() {

        Logger root = lc.getLogger(Logger.ROOT_LOGGER_NAME);
        Logger a = lc.getLogger("a");
        Logger ab = lc.getLogger("a.b");

        ab.setLevel(Level.WARN);
        root.setLevel(Level.INFO);
        lc.reset();
        assertEquals(Level.DEBUG, root.getEffectiveLevel());
        assertTrue(root.isDebugEnabled());
        assertEquals(Level.DEBUG, a.getEffectiveLevel());
        assertEquals(Level.DEBUG, ab.getEffectiveLevel());

        assertEquals(Level.DEBUG, root.getLevel());
        assertNull(a.getLevel());
        assertNull(ab.getLevel());
    }

    // http://jira.qos.ch/browse/LBCLASSIC-89
    @Test
    public void turboFilterStopOnReset() {
        NOPTurboFilter nopTF = new NOPTurboFilter();
        nopTF.start();
        lc.addTurboFilter(nopTF);
        assertTrue(nopTF.isStarted());
        lc.reset();
        assertFalse(nopTF.isStarted());
    }

    @Test
    public void resetTest_LBCORE_104() {
        lc.putProperty("keyA", "valA");
        lc.putObject("keyA", "valA");
        assertEquals("valA", lc.getProperty("keyA"));
        assertEquals("valA", lc.getObject("keyA"));
        lc.reset();
        assertNull(lc.getProperty("keyA"));
        assertNull(lc.getObject("keyA"));
    }

    @Test
    public void loggerNameEndingInDotOrDollarShouldWork() {
        {
            String loggerName = "toto.x.";
            Logger logger = lc.getLogger(loggerName);
            assertEquals(loggerName, logger.getName());
        }

        {
            String loggerName = "toto.x$";
            Logger logger = lc.getLogger(loggerName);
            assertEquals(loggerName, logger.getName());
        }
    }

    @Test
    public void levelResetTest() {
        Logger root = lc.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.TRACE);
        assertTrue(root.isTraceEnabled());
        lc.reset();
        assertFalse(root.isTraceEnabled());
        assertTrue(root.isDebugEnabled());
    }

    @Test
    public void evaluatorMapPostReset() {
        lc.reset();
        assertNotNull(lc.getObject(CoreConstants.EVALUATOR_MAP));
    }

    // http://jira.qos.ch/browse/LOGBACK-142
    @Test
    public void concurrentModification() {
        final int runLen = 100;
        Thread thread = new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < runLen; i++) {
                    lc.getLogger("a" + i);
                    Thread.yield();
                }
            }
        });
        thread.start();

        for (int i = 0; i < runLen; i++) {
            lc.putProperty("a" + i, "val");
            Thread.yield();
        }
    }

    // https://github.com/qos-ch/logback/issues/1038
    // getLogger() creates loggers under the per-parent monitor, so concurrent
    // creations below distinct parents run under different locks. The internal
    // logger count must be maintained atomically; otherwise incSize()'s size++
    // races and increments are lost, leaving size() below the real count.
    @Test
    public void concurrentGetLoggerKeepsSizeConsistent() throws InterruptedException {
        final int threadCount = 16;
        final int loggersPerThread = 500;
        // each thread works under its own parent ("t<idx>") so its child
        // creations hold a distinct monitor, maximising contention on size.
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        final CyclicBarrier start = new CyclicBarrier(threadCount);
        List<Future<?>> futures = new ArrayList<Future<?>>();
        for (int t = 0; t < threadCount; t++) {
            final int idx = t;
            futures.add(pool.submit(new Runnable() {
                public void run() {
                    try {
                        start.await();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    for (int j = 0; j < loggersPerThread; j++) {
                        lc.getLogger("t" + idx + ".c" + j);
                    }
                }
            }));
        }
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e.getCause());
            }
        }
        pool.shutdown();

        // root + one "t<idx>" parent per thread + all children
        int expected = 1 + threadCount + threadCount * loggersPerThread;
        assertEquals(expected, lc.getLoggerList().size());
        assertEquals(expected, lc.size());
    }

}