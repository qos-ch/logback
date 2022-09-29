/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.pattern.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegularEscapeUtilTest {

    @BeforeEach
    public void setUp() throws Exception {
    }

    @Test
    public void basicEscape() {
        assertEquals("a", RegularEscapeUtil.basicEscape("a"));
        assertEquals("a\t", RegularEscapeUtil.basicEscape("a\t"));
        assertEquals("a\\", RegularEscapeUtil.basicEscape("a\\"));
        assertEquals("a\\", RegularEscapeUtil.basicEscape("a\\\\"));
    }

    @Test
    public void zbasicEscape() {

    }
}