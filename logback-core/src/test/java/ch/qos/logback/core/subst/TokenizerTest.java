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
package ch.qos.logback.core.subst;

import ch.qos.logback.core.spi.ScanException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TokenizerTest {

    List<Token> witnessList = new ArrayList<Token>();

    @Test
    public void literalOnly() throws ScanException {
        String input = "abc";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokenList = tokenizer.tokenize();
        witnessList.add(new Token(Token.Type.LITERAL, input));
        assertEquals(witnessList, tokenList);
    }

    @Test
    public void literalWithAccolades() throws ScanException {
        String input0 = "%logger";
        String input1 = "24";
        String input2 = " - %m";
        String input = input0 + "{" + input1 + "}" + input2;
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokenList = tokenizer.tokenize();
        witnessList.add(new Token(Token.Type.LITERAL, input0));
        witnessList.add(Token.CURLY_LEFT_TOKEN);
        witnessList.add(new Token(Token.Type.LITERAL, input1));

        witnessList.add(Token.CURLY_RIGHT_TOKEN);
        witnessList.add(new Token(Token.Type.LITERAL, input2));
        assertEquals(witnessList, tokenList);
    }

    @Test
    public void simleVariable() throws ScanException {
        String input = "${abc}";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokenList = tokenizer.tokenize();
        witnessList.add(Token.START_TOKEN);
        witnessList.add(new Token(Token.Type.LITERAL, "abc"));
        witnessList.add(Token.CURLY_RIGHT_TOKEN);
        assertEquals(witnessList, tokenList);
    }

    @Test
    public void mix() throws ScanException {
        String input = "a${b}c";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokenList = tokenizer.tokenize();
        witnessList.add(new Token(Token.Type.LITERAL, "a"));
        witnessList.add(Token.START_TOKEN);
        witnessList.add(new Token(Token.Type.LITERAL, "b"));
        witnessList.add(Token.CURLY_RIGHT_TOKEN);
        witnessList.add(new Token(Token.Type.LITERAL, "c"));
        assertEquals(witnessList, tokenList);
    }

    @Test
    public void nested() throws ScanException {
        String input = "a${b${c}}";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokenList = tokenizer.tokenize();
        witnessList.add(new Token(Token.Type.LITERAL, "a"));
        witnessList.add(Token.START_TOKEN);
        witnessList.add(new Token(Token.Type.LITERAL, "b"));
        witnessList.add(Token.START_TOKEN);
        witnessList.add(new Token(Token.Type.LITERAL, "c"));
        witnessList.add(Token.CURLY_RIGHT_TOKEN);
        witnessList.add(Token.CURLY_RIGHT_TOKEN);
        assertEquals(witnessList, tokenList);
    }

    @Test
    public void basicDefaultSeparator() throws ScanException {
        String input = "${a:-b}";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokenList = tokenizer.tokenize();
        witnessList.add(Token.START_TOKEN);
        witnessList.add(new Token(Token.Type.LITERAL, "a"));
        witnessList.add(Token.DEFAULT_SEP_TOKEN);
        witnessList.add(new Token(Token.Type.LITERAL, "b"));
        witnessList.add(Token.CURLY_RIGHT_TOKEN);
        assertEquals(witnessList, tokenList);
    }

    @Test
    public void colon() throws ScanException {
        String input = "a:b";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokenList = tokenizer.tokenize();
        witnessList.add(new Token(Token.Type.LITERAL, "a"));
        witnessList.add(new Token(Token.Type.LITERAL, ":b"));
        assertEquals(witnessList, tokenList);
    }

    // /LOGBACK-744
    @Test
    public void colonFollowedByDollar() throws ScanException {
        String input = "a:${b}";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokenList = tokenizer.tokenize();
        witnessList.add(new Token(Token.Type.LITERAL, "a"));
        witnessList.add(new Token(Token.Type.LITERAL, ":"));
        witnessList.add(Token.START_TOKEN);
        witnessList.add(new Token(Token.Type.LITERAL, "b"));
        witnessList.add(Token.CURLY_RIGHT_TOKEN);
        assertEquals(witnessList, tokenList);
        
    }

    @Test
    public void defaultSeparatorOutsideVariable() throws ScanException {

        String input = "{a:-b}";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokenList = tokenizer.tokenize();
        witnessList.add(Token.CURLY_LEFT_TOKEN);
        witnessList.add(new Token(Token.Type.LITERAL, "a"));
        witnessList.add(Token.DEFAULT_SEP_TOKEN);
        witnessList.add(new Token(Token.Type.LITERAL, "b"));
        witnessList.add(Token.CURLY_RIGHT_TOKEN);
        assertEquals(witnessList, tokenList);
    }

    @Test
    public void literalContainingColon() throws ScanException {
        String input = "a:b";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokenList = tokenizer.tokenize();
        witnessList.add(new Token(Token.Type.LITERAL, "a"));
        witnessList.add(new Token(Token.Type.LITERAL, ":b"));
        assertEquals(witnessList, tokenList);
    }

    @Test
    public void literalEndingWithColon_LOGBACK_1140() throws ScanException {
        String input = "a:";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokenList = tokenizer.tokenize();
        witnessList.add(new Token(Token.Type.LITERAL, "a"));
        witnessList.add(new Token(Token.Type.LITERAL, ":"));
        assertEquals(witnessList, tokenList);
    }
    
    @Test
    public void literalEndingWithDollar_LOGBACK_1149() throws ScanException {
        String input = "a$";
        Tokenizer tokenizer = new Tokenizer(input);
        List<Token> tokenList = tokenizer.tokenize();
        witnessList.add(new Token(Token.Type.LITERAL, "a"));
        witnessList.add(new Token(Token.Type.LITERAL, "$"));
        assertEquals(witnessList, tokenList);
    }
}
