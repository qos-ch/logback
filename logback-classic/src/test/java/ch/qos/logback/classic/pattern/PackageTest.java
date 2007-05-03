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


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase {
  
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(ConverterTest.class);
    suite.addTestSuite(ClassNameAbbreviatorTest.class);
    suite.addTestSuite(MDCConverterTest.class);
    suite.addTestSuite(MarkerConverterTest.class);
    return suite;
  }
}