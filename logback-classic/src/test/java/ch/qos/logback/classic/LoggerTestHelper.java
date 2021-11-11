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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LoggerTestHelper {

    static void assertNameEquals(final Logger logger, final String name) {
        assertNotNull(logger);
        assertEquals(name, logger.getName());
    }

    static void assertLevels(final Level level, final Logger logger, final Level effectiveLevel) {
        if (level == null) {
            assertNull(logger.getLevel());
        } else {
            assertEquals(level, logger.getLevel());
        }
        assertEquals(effectiveLevel, logger.getEffectiveLevel());
    }
}