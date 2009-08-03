/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.rolling.helper;

import java.util.ArrayList;
import java.util.List;

public class DatePatternToRegex {

  final String datePattern;
  final int length;

  DatePatternToRegex(String datePattern) {
    this.datePattern = datePattern;
    length = datePattern.length();
  }

  String toRegex() {
    List<SDFToken> tokenList = tokenize();
    StringBuilder sb = new StringBuilder();
    for(SDFToken token: tokenList) {
      sb.append(token.toRegex());
    }
    return sb.toString();
  }

  private List<SDFToken> tokenize() {
    List<SDFToken> tokenList = new ArrayList<SDFToken>();
    SDFToken token = null;
    for (int i = 0; i < length; i++) {
      char t = datePattern.charAt(i);
      if (token == null || token.c != t) {
        token = addNewToken(tokenList, t);
      } else {
        token.inc();
      }
    }
    return tokenList;
  }

  SDFToken addNewToken(List<SDFToken> tokenList, char t) {
    SDFToken token = new SDFToken(t);
    tokenList.add(token);
    return token;
  }
}
