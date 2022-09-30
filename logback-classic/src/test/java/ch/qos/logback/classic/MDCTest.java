/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic;

import java.util.HashMap;

import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MDCTest {

    int diff = RandomUtil.getPositiveInt();


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


    // this test shows the
    @Disabled
    @Test
    public void closableTestA() {
        String key = "key-" + diff;
        String val = "val-" + diff;

        try (MDC.MDCCloseable closeable = MDC.putCloseable(key, val)) {
            if (1 == 1)
                throw new IllegalStateException("x");
        } catch (IllegalStateException e) {
            assertNotNull(MDC.get(key));
            assertEquals(val, MDC.get(key));
        } finally {
        }
        assertNull(MDC.get(key));
    }

    @Test
    public void closableTest() {
        String key = "key-" + diff;
        String val = "val-" + diff;
        MDC.MDCCloseable closeable = MDC.putCloseable(key, val);

        try {
            if (1 == 1)
                throw new IllegalStateException("x");
        } catch (IllegalStateException e) {
            assertNotNull(MDC.get(key));
            assertEquals(val, MDC.get(key));
        } finally {
            closeable.close();
        }
        assertNull(MDC.get(key));
    }

}
