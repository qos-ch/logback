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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.StatusBase;
import ch.qos.logback.core.status.StatusListener;

import ch.qos.logback.core.status.StatusUtil;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.Status;

public class BasicStatusManagerTest {

    BasicStatusManager bsm = new BasicStatusManager();

    @Test
    public void smoke() {
        bsm.add(new ErrorStatus("hello", this));
        assertEquals(Status.ERROR, bsm.getLevel());

        List<Status> statusList = bsm.getCopyOfStatusList();
        assertNotNull(statusList);
        assertEquals(1, statusList.size());
        assertEquals("hello", statusList.get(0).getMessage());
    }

    @Test
    public void many() {
        int margin = 300;
        int len = MAX_HEADER_COUNT + TAIL_SIZE + margin;
        List<Status> witness = new ArrayList<Status>();
        for (int i = 0; i < len; i++) {
            Status s = new ErrorStatus("" + i, this);
            bsm.add(s);
            if(i < MAX_HEADER_COUNT) {
                witness.add(s);
            }
            if(i >= MAX_HEADER_COUNT + margin) {
                witness.add(s);
            }
        }

        List<Status> statusList = bsm.getCopyOfStatusList();
        assertNotNull(statusList);
        assertEquals(MAX_HEADER_COUNT + TAIL_SIZE, statusList.size());

        arrayDiff(witness, statusList);
    }

    private void arrayDiff(List<Status> witness, List<Status> otherList) {
        int witnessSize = witness.size();
        int otherSize = otherList.size();
        boolean diff = false;
        for(int i = 0; i < witness.size(); i++) {

            Status w = witness.get(i);
            Status o = otherList.get(i);
            if(!w.equals(o)) {
                System.out.println("at "+i + " differs w.message=" + w.getMessage() + " and o.message=" +o.getMessage());
                String diffMsg = StatusUtil.diff(w, o);
                System.out.println(diffMsg);
                diff = true;
            }
        }


        assertEquals(witnessSize, otherSize, "witnessSize="+witnessSize+" does not match resultSize="+otherSize);
        assertFalse(diff, "diff detected");
    }

    @Test
    public void duplicateInstallationsOfOnConsoleListener() {
        OnConsoleStatusListener sl0 = new OnConsoleStatusListener();
        sl0.start();
        OnConsoleStatusListener sl1 = new OnConsoleStatusListener();
        sl1.start();

        assertTrue(bsm.add(sl0));

        {
            List<StatusListener> listeners = bsm.getCopyOfStatusListenerList();
            assertEquals(1, listeners.size());
        }

        assertFalse(bsm.add(sl1));
        {
            List<StatusListener> listeners = bsm.getCopyOfStatusListenerList();
            assertEquals(1, listeners.size());
        }
    }

}
