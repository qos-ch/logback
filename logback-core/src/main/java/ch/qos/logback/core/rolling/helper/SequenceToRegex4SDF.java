/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.rolling.helper;

/**
 * This class supports mapping tokens (set of same character sequences) to
 * regular expressions as appropriate for SimpleDateFormatter.
 * 
 * @author ceki
 * 
 */
class SequenceToRegex4SDF {
  final char c;
  int occurrences;

  public SequenceToRegex4SDF(char c) {
    this.c = c;
    this.occurrences = 1;
  }

  void inc() {
    occurrences++;
  }

  String toRegex() {
    switch (c) {
    case 'G':
      return ".*";
    case 'M':
      if (occurrences >= 3) {
        return ".*";
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
      return ".{3,12}";
    case 'a':
      return ".{2}";
    case 'z':
      return ".*";
    case 'Z':
      return "(\\+|-)\\d{4}";
    case '.':
      return "\\.";
    case '\\': 
      throw new IllegalStateException("Forward slashes are not allowed");
    default:
      if (occurrences == 1) {
        return "" + c;
      } else {
        return c + "{" + occurrences + "}";
      }
    }
  }

  @Override
  public String toString() {
    return c + "(" + occurrences + ")";
  }

  private String number(int occurences) {
    return "\\d{" + occurrences + "}";
  }
}