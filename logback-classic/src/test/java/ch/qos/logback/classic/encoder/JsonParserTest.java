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


package ch.qos.logback.classic.encoder;

import ch.qos.logback.classic.encoder.JsonParser.JsonNumber;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonParserTest {

    @Test
    void literals() {
        assertEquals(Boolean.TRUE, JsonParser.parse("true"));
        assertEquals(Boolean.FALSE, JsonParser.parse("false"));
        assertNull(JsonParser.parse("null"));
        assertEquals("", JsonParser.parse("\"\""));
    }

    @Test
    void escapes() {
        assertEquals("\" \\ / \b \f \n \r \t", JsonParser.parse("\"\\\" \\\\ \\/ \\b \\f \\n \\r \\t\""));
        assertEquals("A\u00e9\u0001", JsonParser.parse("\"\\u0041\\u00E9\\u0001\""));
        assertEquals("\u00e9", JsonParser.parse("\"\\u00e9\""));
        // surrogate pair
        assertEquals(new String(Character.toChars(0x1F600)), JsonParser.parse("\"\\uD83D\\uDE00\""));
        // raw, unescaped non-ASCII text
        assertEquals("日本 " + new String(Character.toChars(0x1F600)),
                JsonParser.parse("\"日本 " + new String(Character.toChars(0x1F600)) + "\""));
        // a lone surrogate is kept
        assertEquals(String.valueOf((char) 0xD800), JsonParser.parse("\"\\uD800\""));
    }

    @Test
    void numbers() {
        for (String n : Arrays.asList("0", "-0", "12", "-12", "1.5", "-0.25", "1e10", "1E+10", "1.5e-3",
                "123456789012345678901234567890")) {
            assertEquals(new JsonNumber(n), JsonParser.parse(n), n);
        }
        assertTrue(((JsonNumber) JsonParser.parse("-12")).isIntegral());
        assertFalse(((JsonNumber) JsonParser.parse("1e3")).isIntegral());
        assertFalse(((JsonNumber) JsonParser.parse("1.0")).isIntegral());
    }

    @Test
    void nestingAndWhitespace() {
        Object value = JsonParser.parse(" \t\r\n{ \"a\" : [ 1 , { \"b\" : [ ] } , [ [ ] ] ] , \"c\" : { } }\n ");
        Map<String, Object> expected = new LinkedHashMap<>();
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("b", List.of());
        expected.put("a", Arrays.asList(new JsonNumber("1"), inner, List.of(List.of())));
        expected.put("c", new LinkedHashMap<>());
        assertEquals(expected, value);
    }

    @Test
    void memberOrderIsKept() {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) JsonParser.parse("{\"z\":1,\"a\":2,\"m\":3}");
        assertEquals(List.of("z", "a", "m"), List.copyOf(map.keySet()));
    }

    @Test
    void errorsGiveOffset() {
        assertError("{\"a\":}", "at offset 5");
        assertError("[1,]", "at offset 3");
        assertError("[1 2]", "at offset 3");
        assertError("{\"a\" 1}", "at offset 5");
        assertError("{a:1}", "at offset 1");
        assertError("\"abc", "at end of input (offset 4)");
        assertError("01", "at offset 1");
        assertError("1.", "at end of input (offset 2)");
        assertError("1e", "at end of input (offset 2)");
        assertError("-", "at end of input (offset 1)");
        assertError("+1", "at offset 0");
        assertError("tru", "at offset 0");
        assertError("nul", "at offset 0");
        assertError("\"a\nb\"", "at offset 2");
        assertError("\"\\x\"", "at offset 2");
        assertError("\"\\u12G4\"", "at offset 5");
        assertError("\"\\u12", "at offset 3");
        assertError("", "at end of input (offset 0)");
        assertError("{} {}", "at offset 3");
        assertError("{\"a\":1", "at end of input (offset 6)");
    }

    @Test
    void tooDeep() {
        String deep = "[".repeat(JsonParser.MAX_DEPTH + 1) + "]".repeat(JsonParser.MAX_DEPTH + 1);
        assertError(deep, "nesting deeper than");
        String ok = "[".repeat(JsonParser.MAX_DEPTH) + "]".repeat(JsonParser.MAX_DEPTH);
        JsonParser.parse(ok);
    }

    private static void assertError(String input, String expectedFragment) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> JsonParser.parse(input),
                input);
        assertTrue(e.getMessage().contains(expectedFragment), input + " -> " + e.getMessage());
    }
}
