/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 2017, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Patrick Reinhart
 */
public class EnvUtilTest {
    @Mock
    private String savedVersion = System.getProperty("java.version");

    @After
    public void tearDown() {
        System.setProperty("java.version", savedVersion);
    }

    @Test
    public void jdkVersion() {
        assertEquals(4, EnvUtil.getJDKVersion("1.4.xx"));
        assertEquals(5, EnvUtil.getJDKVersion("1.5"));
        assertEquals(5, EnvUtil.getJDKVersion("1.5.xx"));
        assertEquals(5, EnvUtil.getJDKVersion("1.5AA"));
        assertEquals(9, EnvUtil.getJDKVersion("9EA"));
        assertEquals(9, EnvUtil.getJDKVersion("9.0.1"));
        assertEquals(18, EnvUtil.getJDKVersion("18.3+xx"));
    }

    @Test
    public void testJava1_4() {
        System.setProperty("java.version", "1.4.xx");

        assertFalse(EnvUtil.isJDK5());
        assertFalse(EnvUtil.isJDK6OrHigher());
        assertFalse(EnvUtil.isJDK7OrHigher());
    }

    @Test
    public void testJava1_5() {
        System.setProperty("java.version", "1.5");

        assertTrue(EnvUtil.isJDK5());
        assertFalse(EnvUtil.isJDK6OrHigher());
        assertFalse(EnvUtil.isJDK7OrHigher());
    }

    @Test
    public void testJava1_5_x() {
        System.setProperty("java.version", "1.5.xx");

        assertTrue(EnvUtil.isJDK5());
        assertFalse(EnvUtil.isJDK6OrHigher());
        assertFalse(EnvUtil.isJDK7OrHigher());
    }

    @Test
    public void testJava1_6() {
        System.setProperty("java.version", "1.6.xx");

        assertTrue(EnvUtil.isJDK5());
        assertTrue(EnvUtil.isJDK6OrHigher());
        assertFalse(EnvUtil.isJDK7OrHigher());
    }

    @Test
    public void testJava1_7() {
        System.setProperty("java.version", "1.7.xx");

        assertTrue(EnvUtil.isJDK5());
        assertTrue(EnvUtil.isJDK6OrHigher());
        assertTrue(EnvUtil.isJDK7OrHigher());
    }

    @Test
    public void testJava1_8() {
        System.setProperty("java.version", "1.8.xx");

        assertTrue(EnvUtil.isJDK5());
        assertTrue(EnvUtil.isJDK6OrHigher());
        assertTrue(EnvUtil.isJDK7OrHigher());
    }

    @Test
    public void testJava9() {
        System.setProperty("java.version", "9");

        assertTrue(EnvUtil.isJDK5());
        assertTrue(EnvUtil.isJDK6OrHigher());
        assertTrue(EnvUtil.isJDK7OrHigher());
    }

    @Test
    public void testJava9_1() {
        System.setProperty("java.version", "9.xx");

        assertTrue(EnvUtil.isJDK5());
        assertTrue(EnvUtil.isJDK6OrHigher());
        assertTrue(EnvUtil.isJDK7OrHigher());
    }

    @Test
    public void testJava18_3() {
        System.setProperty("java.version", "18.3+xx");
        assertEquals(18, EnvUtil.getJDKVersion("18.3+xx"));
        assertTrue(EnvUtil.isJDK5());
        assertTrue(EnvUtil.isJDK6OrHigher());
        assertTrue(EnvUtil.isJDK7OrHigher());
    }
}
