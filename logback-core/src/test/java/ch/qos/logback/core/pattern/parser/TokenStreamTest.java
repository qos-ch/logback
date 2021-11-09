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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.qos.logback.core.pattern.util.AlmostAsIsEscapeUtil;
import ch.qos.logback.core.spi.ScanException;

public class TokenStreamTest {

    @Test
    public void testEmpty() throws ScanException {
        try {
            new TokenStream("").tokenize();
            fail("empty string not allowed");
        } catch (final IllegalArgumentException e) {
        }
    }

    @Test
    public void testSingleLiteral() throws ScanException {
        final List<Token> tl = new TokenStream("hello").tokenize();
        final List<Token> witness = new ArrayList<>();
        witness.add(new Token(Token.LITERAL, "hello"));
        assertEquals(witness, tl);
    }

    @Test
    public void testLiteralWithPercent() throws ScanException {
        {
            final List<Token> tl = new TokenStream("hello\\%world").tokenize();

            final List<Token> witness = new ArrayList<>();
            witness.add(new Token(Token.LITERAL, "hello%world"));
            assertEquals(witness, tl);
        }
        {
            final List<Token> tl = new TokenStream("hello\\%").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(new Token(Token.LITERAL, "hello%"));
            assertEquals(witness, tl);
        }

        {
            final List<Token> tl = new TokenStream("\\%").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(new Token(Token.LITERAL, "%"));
            assertEquals(witness, tl);
        }
    }

