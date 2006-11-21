/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.access;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTest extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(ch.qos.logback.access.net.PackageTest.suite());
    suite.addTest(ch.qos.logback.access.pattern.PackageTest.suite());
    suite.addTest(ch.qos.logback.access.jetty.PackageTest.suite());
    suite.addTest(ch.qos.logback.access.filter.PackageTest.suite());
    return suite;
  }
}
