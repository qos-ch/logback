/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.joran.spi;

import java.util.ArrayList;


public class Pattern {
  
  // contains String instances
  ArrayList<String> components;

  public Pattern() {
    components = new ArrayList<String>();
  }
  
  public Object clone() {
    Pattern p = new Pattern();
    p.components.addAll(this.components);
    return p;
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

    //System.out.println("p is "+ p);
    while (true) {
      int k = p.indexOf('/', lastIndex);

      //System.out.println("k is "+ k);
      if (k == -1) {
        components.add(p.substring(lastIndex));

        break;
      } else {
        String c = p.substring(lastIndex, k);

        if (c.length() > 0) {
          components.add(c);
        }

        lastIndex = k + 1;
      }
    }

    //System.out.println(components);
  }

  public void push(String s) {
    components.add(s);
  }

  public int size() {
    return components.size();
  }

  public String get(int i) {
    return (String) components.get(i);
  }

  public void pop() {
    if (!components.isEmpty()) {
      components.remove(components.size() - 1);
    }
  }
  
  public String peekLast() {
    if (!components.isEmpty()) {
      int size = components.size();
      return (String) components.get(size - 1);
    } else {
     return null;
    }
  }

  /**
   * Returns the number of "tail" components that this pattern has in common
   * with the pattern p passed as parameter. By "tail" components we mean the
   * components at the end of the pattern.
   */
  public int tailMatch(Pattern p) {
    if (p == null) {
      return 0;
    }

    int lSize = this.components.size();
    int rSize = p.components.size();

    // no match possible for empty sets
    if ((lSize == 0) || (rSize == 0)) {
      return 0;
    }

    int minLen = (lSize <= rSize) ? lSize : rSize;
    int match = 0;

    // loop from the end to the front
    for (int i = 1; i <= minLen; i++) {
      String l = (String) this.components.get(lSize - i);
      String r = (String) p.components.get(rSize - i);

      if (l.equals(r)) {
        match++;
      } else {
        break;
      }
    }

    return match;
  }

  public boolean equals(Object o) {
    //System.out.println("in equals:" +this+ " vs. " + o);
    if ((o == null) || !(o instanceof Pattern)) {
      return false;
    }

    //System.out.println("both are Patterns");
    Pattern r = (Pattern) o;

    if (r.size() != size()) {
      return false;
    }

    //System.out.println("both are size compatible");
    int len = size();

    for (int i = 0; i < len; i++) {
      if (!(get(i).equals(r.get(i)))) {
        return false;
      }
    }

    // if everything matches, then the twp patterns are equal
    return true;
  }

  public int hashCode() {
    int hc = 0;
    int len = size();

    for (int i = 0; i < len; i++) {
      hc ^= get(i).hashCode();

      //System.out.println("i = "+i+", hc="+hc);
    }

    return hc;
  }

  public String toString() {
    int size = components.size();
    String result = "";
    for(int i = 0; i < size; i++) {
      result +=  "/" + components.get(i);
    }
    return result;
  }
}
