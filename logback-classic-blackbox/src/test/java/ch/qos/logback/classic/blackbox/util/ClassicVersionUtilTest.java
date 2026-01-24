/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 *  Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *     or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.classic.blackbox.util;

import ch.qos.logback.classic.util.ClassicVersionUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClassicVersionUtilTest {

    @Test
    public void bySelfDeclaredProperties() {
        String version = ClassicVersionUtil.getVersionBySelfDeclaredProperties();
        assertNotNull(version);
        assertTrue(version.startsWith("1.5"));
    }

}
