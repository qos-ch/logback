/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic;

import junit.framework.*;

public class PackageTest extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(LoggerContextTest.class);
    suite.addTestSuite(DynamicLoggerContextTest.class);
    suite.addTestSuite(PatternLayoutTest.class);
    suite.addTestSuite(BasicLoggerTest.class);
    suite.addTestSuite(MessageFormattingTest.class);
    suite.addTestSuite(MDCTest.class);
    suite.addTestSuite(TurboFilteringInLoggerTest.class);
    return suite;
  }
}