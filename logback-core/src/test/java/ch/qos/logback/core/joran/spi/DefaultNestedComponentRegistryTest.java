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
package ch.qos.logback.core.joran.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Window;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.joran.util.House;

public class DefaultNestedComponentRegistryTest {

    DefaultNestedComponentRegistry registry = new DefaultNestedComponentRegistry();

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void smoke() {
        String propertyName = "window";
        registry.add(House.class, propertyName, Window.class);
        Class<?> result = registry.findDefaultComponentType(House.class, propertyName);
        assertEquals(Window.class, result);
    }

    @Test
    public void absent() {
        registry.add(House.class, "a", Window.class);
        Class<?> result = registry.findDefaultComponentType(House.class, "other");
        assertNull(result);
    }
}
