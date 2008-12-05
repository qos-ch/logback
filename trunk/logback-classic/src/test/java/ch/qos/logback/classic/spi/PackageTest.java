/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.spi;


import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase {
  
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(ContextListenerTest.class);
    suite.addTestSuite(CallerDataTest.class); 
    suite.addTest(new JUnit4TestAdapter (LoggerComparatorTest.class));
    suite.addTest(new JUnit4TestAdapter (LoggingEventSerializationTest.class));
    suite.addTest(new JUnit4TestAdapter(LoggingEventSerializationPerfTest.class));
    suite.addTest(new JUnit4TestAdapter(ThrowableToDataPointTest.class));
    suite.addTest(new JUnit4TestAdapter(BasicCPDCTest.class));
    return suite;
  }
}