/*
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

package ch.qos.logback.core.encoder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonEscapeUtilTest {

    @Test
    public void smokeTestEscapeCodes() {
        assertEquals("\\u0001", JsonEscapeUtil.ESCAPE_CODES[1]);
        assertEquals("\\u0005", JsonEscapeUtil.ESCAPE_CODES[5]);
        assertEquals("\\b", JsonEscapeUtil.ESCAPE_CODES[8]);
        assertEquals("\\t", JsonEscapeUtil.ESCAPE_CODES[9]);
        assertEquals("\\n", JsonEscapeUtil.ESCAPE_CODES[0x0A]);
        assertEquals("\\u000B", JsonEscapeUtil.ESCAPE_CODES[0x0B]);
        assertEquals("\\f", JsonEscapeUtil.ESCAPE_CODES[0x0C]);
        assertEquals("\\r", JsonEscapeUtil.ESCAPE_CODES[0x0D]);
        assertEquals("\\u000E", JsonEscapeUtil.ESCAPE_CODES[0x0E]);

        assertEquals("\\u001A", JsonEscapeUtil.ESCAPE_CODES[0x1A]);
    }

    @Test
    public void smokeTestEscapeString() {
        assertEquals("abc", JsonEscapeUtil.jsonEscapeString("abc"));
        assertEquals("{world: \\\"world\\\"}", JsonEscapeUtil.jsonEscapeString("{world: \"world\"}"));
        assertEquals("{world: "+'\\'+'"'+"world\\\"}", JsonEscapeUtil.jsonEscapeString("{world: \"world\"}"));
    }

    @Test
    public void testEscapingLF() {
        String input = "{\nhello: \"wo\nrld\"}";
        System.out.println(input);
        assertEquals("{\\nhello: "+'\\'+'"'+"wo\\nrld\\\"}", JsonEscapeUtil.jsonEscapeString(input));
    }

    @Test
    public void testEscapingTab() {
        String input = "{hello: \"\tworld\"}";
        System.out.println(input);
        assertEquals("{hello: "+'\\'+'"'+"\\tworld\\\"}", JsonEscapeUtil.jsonEscapeString(input));
    }
}