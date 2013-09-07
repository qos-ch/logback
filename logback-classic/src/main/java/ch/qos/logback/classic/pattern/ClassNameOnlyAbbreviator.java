/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
    // we ignore the fact that the separator character can also be a dollar
    // If the inner class is org.good.AClass#Inner, returning
    // AClass#Inner seems most appropriate
    int lastIndex = fqClassName.lastIndexOf(CoreConstants.DOT);
    if (lastIndex != -1) {
      return fqClassName.substring(lastIndex + 1, fqClassName.length());
    } else {
      return fqClassName;
    }
  }
}
