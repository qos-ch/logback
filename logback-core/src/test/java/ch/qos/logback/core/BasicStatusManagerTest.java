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

import static ch.qos.logback.core.BasicStatusManager.MAX_HEADER_COUNT;
import static ch.qos.logback.core.BasicStatusManager.TAIL_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;

public class BasicStatusManagerTest {

    BasicStatusManager bsm = new BasicStatusManager();

    @Test
    public void smoke() {
        bsm.add(new ErrorStatus("hello", this));
        assertEquals(Status.ERROR, bsm.getLevel());

        final List<Status> statusList = bsm.getCopyOfStatusList();
        assertNotNull(statusList);
        assertEquals(1, statusList.size());
        assertEquals("hello", statusList.get(0).getMessage());
    }

    @Test
    public void many() {
        final int margin = 300;
        final int len = MAX_HEADER_COUNT + TAIL_SIZE + margin;
        for (int i = 0; i < len; i++) {
            bsm.add(new ErrorStatus("" + i, this));
        }

        final List<Status> statusList = bsm.getCopyOfStatusList();
        assertNotNull(statusList);
        assertEquals(MAX_HEADER_COUNT + TAIL_SIZE, statusList.size());
        final List<Status> witness = new ArrayList<>();
        for (int i = 0; i < MAX_HEADER_COUNT; i++) {
            witness.add(new ErrorStatus("" + i, this));
        }
        for (int i = 0; i < TAIL_SIZE; i++) {
            witness.add(new ErrorStatus("" + (MAX_HEADER_COUNT + margin + i), this));
        }
        assertEquals(witness, statusList);
    }

    @Test
    public void duplicateInstallationsOfOnConsoleListener() {
        final OnConsoleStatusListener sl0 = new OnConsoleStatusListener();
        sl0.start();
        final OnConsoleStatusListener sl1 = new OnConsoleStatusListener();
        sl1.start();

        assertTrue(bsm.add(sl0));

        {
            final List<StatusListener> listeners = bsm.getCopyOfStatusListenerList();
            assertEquals(1, listeners.size());
        }

        assertFalse(bsm.add(sl1));
        {
            final List<StatusListener> listeners = bsm.getCopyOfStatusListenerList();
            assertEquals(1, listeners.size());
        }
    }

}
