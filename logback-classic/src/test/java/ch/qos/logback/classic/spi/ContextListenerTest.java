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
package ch.qos.logback.classic.spi;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.BasicContextListener.UpdateType;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContextListenerTest {

    LoggerContext loggerContext;
    BasicContextListener listener;

    @BeforeEach
    public void setUp() throws Exception {
        loggerContext = new LoggerContext();
        loggerContext.start();
        listener = new BasicContextListener();
        loggerContext.addListener(listener);
    }

    @Test
    public void testNotifyOnReset() {
        loggerContext.reset();
        assertEquals(UpdateType.RESET, listener.updateType);
        assertEquals(listener.context, loggerContext);
    }

    @Test
    public void testResistantListener_NotifyOnStop() {
        listener.setResetResistant(true);
        loggerContext.stop();
        assertEquals(UpdateType.STOP, listener.updateType);
        assertEquals(listener.context, loggerContext);
    }

    @Test
    public void testNotResistantListener_NotifyOnStop() {
        loggerContext.stop();
        assertEquals(UpdateType.RESET, listener.updateType);
        assertEquals(listener.context, loggerContext);
    }

    @Test
    public void testNotifyOnStart() {
        loggerContext.start();
        assertEquals(UpdateType.START, listener.updateType);
        assertEquals(listener.context, loggerContext);
    }

    void checkLevelChange(String loggerName, Level level) {
        Logger logger = loggerContext.getLogger(loggerName);
        logger.setLevel(level);

        assertEquals(UpdateType.LEVEL_CHANGE, listener.updateType);
        assertEquals(listener.logger, logger);
        assertEquals(listener.level, level);

    }

    @Test
    public void testLevelChange() {
        checkLevelChange("a", Level.INFO);
        checkLevelChange("a.b", Level.ERROR);
        checkLevelChange("a.b.c", Level.DEBUG);
    }
}
