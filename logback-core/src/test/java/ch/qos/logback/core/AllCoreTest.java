/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({BasicStatusManagerTest.class,
  ch.qos.logback.core.util.PackageTest.class,
  ch.qos.logback.core.helpers.PackageTest.class,
  ch.qos.logback.core.pattern.PackageTest.class,
  ch.qos.logback.core.PackageTest.class,
  ch.qos.logback.core.joran.PackageTest.class,
  ch.qos.logback.core.appender.PackageTest.class,
  ch.qos.logback.core.spi.PackageTest.class,
  ch.qos.logback.core.rolling.PackageTest.class})
public class AllCoreTest {

}
