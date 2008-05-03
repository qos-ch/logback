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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTest extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite();
 
    suite.addTest(ch.qos.logback.classic.PackageTest.suite());
    suite.addTest(ch.qos.logback.classic.control.PackageTest.suite());
    
    suite.addTest(ch.qos.logback.classic.joran.PackageTest.suite());
    suite.addTest(ch.qos.logback.classic.boolex.PackageTest.suite());
    suite.addTest(ch.qos.logback.classic.selector.PackageTest.suite()); 
     
    suite.addTest(ch.qos.logback.classic.html.PackageTest.suite()); 
    suite.addTest(ch.qos.logback.classic.net.PackageTest.suite());
    suite.addTest(ch.qos.logback.classic.pattern.PackageTest.suite()); 
    suite.addTest(ch.qos.logback.classic.db.PackageTest.suite()); 
    suite.addTest(ch.qos.logback.classic.spi.PackageTest.suite()); 
    suite.addTest(ch.qos.logback.classic.turbo.PackageTest.suite()); 
    suite.addTest(ch.qos.logback.classic.stopwatch.PackageTest.suite()); 
    
    return suite;
  }
}
