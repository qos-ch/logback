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

package ch.qos.logback.core.util;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.StatusManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatusListenerConfigHelperTest {

    Context context = new ContextBase();
    StatusManager sm = context.getStatusManager();

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void addOnConsoleListenerInstanceShouldNotStartSecondListener() {
        OnConsoleStatusListener ocl0 = new OnConsoleStatusListener();
        OnConsoleStatusListener ocl1 = new OnConsoleStatusListener();

        StatusListenerConfigHelper.addOnConsoleListenerInstance(context, ocl0);
        {
            List<StatusListener> listeners = sm.getCopyOfStatusListenerList();
            assertEquals(1, listeners.size());
            assertTrue(ocl0.isStarted());
        }

        // second listener should not have been started
        StatusListenerConfigHelper.addOnConsoleListenerInstance(context, ocl1);
        {
            List<StatusListener> listeners = sm.getCopyOfStatusListenerList();
            assertEquals(1, listeners.size());
            assertFalse(ocl1.isStarted());
        }
    }

}
