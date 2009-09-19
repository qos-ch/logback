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
package ch.qos.logback.core.joran.spi;

import java.util.ArrayList;

/**
 * A pattern is used to designate XML elements in a document.
 * 
 * <p>For more information see
 * http://logback.qos.ch/manual/onJoran.html#pattern
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class Pattern {

  // contains String instances
  ArrayList<String> partList = new ArrayList<String>();

  public Pattern() {
  }

  /**
   * Build a pattern from a string.
   * 
   * Note that "/x" is considered equivalent to "x" and to "x/"
   * 
   */
  public Pattern(String p) {
    this();

    if (p == null) {
      return;
    }

    int lastIndex = 0;

    // System.out.println("p is "+ p);
    while (true) {
      int k = p.indexOf('/', lastIndex);

      // System.out.println("k is "+ k);
      if (k == -1) {
        String lastPart = p.substring(lastIndex);
        if (lastPart != null && lastPart.length() > 0) {
          partList.add(p.substring(lastIndex));
        }
        break;
      } else {
        String c = p.substring(lastIndex, k);

        if (c.length() > 0) {
          partList.add(c);
        }

        lastIndex = k + 1;
      }
    }

    // System.out.println(components);
  }

  public Object clone() {
    Pattern p = new Pattern();
    p.partList.addAll(this.partList);
    return p;
  }

  public void push(String s) {
    partList.add(s);
  }

  public int size() {
    return partList.size();
  }

  public String get(int i) {
    return (String) partList.get(i);
  }

  public void pop() {
    if (!partList.isEmpty()) {
      partList.remove(partList.size() - 1);
    }
  }

  public String peekLast() {
    if (!partList.isEmpty()) {
      int size = partList.size();
      return (String) partList.get(size - 1);
    } else {
      return null;
    }
  }

  /**
   * Returns the number of "tail" components that this pattern has in common
   * with the pattern p passed as parameter. By "tail" components we mean the
   * components at the end of the pattern.
   */
  public int getTailMatchLength(Pattern p) {
    if (p == null) {
      return 0;
    }

    int lSize = this.partList.size();
    int rSize = p.partList.size();

    // no match possible for empty sets
    if ((lSize == 0) || (rSize == 0)) {
      return 0;
    }

    int minLen = (lSize <= rSize) ? lSize : rSize;
    int match = 0;

    // loop from the end to the front
    for (int i = 1; i <= minLen; i++) {
      String l = (String) this.partList.get(lSize - i);
      String r = (String) p.partList.get(rSize - i);

      if (equalityCheck(l, r)) {
        match++;
      } else {
        break;
      }
    }

    return match;
  }

  /**
   * Returns the number of "prefix" components that this pattern has in common
   * with the pattern p passed as parameter. By "prefix" components we mean the
   * components at the beginning of the pattern.
   */
  public int getPrefixMatchLength(Pattern p) {
    if (p == null) {
      return 0;
    }

    int lSize = this.partList.size();
    int rSize = p.partList.size();

    // no match possible for empty sets
    if ((lSize == 0) || (rSize == 0)) {
      return 0;
    }

    int minLen = (lSize <= rSize) ? lSize : rSize;
    int match = 0;

    for (int i = 0; i < minLen; i++) {
      String l = (String) this.partList.get(i);
      String r = (String) p.partList.get(i);

      if (equalityCheck(l, r)) {
        match++;
      } else {
        break;
      }
    }

    return match;
  }

  private boolean equalityCheck(String x, String y) {
    return x.equalsIgnoreCase(y);
  }

  @Override
  public boolean equals(Object o) {
    if ((o == null) || !(o instanceof Pattern)) {
      return false;
    }

    Pattern r = (Pattern) o;

    if (r.size() != size()) {
      return false;
    }

    int len = size();

    for (int i = 0; i < len; i++) {
      if (!equalityCheck(get(i), r.get(i))) {
        return false;
      }
    }

    // if everything matches, then the two patterns are equal
    return true;
  }

  @Override
  public int hashCode() {
    int hc = 0;
    int len = size();

    for (int i = 0; i < len; i++) {
      // make Pattern comparisons case insensitive
      // http://jira.qos.ch/browse/LBCORE-76
      hc ^= get(i).toLowerCase().hashCode();
    }

    return hc;
  }

  @Override
  public String toString() {
    int size = partList.size();
    String result = "";
    for (int i = 0; i < size; i++) {
      result += "[" + partList.get(i) + "]";
    }
    return result;
  }
}
