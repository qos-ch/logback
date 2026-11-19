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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A recursive-descent parser for JSON as defined by
 * <a href="https://datatracker.ietf.org/doc/html/rfc8259">RFC 8259</a>.
 *
 * <p>A JSON value becomes:</p>
 * <ul>
 * <li>an object, a {@link LinkedHashMap} keeping the order of its members; a member that appears
 * twice keeps its last value;</li>
 * <li>an array, a {@link List};</li>
 * <li>a string, a {@link String};</li>
 * <li>a number, a {@link JsonNumber} holding its text, so that the caller chooses its type;</li>
 * <li>{@code true} and {@code false}, {@link Boolean#TRUE} and {@link Boolean#FALSE};</li>
 * <li>{@code null}, {@code null}.</li>
 * </ul>
 *
 * <p>Errors are reported as an {@link IllegalArgumentException} whose message gives the offset, in
 * characters, at which the error was found.</p>
 */
class JsonParser {

    /**
     * Nesting deeper than this is rejected, so that a hostile input cannot exhaust the stack.
     */
    static final int MAX_DEPTH = 512;

    /**
     * A JSON number, kept as its text.
     */
    static final class JsonNumber {
        final String text;

        JsonNumber(String text) {
            this.text = text;
        }

        /**
         * @return true if the number has neither a fraction nor an exponent
         */
        boolean isIntegral() {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '.' || c == 'e' || c == 'E')
                    return false;
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof JsonNumber && ((JsonNumber) o).text.equals(text);
        }

        @Override
        public int hashCode() {
            return text.hashCode();
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private final String input;
    private final int length;
    private int pos;
    private int depth;

    private JsonParser(String input) {
        this.input = input;
        this.length = input.length();
    }

    /**
     * Parse one JSON value. Whitespace may surround it; anything else after it is an error.
     *
     * @param input the JSON text, not null
     * @return the value, as described in the class documentation
     * @throws IllegalArgumentException if the input is not valid JSON
     */
    static Object parse(String input) {
        JsonParser parser = new JsonParser(input);
        parser.skipWhitespace();
        Object value = parser.parseValue();
        parser.skipWhitespace();
        if (parser.pos != parser.length) {
            throw parser.error("unexpected character after the JSON value");
        }
        return value;
    }

    private Object parseValue() {
        if (pos >= length) {
            throw error("unexpected end of input, expected a value");
        }
        char c = input.charAt(pos);
        switch (c) {
        case '{':
            return parseObject();
        case '[':
            return parseArray();
        case '"':
            return parseString();
        case 't':
            expectLiteral("true");
            return Boolean.TRUE;
        case 'f':
            expectLiteral("false");
            return Boolean.FALSE;
        case 'n':
            expectLiteral("null");
            return null;
        default:
            if (c == '-' || (c >= '0' && c <= '9')) {
                return parseNumber();
            }
            throw error("unexpected character '" + printable(c) + "', expected a value");
        }
    }

    private Map<String, Object> parseObject() {
        enter();
        pos++; // '{'
        Map<String, Object> map = new LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            depth--;
            return map;
        }
        while (true) {
            skipWhitespace();
            if (peek() != '"') {
                throw error("expected a member name");
            }
            String name = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            Object value = parseValue();
            map.put(name, value);
            skipWhitespace();
            char c = peek();
            if (c == ',') {
                pos++;
            } else if (c == '}') {
                pos++;
                depth--;
                return map;
            } else {
                throw error("expected ',' or '}'");
            }
        }
    }

    private List<Object> parseArray() {
        enter();
        pos++; // '['
        List<Object> list = new ArrayList<>();
        skipWhitespace();
        if (peek() == ']') {
            pos++;
            depth--;
            return list;
        }
        while (true) {
            skipWhitespace();
            list.add(parseValue());
            skipWhitespace();
            char c = peek();
            if (c == ',') {
                pos++;
            } else if (c == ']') {
                pos++;
                depth--;
                return list;
            } else {
                throw error("expected ',' or ']'");
            }
        }
    }

    private String parseString() {
        pos++; // opening quote
        StringBuilder sb = null;
        int start = pos;
        while (true) {
            if (pos >= length) {
                throw error("unterminated string");
            }
            char c = input.charAt(pos);
            if (c == '"') {
                String result;
                if (sb == null) {
                    result = input.substring(start, pos);
                } else {
                    sb.append(input, start, pos);
                    result = sb.toString();
                }
                pos++;
                return result;
            }
            if (c < 0x20) {
                throw error("unescaped control character in string");
            }
            if (c == '\\') {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append(input, start, pos);
                pos++;
                sb.append(parseEscape());
                start = pos;
            } else {
                pos++;
            }
        }
    }

    private char parseEscape() {
        if (pos >= length) {
            throw error("unterminated escape sequence");
        }
        char c = input.charAt(pos++);
        switch (c) {
        case '"':
            return '"';
        case '\\':
            return '\\';
        case '/':
            return '/';
        case 'b':
            return '\b';
        case 'f':
            return '\f';
        case 'n':
            return '\n';
        case 'r':
            return '\r';
        case 't':
            return '\t';
        case 'u':
            return parseHex4();
        default:
            pos--;
            throw error("invalid escape character '" + printable(c) + "'");
        }
    }

    private char parseHex4() {
        if (pos + 4 > length) {
            throw error("incomplete \\u escape");
        }
        int value = 0;
        for (int i = 0; i < 4; i++) {
            char h = input.charAt(pos);
            int digit = (h <= 'f') ? Character.digit(h, 16) : -1;
            if (digit < 0) {
                throw error("invalid hexadecimal digit '" + printable(h) + "' in \\u escape");
            }
            value = (value << 4) | digit;
            pos++;
        }
        return (char) value;
    }

    private JsonNumber parseNumber() {
        int start = pos;
        if (peek() == '-') {
            pos++;
        }
        char c = peek();
        if (c == '0') {
            pos++;
        } else if (c >= '1' && c <= '9') {
            skipDigits();
        } else {
            throw error("expected a digit");
        }
        if (peek() == '.') {
            pos++;
            if (!isDigit(peek())) {
                throw error("expected a digit after the decimal point");
            }
            skipDigits();
        }
        c = peek();
        if (c == 'e' || c == 'E') {
            pos++;
            c = peek();
            if (c == '+' || c == '-') {
                pos++;
            }
            if (!isDigit(peek())) {
                throw error("expected a digit in the exponent");
            }
            skipDigits();
        }
        return new JsonNumber(input.substring(start, pos));
    }

    private void skipDigits() {
        while (isDigit(peek())) {
            pos++;
        }
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void expectLiteral(String literal) {
        if (!input.startsWith(literal, pos)) {
            throw error("invalid literal, expected '" + literal + "'");
        }
        pos += literal.length();
    }

    private void expect(char expected) {
        if (peek() != expected) {
            throw error("expected '" + expected + "'");
        }
        pos++;
    }

    /**
     * @return the current character, or 0 at the end of the input
     */
    private char peek() {
        return pos < length ? input.charAt(pos) : 0;
    }

    private void skipWhitespace() {
        while (pos < length) {
            char c = input.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                pos++;
            } else {
                return;
            }
        }
    }

    private void enter() {
        if (++depth > MAX_DEPTH) {
            throw error("nesting deeper than " + MAX_DEPTH);
        }
    }

    private IllegalArgumentException error(String message) {
        String where = pos >= length ? "at end of input (offset " + pos + ")" : "at offset " + pos;
        return new IllegalArgumentException("Invalid JSON " + where + ": " + message);
    }

    private static String printable(char c) {
        if (c < 0x20 || c > 0x7E) {
            return String.format("\\u%04X", (int) c);
        }
        return String.valueOf(c);
    }
}
