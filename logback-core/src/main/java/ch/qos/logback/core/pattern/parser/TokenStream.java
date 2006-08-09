/** 
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2005, QOS.ch, LOGBack.com
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.pattern.parser;

import java.util.List;
import java.util.ArrayList;

import ch.qos.logback.core.pattern.util.EscapeUtil;

/**
 * <p>Return a steady stream of tokens. <p/>
 * 
 * <p>The returned tokens are one of:
 * LITERAL, '%', FORMAT_MODIFIER, KEYWWORD, OPTION, LEFT_PARENTHESIS, and
 * RIGHT_PARENTHESIS.</p>
 * 
 * <p>The '\' character is used as escape. It can be used to escape '_', '%', '(' and 
 * '('.<p>
 * 
 * <p>Note that there is no EOS token returned.</p>
 */
class TokenStream {

  private static final char ESCAPE_CHAR = '\\';
  private static final char PERCENT_CHAR = '%';
  private static final char LEFT_PARENTHESIS = '(';
  private static final char RIGHT_PARENTHESIS = ')';
  private static final char CURLY_LEFT = '{';
  private static final char CURLY_RIGHT = '}';

  private static final int LITERAL_STATE = 0;
  private static final int FORMAT_MODIFIER_STATE = 1;
  private static final int KEYWORD_STATE = 2;
  private static final int OPTION_STATE = 3;

  final String pattern;
  final int patternLength;
  int state = LITERAL_STATE;
  int pointer = 0;

  TokenStream(String pattern) {
    if(pattern == null) {
      throw new NullPointerException("null pattern string not allowed");
    }
    this.pattern = pattern;
    patternLength = pattern.length();
  }

  List tokenize() throws ScanException {
    List<Token> tokenList = new ArrayList<Token>();
    StringBuffer buf = new StringBuffer();

    while (pointer < patternLength) {
      char c = pattern.charAt(pointer);
      pointer++;

      switch (state) {

      case LITERAL_STATE:
        switch (c) {
        case ESCAPE_CHAR:
          escape("%()", buf);
          break;
        case PERCENT_CHAR:
          addValuedToken(Token.LITERAL, buf, tokenList);
          tokenList.add(Token.PERCENT_TOKEN);
          state = FORMAT_MODIFIER_STATE;
          break;

        case RIGHT_PARENTHESIS:
          if (buf.length() >= 1 && buf.charAt(buf.length() - 1) == '\\') {
            buf.deleteCharAt(buf.length() - 1);
            buf.append(RIGHT_PARENTHESIS);
          } else {
            addValuedToken(Token.LITERAL, buf, tokenList);
            tokenList.add(Token.RIGHT_PARENTHESIS_TOKEN);
          }
          break;

        default:
          buf.append(c);
        }
        break;
      //
      case FORMAT_MODIFIER_STATE:
        if (c == LEFT_PARENTHESIS) {
          addValuedToken(Token.FORMAT_MODIFIER, buf, tokenList);
          tokenList.add(Token.LEFT_PARENTHESIS_TOKEN);
          state = LITERAL_STATE;
        } else if (Character.isJavaIdentifierStart(c)) {
          addValuedToken(Token.FORMAT_MODIFIER, buf, tokenList);
          state = KEYWORD_STATE;
          buf.append(c);
        } else {
          buf.append(c);
        }
        break;
      case OPTION_STATE:
        switch (c) {
        case CURLY_RIGHT:
          addValuedToken(Token.OPTION, buf, tokenList);
          state = LITERAL_STATE;
          break;
        case ESCAPE_CHAR:
          escape("%{}", buf);
          break;
        default:
          buf.append(c);
        }
        break;
      case KEYWORD_STATE:
        if (c == CURLY_LEFT) {
          addValuedToken(Token.KEYWORD, buf, tokenList);
          state = OPTION_STATE;
        } else if (Character.isJavaIdentifierPart(c)) {
          buf.append(c);
        } else if (c == PERCENT_CHAR) {
          addValuedToken(Token.KEYWORD, buf, tokenList);
          tokenList.add(Token.PERCENT_TOKEN);
          state = FORMAT_MODIFIER_STATE;
        } else {
          addValuedToken(Token.KEYWORD, buf, tokenList);
          if (c == RIGHT_PARENTHESIS) {
            // if c is a right parenthesis, then add it as such
            tokenList.add(Token.RIGHT_PARENTHESIS_TOKEN);
          } else if (c == ESCAPE_CHAR) {
            if ((pointer < patternLength)) {
              char next = pattern.charAt(pointer++);
              EscapeUtil.escape("%()", buf, next, pointer);
            }
          } else {
            buf.append(c);
          }
          state = LITERAL_STATE;
        }
        break;

      default:
      }
    }

    // EOS
    switch (state) {
    case LITERAL_STATE:
      addValuedToken(Token.LITERAL, buf, tokenList);
      break;
    case KEYWORD_STATE:
      tokenList.add(new Token(Token.KEYWORD, buf.toString()));
      buf.setLength(0);
      break;

    case FORMAT_MODIFIER_STATE:
    case OPTION_STATE:
      throw new ScanException("Unexpected end of pattern string");
    }

    return tokenList;
  }

  void escape(String escapeChars, StringBuffer buf) {
    if ((pointer < patternLength)) {
      char next = pattern.charAt(pointer++);
      EscapeUtil.escape(escapeChars, buf, next, pointer);
    }
  }

  private void addValuedToken(int type, StringBuffer buf, List<Token> tokenList) {
    if (buf.length() > 0) {
      tokenList.add(new Token(type, buf.toString()));
      buf.setLength(0);
    }
  }
}