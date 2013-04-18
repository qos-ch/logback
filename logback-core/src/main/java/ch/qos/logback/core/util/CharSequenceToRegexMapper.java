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
package ch.qos.logback.core.util;

/**
 * This class supports mapping character sequences to
 * regular expressions as appropriate for SimpleDateFormatter.
 * 
 * @author Ceki
 * 
 */
class CharSequenceToRegexMapper {

  String toRegex(CharSequenceState css) {
    final int occurrences = css.occurrences;
    final char c = css.c;
    switch (css.c) {
    case 'G':
    case 'z':
      return ".*";
    case 'M':
      if (occurrences >= 3) {
        return ".{3,12}";
      } else {
        return number(occurrences);
      }
    case 'y':
    case 'w':
    case 'W':
    case 'D':
    case 'd':
    case 'F':
    case 'H':
    case 'k':
    case 'K':
    case 'h':
    case 'm':
    case 's':
    case 'S':
      return number(occurrences);
    case 'E':
      return ".{2,12}";
    case 'a':
      return ".{2}";
    case 'Z':
      return "(\\+|-)\\d{4}";
    case '.':
      return "\\.";
    case '\\': 
      throw new IllegalStateException("Forward slashes are not allowed");
    case '\'':
      if (occurrences == 1) {
        return "";
      }
      throw new IllegalStateException("Too many single quotes");
    default:
      if (occurrences == 1) {
        return "" + c;
      } else {
        return c + "{" + occurrences + "}";
      }
    }
  }

  private String number(int occurrences) {
    return "\\d{" + occurrences + "}";
  }
}
