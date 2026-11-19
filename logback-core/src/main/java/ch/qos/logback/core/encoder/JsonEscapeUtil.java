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

package ch.qos.logback.core.encoder;

public class JsonEscapeUtil {

    protected final static char[] HEXADECIMALS_TABLE = "0123456789ABCDEF".toCharArray();

    static final int ESCAPE_CODES_COUNT = 32;

    static final String[] ESCAPE_CODES = new String[ESCAPE_CODES_COUNT];

    // From RFC-8259 page 5

    //  "    quotation mark  U+0022 -- escaped as \"
    //  \    reverse solidus U+005C -- escaped as \\
    //  /    solidus         U+002F -- escaped as \/

    //  backspace       U+0008  -- escaped as \b
    //  tab             U+0009  -- escaped as \t
    //  line feed       U+000A  -- escaped as \n
    //  form feed       U+000C -- escaped as \f
    //  carriage return U+000D -- escaped as \r

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

    //  "    quotation mark  U+0022 -- escaped as \"
    //  \    reverse solidus U+005C -- escaped as \\
    //  /    solidus         U+002F -- escaped as \/

    static String getObligatoryEscapeCode(char c) {
        if (c < 32)
            return ESCAPE_CODES[c];
        if (c == 0x22)
            return "\\\"";
        if (c == 0x2F)
            return "\\/";
        if (c == 0x5C)
            return "\\\\";

        return null;
    }

    /**
     * Escape the input so that it can be placed between double quotes in a JSON document.
     *
     * <p>Besides the escapes required by RFC 8259, a lone surrogate, that is a high surrogate not
     * followed by a low surrogate or a low surrogate not preceded by a high surrogate, is written
     * as a <code>&#92;uXXXX</code> escape. Encoding it as UTF-8 would otherwise replace it with '?'.</p>
     *
     * @param input the string to escape, not null
     * @return the escaped string
     */
    static public String jsonEscapeString(String input) {
        int length = input.length();
        int lenthWithLeeway = (int) (length * 1.1);

        StringBuilder sb = new StringBuilder(lenthWithLeeway);
        appendJsonEscapedString(sb, input);
        return sb.toString();
    }

    /**
     * Append the escaped form of the input to the given {@link StringBuilder}, as
     * {@link #jsonEscapeString(String)} computes it, without the surrounding double quotes.
     *
     * @param sb the builder to append to, not null
     * @param input the string to escape, not null
     * @since 1.6.8
     */
    static public void appendJsonEscapedString(StringBuilder sb, String input) {
        final int length = input.length();
        for (int i = 0; i < length; i++) {
            final char c = input.charAt(i);
            String escaped = getObligatoryEscapeCode(c);
            if (escaped != null) {
                sb.append(escaped);
            } else if (Character.isSurrogate(c)) {
                if (Character.isHighSurrogate(c) && i + 1 < length && Character.isLowSurrogate(input.charAt(i + 1))) {
                    sb.append(c).append(input.charAt(i + 1));
                    i++;
                } else {
                    appendUnicodeEscape(sb, c);
                }
            } else {
                sb.append(c);
            }
        }
    }

    private static void appendUnicodeEscape(StringBuilder sb, char c) {
        sb.append("\\u");
        sb.append(HEXADECIMALS_TABLE[(c >> 12) & 0x0F]);
        sb.append(HEXADECIMALS_TABLE[(c >> 8) & 0x0F]);
        sb.append(HEXADECIMALS_TABLE[(c >> 4) & 0x0F]);
        sb.append(HEXADECIMALS_TABLE[c & 0x0F]);
    }

}
