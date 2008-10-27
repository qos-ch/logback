/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic;

public class TestConstants {

  final static public String ISO_REGEX =  "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}";
  final static public String NAKED_MAIN_REGEX =  "[mM]ain(\\sThread)?";
  final static public String MAIN_REGEX =  "\\["+NAKED_MAIN_REGEX+"\\]";
  final static public String JORAN_ONPUT_PREFIX = "src/test/input/joran";
}
