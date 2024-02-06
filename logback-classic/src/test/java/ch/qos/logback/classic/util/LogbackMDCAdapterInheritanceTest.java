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
package ch.qos.logback.classic.util;

import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class LogbackMDCAdapterInheritanceTest extends LogbackMDCAdapterTest {

    protected boolean copyOnInhertiance() {
        return true;
    }

    /**
     * Test that LogbackMDCAdapter copies its hashmap when a child
     * thread inherits it.
     *
     * @throws InterruptedException
     */
    @Test
    public void copyOnInheritenceTest() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String firstKey = "x" + diff;
        String secondKey = "o" + diff;
        mdcAdapter.put(firstKey, firstKey + A_SUFFIX);

        ChildThread childThread = new ChildThread(mdcAdapter, firstKey, secondKey, countDownLatch, firstKey + A_SUFFIX);
        childThread.start();
        countDownLatch.await();
        mdcAdapter.put(firstKey, firstKey + B_SUFFIX);
        childThread.join();

        assertNull(mdcAdapter.get(secondKey));
        assertTrue(childThread.successful);

        Map<String, String> parentHM = getMapFromMDCAdapter(mdcAdapter);
        assertTrue(parentHM != childThread.childHM);

        Map<String, String> parentHMWitness = new  HashMap<String, String>();
        parentHMWitness.put(firstKey, firstKey + B_SUFFIX);
        assertEquals(parentHMWitness, parentHM);

        Map<String, String> childHMWitness = new  HashMap<String, String>();
        childHMWitness.put(firstKey, firstKey + A_SUFFIX);
        childHMWitness.put(secondKey, secondKey + A_SUFFIX);
        assertEquals(childHMWitness, childThread.childHM);

    }


}
