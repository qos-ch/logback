/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.util;

/**
 * Various utility methods for processing strings representing context types.
 * 
 * @author Ceki Gulcu
 * 
 */
public class ContentTypeUtil {

  public static boolean isTextual(String contextType) {
    if (contextType == null) {
      return false;
    }
    return contextType.startsWith("text");
  }

  public static String getSubType(String contextType) {
    if (contextType == null) {
      return null;
    }
    int index = contextType.indexOf('/');
    if (index == -1) {
      return null;
    } else {
      int subTypeStartIndex = index + 1;
      if (subTypeStartIndex < contextType.length()) {
        return contextType.substring(subTypeStartIndex);
      } else {
        return null;
      }
    }
  }
}
