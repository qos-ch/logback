/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.blackbox.util;

import ch.qos.logback.classic.util.ClassicEnvUtil;
import ch.qos.logback.core.util.EnvUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnvUtilTest {

    // Beware: ----------------------------------------
    // Beware:  needs to be updated upon version change
    // Beware: ----------------------------------------
    static final String EXPECTED_VERSION = "1.5";


    @BeforeEach
    public void setUp() throws Exception {

    }

    // this test runs fine if run from logback-classic but fails when
    // run from logback-core. This is due to the fact that package information
    // is added when creating the jar.
    @Test
    public void versionTest() {
        String versionStr = EnvUtil.logbackVersion();
        assertNotNull(versionStr);
        assertTrue(versionStr.startsWith(EXPECTED_VERSION));
    }

    @Test
    public void versionCompare() {
        String coreVersionStr = EnvUtil.logbackVersion();
        String versionOfLogbackClassic = ClassicEnvUtil.getVersionOfLogbackClassic();
        assertNotNull(coreVersionStr);
        assertNotNull(versionOfLogbackClassic);

        assertEquals(coreVersionStr, versionOfLogbackClassic);
    }


}
