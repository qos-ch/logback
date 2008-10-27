/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new JUnit4TestAdapter(SkippingInInterpreterTest.class));
    suite.addTest(new JUnit4TestAdapter(TrivialcConfiguratorTest.class));
    suite.addTest(ch.qos.logback.core.joran.action.PackageTest.suite());
    suite.addTest(ch.qos.logback.core.joran.event.PackageTest.suite());
    suite.addTest(ch.qos.logback.core.joran.spi.PackageTest.suite());
    suite.addTest(ch.qos.logback.core.joran.replay.PackageTest.suite());
    suite.addTest(ch.qos.logback.core.joran.implicitAction.PackageTest.suite());
    return suite;
  }
}
