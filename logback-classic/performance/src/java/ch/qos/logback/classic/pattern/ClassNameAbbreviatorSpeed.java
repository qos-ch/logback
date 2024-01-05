/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
