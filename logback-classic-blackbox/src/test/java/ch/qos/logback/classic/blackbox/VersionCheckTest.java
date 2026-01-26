/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.classic.blackbox;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.util.CoreVersionUtil;
import ch.qos.logback.core.util.VersionUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The VersionCheckTest class is designed to perform a validation test
 * on the compatibility of version dependencies, specifically focusing
 * on the interaction between "logback-classic" and "logback-core" libraries.
 *
 * <p>In particular, it checks that when "logback-core" is older than version 1.5.25,
 * a NoClassDefFoundError is caught.
 * </p>
 *
 * <p>Use the following command to run this test
 * </p>
 *
 * <pre>  cd logback-classic-blackbox;
 * mvn test -P older-core -Dtest=ch.qos.logback.classic.blackbox.VersionCheckTest
 * </pre>
 *
 * @since 1.5.25
 */

public class VersionCheckTest {


    // WARNING: do not add other tests to this file

    LoggerContext loggerContext = new LoggerContext();

    /**
     *
     * Assertions:
     * 1. Verifies that the "olderCore" property matches the expected value "1.5.20".
     * 2. Ensures that a {@link NoClassDefFoundError} is thrown in presence of logback-core version
     * 1.5.25 or older.
     */
    @Test
    @Disabled
    public void versionTest() {
        String olderCoreVersion = System.getProperty("olderCore", "none");
        //assertEquals("1.5.20", olderCoreVersion);
        assertEquals("1.5.25", olderCoreVersion);
        try {
            VersionUtil.checkForVersionEquality(loggerContext, this.getClass(), CoreConstants.class, "logback-classic", "logback-core");
            fail("Expected NoClassDefFoundError");
        } catch (NoClassDefFoundError e) {
            // logback-core version is older than 1.5.25
            System.out.println("Got expected NoClassDefFoundError.");
        }
    }

    @Test
    public void otherVersionTest() {
        String olderCoreVersion = System.getProperty("olderCore", "none");
        //assertEquals("1.5.20", olderCoreVersion);
        assertEquals("1.5.25", olderCoreVersion);
        try {
            CoreVersionUtil.getCoreVersionBySelfDeclaredProperties();
            fail("Expected Error");
        } catch (NoClassDefFoundError e) {
            // logback-core version is 1.5.24 or older
            System.out.println("Got expected NoClassDefFoundError.");
        } catch (NoSuchMethodError e) {
            // logback-core version is 1.5.25 or older
            System.out.println("Got expected NoSuchFieldError.");
        }
    }




    // WARNING: do not add other tests to this file

}
