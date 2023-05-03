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

public class JsonEscapeUtil {

    protected final static char[] HEXADECIMALS_TABLE = "0123456789ABCDEF".toCharArray();

    static final int ESCAPE_CODES_COUNT = 32;

    static final String[] ESCAPE_CODES = new String[ESCAPE_CODES_COUNT];

    // From RFC-8259 page 5

    //  %x22 /          ; "    quotation mark  U+0022
    //  %x5C /          ; \    reverse solidus U+005C
    //  %x2F /          ; /    solidus         U+002F

    //  %x62 /          ; b    backspace       U+0008
    //  %x74 /          ; t    tab             U+0009
    //  %x6E /          ; n    line feed       U+000A
    //  %x66 /          ; f    form feed       U+000C
    //  %x72 /          ; r    carriage return U+000D

    static {
        for (char c = 0; c < ESCAPE_CODES_COUNT; c++) {

            switch (c) {
            case 0x08:
                ESCAPE_CODES[c] = "\\b";
                break;
            case 0x09:
                ESCAPE_CODES[c] = "\\t";
                break;
            case 0x0A:
                ESCAPE_CODES[c] = "\\n";
                break;
            case 0x0C:
                ESCAPE_CODES[c] = "\\f";
                break;
            case 0x0D:
                ESCAPE_CODES[c] = "\\r";
                break;
            default:
                ESCAPE_CODES[c] = _computeEscapeCodeBelowASCII32(c);
            }
        }
    }

    // this method should not be called by methods except the static initializer
    private static String _computeEscapeCodeBelowASCII32(char c) {
        if (c > 32) {
            throw new IllegalArgumentException("input must be less than 32");
        }

        StringBuilder sb = new StringBuilder(6);
        sb.append("\\u00");

        int highPart = c >> 4;
        sb.append(HEXADECIMALS_TABLE[highPart]);

        int lowPart = c & 0x0F;
        sb.append(HEXADECIMALS_TABLE[lowPart]);

        return sb.toString();
    }

    //  %x22 /          ; "    quotation mark  U+0022
    //  %x5C /          ; \    reverse solidus U+005C

    static String getObligatoryEscapeCode(char c) {
        if (c < 32)
            return ESCAPE_CODES[c];
        if (c == 0x22)
            return "\\\"";
        if (c == 0x5C)
            return "\\/";

        return null;
    }

    static public String jsonEscapeString(String input) {
        int length = input.length();
        int lenthWithLeeway = (int) (length * 1.1);

        StringBuilder sb = new StringBuilder(lenthWithLeeway);
        for (int i = 0; i < length; i++) {
            final char c = input.charAt(i);
            String escaped = getObligatoryEscapeCode(c);
            if (escaped == null)
                sb.append(c);
            else {
                sb.append(escaped);
            }
        }

        return sb.toString();
    }

}
