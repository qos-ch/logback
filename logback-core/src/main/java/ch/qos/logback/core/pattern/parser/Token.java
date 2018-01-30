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
package ch.qos.logback.core.pattern.parser;

import java.util.List;

class Token {

    static final int PERCENT = 37;
    // static final int LEFT_PARENTHESIS = 40;
    static final int RIGHT_PARENTHESIS = 41;
    static final int MINUS = 45;
    static final int DOT = 46;
    static final int CURLY_LEFT = 123;
    static final int CURLY_RIGHT = 125;

    static final int LITERAL = 1000;
    static final int FORMAT_MODIFIER = 1002;
    static final int SIMPLE_KEYWORD = 1004;
    static final int COMPOSITE_KEYWORD = 1005;
    static final int OPTION = 1006;

    static final int EOF = Integer.MAX_VALUE;

    static Token EOF_TOKEN = new Token(EOF, "EOF");
    static Token RIGHT_PARENTHESIS_TOKEN = new Token(RIGHT_PARENTHESIS);
    static Token BARE_COMPOSITE_KEYWORD_TOKEN = new Token(COMPOSITE_KEYWORD, "BARE");
    static Token PERCENT_TOKEN = new Token(PERCENT);

    private final int type;
    private final String value;
    private final List<String> optionsList;

    public Token(int type) {
        this(type, null, null);
    }

    public Token(int type, String value) {
        this(type, value, null);
    }
    
    public Token(int type, List<String> optionsList) {
        this(type, null, optionsList);
    }
    
    public Token(int type, String value, List<String> optionsList) {
        this.type = type;
        this.value = value;
        this.optionsList = optionsList;
    }

    public int getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
    
    public List<String> getOptionsList() {
        return optionsList;
    }
    
    public String toString() {
        String typeStr = null;
        switch (type) {

        case PERCENT:
            typeStr = "%";
            break;
        case FORMAT_MODIFIER:
            typeStr = "FormatModifier";
            break;
        case LITERAL:
            typeStr = "LITERAL";
            break;
        case OPTION:
            typeStr = "OPTION";
            break;
        case SIMPLE_KEYWORD:
            typeStr = "SIMPLE_KEYWORD";
            break;
        case COMPOSITE_KEYWORD:
            typeStr = "COMPOSITE_KEYWORD";
            break;
        case RIGHT_PARENTHESIS:
            typeStr = "RIGHT_PARENTHESIS";
            break;
        default:
            typeStr = "UNKNOWN";
        }
        if (value == null) {
            return "Token(" + typeStr + ")";

        } else {
            return "Token(" + typeStr + ", \"" + value + "\")";
        }
    }

    public int hashCode() {
        int result;
        result = type;
        result = 29 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Token))
            return false;

        final Token token = (Token) o;

        if (type != token.type)
            return false;
        if (value != null ? !value.equals(token.value) : token.value != null)
            return false;

        return true;
    }
}
