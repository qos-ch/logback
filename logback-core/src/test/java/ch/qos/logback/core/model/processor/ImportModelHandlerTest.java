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

package ch.qos.logback.core.model.processor;

import org.junit.jupiter.api.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ImportModelHandlerTest {

    Context context = new ContextBase();
    ImportModelHandler imh = new ImportModelHandler(context);

    @Test
    public void testStemExtraction() {
        assertNull(imh.extractStem(null));
        assertNull(imh.extractStem(""));
        assertNull(imh.extractStem("bla."));
        assertEquals("Foo", imh.extractStem("bla.Foo"));
        assertEquals("Foo", imh.extractStem("com.titi.bla.Foo"));

    }

}
