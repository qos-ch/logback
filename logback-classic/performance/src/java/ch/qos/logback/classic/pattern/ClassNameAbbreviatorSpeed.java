/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.pattern.ClassNameAbbreviator;

public class ClassNameAbbreviatorSpeed {

  static ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(21);
  static String name = "com.logback.wombat.alligator.tomato.Foobar";

  public static void main(String[] args) {
    loop(1000);
    loop(10000);
    loop(100000);
  }

  static void loop(final int size) {
    long start = System.nanoTime();
    for (int i = 0; i < size; i++) {
      abbreviator.abbreviate(name);
    }
    long result = System.nanoTime() - start;
    System.out.println("Average abbrev speed: " + (result / size) + " nanos");
  }
}
