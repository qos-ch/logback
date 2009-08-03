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

  public String toRegex(String datePattern) {
    List<SDFToken> tokenList = tokenize();
    for(SDFToken token: tokenList) {
      
    }
    char c = 0;
    for (int i = 0; i < length; i++) {
      char t = 1;
    }
    return null;
  }

  List<SDFToken> tokenize() {
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
