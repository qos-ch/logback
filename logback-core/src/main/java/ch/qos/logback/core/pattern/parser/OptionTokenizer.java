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

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.pattern.util.AsIsEscapeUtil;
import ch.qos.logback.core.pattern.util.IEscapeUtil;

import static ch.qos.logback.core.CoreConstants.CURLY_RIGHT;

import static ch.qos.logback.core.CoreConstants.ESCAPE_CHAR;
import static ch.qos.logback.core.CoreConstants.COMMA_CHAR;
import static ch.qos.logback.core.CoreConstants.SINGLE_QUOTE_CHAR;
import static ch.qos.logback.core.CoreConstants.DOUBLE_QUOTE_CHAR;

import ch.qos.logback.core.pattern.parser.TokenStream.TokenizerState;
import ch.qos.logback.core.spi.ScanException;

public class OptionTokenizer {

    private final static int EXPECTING_STATE = 0;
    private final static int RAW_COLLECTING_STATE = 1;
    private final static int QUOTED_COLLECTING_STATE = 2;

    final IEscapeUtil escapeUtil;
    final TokenStream tokenStream;
    final String pattern;
    final int patternLength;

    char quoteChar;
    int state = EXPECTING_STATE;

    OptionTokenizer(TokenStream tokenStream) {
        this(tokenStream, new AsIsEscapeUtil());
    }

    OptionTokenizer(TokenStream tokenStream, IEscapeUtil escapeUtil) {
        this.tokenStream = tokenStream;
        this.pattern = tokenStream.pattern;
        this.patternLength = tokenStream.patternLength;
        this.escapeUtil = escapeUtil;
    }

    void tokenize(char firstChar, List<Token> tokenList) throws ScanException {
        StringBuffer buf = new StringBuffer();
        List<String> optionList = new ArrayList<String>();
        char c = firstChar;

        while (tokenStream.pointer < patternLength) {
            switch (state) {
            case EXPECTING_STATE:
                switch (c) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                case COMMA_CHAR:
                    break;
                case SINGLE_QUOTE_CHAR:
                case DOUBLE_QUOTE_CHAR:
                    state = QUOTED_COLLECTING_STATE;
                    quoteChar = c;
                    break;
                case CURLY_RIGHT:
                    emitOptionToken(tokenList, optionList);
                    return;
                default:
                    buf.append(c);
                    state = RAW_COLLECTING_STATE;
                }
                break;
            case RAW_COLLECTING_STATE:
                switch (c) {
                case COMMA_CHAR:
                    optionList.add(buf.toString().trim());
                    buf.setLength(0);
                    state = EXPECTING_STATE;
                    break;
                case CURLY_RIGHT:
                    optionList.add(buf.toString().trim());
                    emitOptionToken(tokenList, optionList);
                    return;
                default:
                    buf.append(c);
                }
                break;
            case QUOTED_COLLECTING_STATE:
                if (c == quoteChar) {
                    optionList.add(buf.toString());
                    buf.setLength(0);
                    state = EXPECTING_STATE;
                } else if (c == ESCAPE_CHAR) {
                    escape(String.valueOf(quoteChar), buf);
                } else {
                    buf.append(c);
                }

                break;
            }

            c = pattern.charAt(tokenStream.pointer);
            tokenStream.pointer++;
        }

        // EOS
        if (c == CURLY_RIGHT) {
            if (state == EXPECTING_STATE) {
                emitOptionToken(tokenList, optionList);
            } else if (state == RAW_COLLECTING_STATE) {
                optionList.add(buf.toString().trim());
                emitOptionToken(tokenList, optionList);
            } else {
                throw new ScanException("Unexpected end of pattern string in OptionTokenizer");
            }
        } else {
            throw new ScanException("Unexpected end of pattern string in OptionTokenizer");
        }
    }

    void emitOptionToken(List<Token> tokenList, List<String> optionList) {
        tokenList.add(new Token(Token.OPTION, optionList));
        tokenStream.state = TokenizerState.LITERAL_STATE;
    }

    void escape(String escapeChars, StringBuffer buf) {
        if ((tokenStream.pointer < patternLength)) {
            char next = pattern.charAt(tokenStream.pointer++);
            escapeUtil.escape(escapeChars, buf, next, tokenStream.pointer);
        }
    }
}
