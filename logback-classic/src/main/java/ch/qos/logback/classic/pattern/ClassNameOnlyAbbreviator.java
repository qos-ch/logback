/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import ch.qos.logback.core.CoreConstants;

/**
 * This abbreviator returns the class name from a fully qualified class name,
 * removing the leading package name.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class ClassNameOnlyAbbreviator implements Abbreviator {

  public String abbreviate(String fqClassName) {
    int lastIndex = fqClassName.lastIndexOf(CoreConstants.DOT);
    if (lastIndex != -1) {
      return fqClassName.substring(lastIndex + 1, fqClassName.length());
    } else {
      return fqClassName;
    }
  }
}