    @Test
    public void testBasic() throws ScanException {

        // test "%c"
        {
            final List<Token> tl = new TokenStream("%c").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "c"));
            assertEquals(witness, tl);
        }

        {
            // test "xyz%-34c"
            final List<Token> tl = new TokenStream("%a%b").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "a"));
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "b"));
            assertEquals(witness, tl);
        }

        {
            // test "xyz%-34c"
            final List<Token> tl = new TokenStream("xyz%-34c").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(new Token(Token.LITERAL, "xyz"));
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.FORMAT_MODIFIER, "-34"));
            witness.add(new Token(Token.SIMPLE_KEYWORD, "c"));
            assertEquals(witness, tl);
        }
    }

    @Test
    public void testComplexNR() throws ScanException {
        final List<Token> tl = new TokenStream("%d{1234} [%34.-67toto] %n").tokenize();
        final List<Token> witness = new ArrayList<>();
        witness.add(Token.PERCENT_TOKEN);
        witness.add(new Token(Token.SIMPLE_KEYWORD, "d"));
        final List<String> ol = new ArrayList<>();
        ol.add("1234");
        witness.add(new Token(Token.OPTION, ol));
        witness.add(new Token(Token.LITERAL, " ["));
        witness.add(Token.PERCENT_TOKEN);
        witness.add(new Token(Token.FORMAT_MODIFIER, "34.-67"));
        witness.add(new Token(Token.SIMPLE_KEYWORD, "toto"));
        witness.add(new Token(Token.LITERAL, "] "));
        witness.add(Token.PERCENT_TOKEN);
        witness.add(new Token(Token.SIMPLE_KEYWORD, "n"));
        assertEquals(witness, tl);
    }

    @Test
    public void testEmptyP() throws ScanException {
        final List<Token> tl = new TokenStream("()").tokenize();
        final List<Token> witness = new ArrayList<>();
        witness.add(new Token(Token.LITERAL, "("));
        witness.add(Token.RIGHT_PARENTHESIS_TOKEN);
        assertEquals(witness, tl);
    }

    @Test
    public void testEmptyP2() throws ScanException {
        final List<Token> tl = new TokenStream("%()").tokenize();
        final List<Token> witness = new ArrayList<>();
        witness.add(Token.PERCENT_TOKEN);
        witness.add(Token.BARE_COMPOSITE_KEYWORD_TOKEN);
        witness.add(Token.RIGHT_PARENTHESIS_TOKEN);
        assertEquals(witness, tl);
    }

    @Test
    public void testEscape() throws ScanException {
        {
            final List<Token> tl = new TokenStream("\\%").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(new Token(Token.LITERAL, "%"));
            assertEquals(witness, tl);
        }

        {
            final List<Token> tl = new TokenStream("\\%\\(\\t\\)\\r\\n").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(new Token(Token.LITERAL, "%(\t)\r\n"));
            assertEquals(witness, tl);
        }

        {
            final List<Token> tl = new TokenStream("\\\\%x").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(new Token(Token.LITERAL, "\\"));
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "x"));
            assertEquals(witness, tl);
        }

        {
            final List<Token> tl = new TokenStream("%x\\)").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "x"));
            witness.add(new Token(Token.LITERAL, ")"));
            assertEquals(witness, tl);
        }

        {
            final List<Token> tl = new TokenStream("%x\\_a").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "x"));
            witness.add(new Token(Token.LITERAL, "a"));
            assertEquals(witness, tl);
        }
        {
            final List<Token> tl = new TokenStream("%x\\_%b").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "x"));
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "b"));
            assertEquals(witness, tl);
        }
    }

    @Test
    public void testOptions() throws ScanException {
        {
            final List<Token> tl = new TokenStream("%x{t}").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "x"));
            final List<String> ol = new ArrayList<>();
            ol.add("t");
            witness.add(new Token(Token.OPTION, ol));
            assertEquals(witness, tl);
        }

        {
            final List<Token> tl = new TokenStream("%x{t,y}").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "x"));
            final List<String> ol = new ArrayList<>();
            ol.add("t");
            ol.add("y");
            witness.add(new Token(Token.OPTION, ol));
            assertEquals(witness, tl);
        }

        {
            final List<Token> tl = new TokenStream("%x{\"hello world.\", \"12y  \"}").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "x"));
            final List<String> ol = new ArrayList<>();
            ol.add("hello world.");
            ol.add("12y  ");
            witness.add(new Token(Token.OPTION, ol));
            assertEquals(witness, tl);
        }

        {
            final List<Token> tl = new TokenStream("%x{'opt}'}").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "x"));
            final List<String> ol = new ArrayList<>();
            ol.add("opt}");
            witness.add(new Token(Token.OPTION, ol));
            assertEquals(witness, tl);
        }
    }

    @Test
    public void testSimpleP() throws ScanException {
        final List<Token> tl = new TokenStream("%(hello %class{.4?})").tokenize();
        final List<Token> witness = new ArrayList<>();
        witness.add(Token.PERCENT_TOKEN);
        witness.add(Token.BARE_COMPOSITE_KEYWORD_TOKEN);
        witness.add(new Token(Token.LITERAL, "hello "));
        witness.add(Token.PERCENT_TOKEN);
        witness.add(new Token(Token.SIMPLE_KEYWORD, "class"));
        final List<String> ol = new ArrayList<>();
        ol.add(".4?");
        witness.add(new Token(Token.OPTION, ol));
        witness.add(Token.RIGHT_PARENTHESIS_TOKEN);
        assertEquals(witness, tl);
    }

    @Test
    public void testSimpleP2() throws ScanException {
        final List<Token> tl = new TokenStream("X %a %-12.550(hello %class{.4?})").tokenize();
        final List<Token> witness = new ArrayList<>();
        witness.add(new Token(Token.LITERAL, "X "));
        witness.add(Token.PERCENT_TOKEN);
        witness.add(new Token(Token.SIMPLE_KEYWORD, "a"));
        witness.add(new Token(Token.LITERAL, " "));
        witness.add(Token.PERCENT_TOKEN);
        witness.add(new Token(Token.FORMAT_MODIFIER, "-12.550"));
        witness.add(Token.BARE_COMPOSITE_KEYWORD_TOKEN);
        witness.add(new Token(Token.LITERAL, "hello "));
        witness.add(Token.PERCENT_TOKEN);
        witness.add(new Token(Token.SIMPLE_KEYWORD, "class"));
        final List<String> ol = new ArrayList<>();
        ol.add(".4?");
        witness.add(new Token(Token.OPTION, ol));
        witness.add(Token.RIGHT_PARENTHESIS_TOKEN);
        assertEquals(witness, tl);
    }

    @Test
    public void testMultipleRecursion() throws ScanException {
        final List<Token> tl = new TokenStream("%-1(%d %45(%class %file))").tokenize();
        final List<Token> witness = new ArrayList<>();
        witness.add(Token.PERCENT_TOKEN);
        witness.add(new Token(Token.FORMAT_MODIFIER, "-1"));
        witness.add(Token.BARE_COMPOSITE_KEYWORD_TOKEN);
        witness.add(Token.PERCENT_TOKEN);
        witness.add(new Token(Token.SIMPLE_KEYWORD, "d"));
        witness.add(new Token(Token.LITERAL, " "));
        witness.add(Token.PERCENT_TOKEN);
        witness.add(new Token(Token.FORMAT_MODIFIER, "45"));
        witness.add(Token.BARE_COMPOSITE_KEYWORD_TOKEN);
        witness.add(Token.PERCENT_TOKEN);
        witness.add(new Token(Token.SIMPLE_KEYWORD, "class"));
        witness.add(new Token(Token.LITERAL, " "));
        witness.add(Token.PERCENT_TOKEN);
        witness.add(new Token(Token.SIMPLE_KEYWORD, "file"));
        witness.add(Token.RIGHT_PARENTHESIS_TOKEN);
        witness.add(Token.RIGHT_PARENTHESIS_TOKEN);

        assertEquals(witness, tl);
    }

    @Test
    public void testNested() throws ScanException {
        final List<Token> tl = new TokenStream("%(%a%(%b))").tokenize();
        final List<Token> witness = new ArrayList<>();
        witness.add(Token.PERCENT_TOKEN);
        witness.add(Token.BARE_COMPOSITE_KEYWORD_TOKEN);
        witness.add(Token.PERCENT_TOKEN);
        witness.add(new Token(Token.SIMPLE_KEYWORD, "a"));
        witness.add(Token.PERCENT_TOKEN);
        witness.add(Token.BARE_COMPOSITE_KEYWORD_TOKEN);
        witness.add(Token.PERCENT_TOKEN);
        witness.add(new Token(Token.SIMPLE_KEYWORD, "b"));
        witness.add(Token.RIGHT_PARENTHESIS_TOKEN);
        witness.add(Token.RIGHT_PARENTHESIS_TOKEN);

        assertEquals(witness, tl);

    }

    @Test
    public void testEscapedParanteheses() throws ScanException {
        {
            final List<Token> tl = new TokenStream("\\(%h\\)").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(new Token(Token.LITERAL, "("));
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "h"));
            witness.add(new Token(Token.LITERAL, ")"));
            assertEquals(witness, tl);
        }
        {
            final List<Token> tl = new TokenStream("(%h\\)").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(new Token(Token.LITERAL, "("));
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "h"));
            witness.add(new Token(Token.LITERAL, ")"));
            assertEquals(witness, tl);
        }
        {
            final List<Token> tl = new TokenStream("%a(x\\)").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.COMPOSITE_KEYWORD, "a"));
            witness.add(new Token(Token.LITERAL, "x)"));
            assertEquals(witness, tl);
        }
        {
            final List<Token> tl = new TokenStream("%a\\(x)").tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "a"));
            witness.add(new Token(Token.LITERAL, "(x"));
            witness.add(new Token(Token.RIGHT_PARENTHESIS));

            assertEquals(witness, tl);
        }
    }

    @Test
    public void testWindowsLikeBackSlashes() throws ScanException {
        final List<Token> tl = new TokenStream("c:\\hello\\world.%i", new AlmostAsIsEscapeUtil()).tokenize();

        final List<Token> witness = new ArrayList<>();
        witness.add(new Token(Token.LITERAL, "c:\\hello\\world."));
        witness.add(Token.PERCENT_TOKEN);
        witness.add(new Token(Token.SIMPLE_KEYWORD, "i"));
        assertEquals(witness, tl);
    }

    @Test
    public void compositedKeyword() throws ScanException {
        {
            final List<Token> tl = new TokenStream("%d(A)", new AlmostAsIsEscapeUtil()).tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.COMPOSITE_KEYWORD, "d"));
            witness.add(new Token(Token.LITERAL, "A"));
            witness.add(Token.RIGHT_PARENTHESIS_TOKEN);
            assertEquals(witness, tl);
        }
        {
            final List<Token> tl = new TokenStream("a %subst(%b C)", new AlmostAsIsEscapeUtil()).tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(new Token(Token.LITERAL, "a "));
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.COMPOSITE_KEYWORD, "subst"));
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.SIMPLE_KEYWORD, "b"));
            witness.add(new Token(Token.LITERAL, " C"));
            witness.add(Token.RIGHT_PARENTHESIS_TOKEN);
            assertEquals(witness, tl);
        }
    }

    @Test
    public void compositedKeywordFollowedByOptions() throws ScanException {
        {
            final List<Token> tl = new TokenStream("%d(A){o}", new AlmostAsIsEscapeUtil()).tokenize();
            final List<Token> witness = new ArrayList<>();
            witness.add(Token.PERCENT_TOKEN);
            witness.add(new Token(Token.COMPOSITE_KEYWORD, "d"));
            witness.add(new Token(Token.LITERAL, "A"));
            witness.add(Token.RIGHT_PARENTHESIS_TOKEN);
            final List<String> ol = new ArrayList<>();
            ol.add("o");
            witness.add(new Token(Token.OPTION, ol));

            assertEquals(witness, tl);
        }
    }
}