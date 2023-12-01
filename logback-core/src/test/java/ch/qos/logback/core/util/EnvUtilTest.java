/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2023, QOS.ch. All rights reserved.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnvUtilTest {


    @Test
    public void jdkVersion() {
        assertEquals(4, EnvUtil.getJDKVersion("1.4.xx"));
        assertEquals(5, EnvUtil.getJDKVersion("1.5"));
        assertEquals(5, EnvUtil.getJDKVersion("1.5.xx"));
        assertEquals(5, EnvUtil.getJDKVersion("1.5AA"));
        assertEquals(9, EnvUtil.getJDKVersion("9EA"));
        assertEquals(9, EnvUtil.getJDKVersion("9.0.1"));
        assertEquals(18, EnvUtil.getJDKVersion("18.3+xx"));
        assertEquals(21, EnvUtil.getJDKVersion("21.0.1"));
    }
}
