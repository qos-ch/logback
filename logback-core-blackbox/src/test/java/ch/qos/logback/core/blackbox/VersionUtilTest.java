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

package ch.qos.logback.core.blackbox;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.util.VersionUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class VersionUtilTest {


    @Test
    public void smoke() {

//        {
//            long startTime = System.nanoTime();
//            String result = VersionUtil.getVersionOfArtifact(CoreConstants.class);
//            long endTime = System.nanoTime();
//            System.out.println(result);
//            System.out.println("Took " + (endTime - startTime)/1000L + " micros");
//        }


        {
            long startTime = System.nanoTime();
            String result = VersionUtil.getArtifactVersionBySelfDeclaredProperties(CoreConstants.class, "logback-core");
            long endTime = System.nanoTime();
            System.out.println("Took " + (endTime - startTime)/1000L + " micros");
            assertNotNull(result);
            assertTrue(result.startsWith("1.5"));
        }
    }
}
