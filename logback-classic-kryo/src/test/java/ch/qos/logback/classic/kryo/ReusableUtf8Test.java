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
package ch.qos.logback.classic.kryo;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class ReusableUtf8Test {

    @Test
    public void encodeMatchesStringGetBytes() {
        ReusableUtf8 utf8 = new ReusableUtf8();
        String[] samples = {
                "",
                "hello",
                "café",
                "日本語",
                "emoji 😀",
                "a".repeat(400),
                "中".repeat(200),
                "\uD800 unpaired high",
                "mix\0null"
        };
        for (String s : samples) {
            byte[] expected = s.getBytes(StandardCharsets.UTF_8);
            int n = utf8.encode(s);
            assertThat(java.util.Arrays.copyOf(utf8.buffer(), n))
                    .as("encode(%s)", printable(s))
                    .isEqualTo(expected);
        }
    }

    @Test
    public void decodeMatchesNewString() {
        ReusableUtf8 utf8 = new ReusableUtf8();
        String[] samples = {"", "hello", "café", "日本語", "emoji 😀", "中".repeat(200)};
        for (String s : samples) {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            assertThat(utf8.decode(bytes, 0, bytes.length)).isEqualTo(s);
        }
    }

    @Test
    public void decodeRespectsOffsetAndLength() {
        ReusableUtf8 utf8 = new ReusableUtf8();
        byte[] padded = "xxhello!".getBytes(StandardCharsets.UTF_8);
        assertThat(utf8.decode(padded, 2, 5)).isEqualTo("hello");
    }

    @Test
    public void encodeIntoDestAtOffset() {
        ReusableUtf8 utf8 = new ReusableUtf8();
        String s = "日本語 and ascii " + "x".repeat(300);
        byte[] dest = new byte[4 + utf8.maxEncodedLength(s.length()) + 8];
        int n = utf8.encode(s, dest, 4);
        byte[] expected = s.getBytes(StandardCharsets.UTF_8);
        assertThat(n).isEqualTo(expected.length);
        byte[] slice = java.util.Arrays.copyOfRange(dest, 4, 4 + n);
        assertThat(slice).isEqualTo(expected);
        assertThat(dest[0]).isEqualTo((byte) 0);
        assertThat(dest[3]).isEqualTo((byte) 0);
    }

    @Test
    public void reuseDoesNotClobberCopiedBytes() {
        ReusableUtf8 utf8 = new ReusableUtf8();
        int n1 = utf8.encode("first-string");
        byte[] copy = java.util.Arrays.copyOf(utf8.buffer(), n1);
        utf8.encode("second");
        assertThat(copy).isEqualTo("first-string".getBytes(StandardCharsets.UTF_8));
        assertThat(utf8.decode(copy, 0, copy.length)).isEqualTo("first-string");
    }

    private static String printable(String s) {
        return s.length() > 40 ? s.substring(0, 20) + "..." : s;
    }
}
