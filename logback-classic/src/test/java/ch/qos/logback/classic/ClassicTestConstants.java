/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic;

public class ClassicTestConstants {

  final static public String ISO_REGEX =  "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}";
  final static public String NAKED_MAIN_REGEX =  "[mM]ain(\\sThread)?";
  final static public String MAIN_REGEX =  "\\["+NAKED_MAIN_REGEX+"\\]";
  final static public String JORAN_INPUT_PREFIX = "src/test/input/joran";
}
