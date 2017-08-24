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
import static org.junit.Assert.assertNull;

import java.util.HashMap;

import org.junit.Test;
import org.slf4j.MDC;

public class MDCTest {

    @Test
    public void test() throws InterruptedException {
        MDCTestThread threadA = new MDCTestThread("a");
        threadA.start();

        MDCTestThread threadB = new MDCTestThread("b");
        threadB.start();

        threadA.join();
        threadB.join();

        assertNull(threadA.x0);
        assertEquals("a", threadA.x1);
        assertNull(threadA.x2);

        assertNull(threadB.x0);
        assertEquals("b", threadB.x1);
        assertNull(threadB.x2);

    }

    @Test
    public void testLBCLASSIC_98() {
        MDC.setContextMap(new HashMap<String, String>());
    }

}
