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
package ch.qos.logback.classic.model.processor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ConfigurationModelHandlerTest {

    @Test
    public void scanStringToBooleanTreatsBlankAsUnset() {
        assertNull(ConfigurationModelHandler.scanStringToBoolean(null));
        assertNull(ConfigurationModelHandler.scanStringToBoolean(""));
        assertNull(ConfigurationModelHandler.scanStringToBoolean("   "));
    }

    @Test
    public void scanStringToBooleanHonorsTrueAndFalse() {
        assertEquals(Boolean.TRUE, ConfigurationModelHandler.scanStringToBoolean("true"));
        assertEquals(Boolean.TRUE, ConfigurationModelHandler.scanStringToBoolean("TRUE"));
        assertEquals(Boolean.FALSE, ConfigurationModelHandler.scanStringToBoolean("false"));
        assertEquals(Boolean.FALSE, ConfigurationModelHandler.scanStringToBoolean("FALSE"));
        assertEquals(Boolean.FALSE, ConfigurationModelHandler.scanStringToBoolean("  false  "));
    }

    @Test
    public void scanStringToBooleanDefaultsUnrecognizedValuesToTrue() {
        assertEquals(Boolean.TRUE, ConfigurationModelHandler.scanStringToBoolean("yes"));
        assertEquals(Boolean.TRUE, ConfigurationModelHandler.scanStringToBoolean("1"));
        assertEquals(Boolean.TRUE, ConfigurationModelHandler.scanStringToBoolean("${logback.scan.enabled:-true}"));
    }
}
