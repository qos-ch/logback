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
package ch.qos.logback.classic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.turbo.NOPTurboFilter;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.status.StatusManager;

public class LoggerContextTest {
    LoggerContext lc;

    @Before
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
        assertTrue("StatusManager has recieved too many messages", sm.getCount() == 1);
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

    @SuppressWarnings("unchecked")
    @Test
    public void collisionMapsPostReset() {
        lc.reset();

        Map<String, String> fileCollisions = (Map<String, String>) lc.getObject(CoreConstants.FA_FILENAME_COLLISION_MAP);
        assertNotNull(fileCollisions);
        assertTrue(fileCollisions.isEmpty());

        Map<String, String> filenamePatternCollisionMap = (Map<String, String>) lc.getObject(CoreConstants.RFA_FILENAME_PATTERN_COLLISION_MAP);
        assertNotNull(filenamePatternCollisionMap);
        assertTrue(filenamePatternCollisionMap.isEmpty());
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

}