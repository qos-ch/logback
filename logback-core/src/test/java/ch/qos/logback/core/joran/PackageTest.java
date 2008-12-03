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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({SkippingInInterpreterTest.class, TrivialcConfiguratorTest.class, ch.qos.logback.core.joran.action.PackageTest.class,
  ch.qos.logback.core.joran.event.PackageTest.class,
  ch.qos.logback.core.joran.spi.PackageTest.class,
  ch.qos.logback.core.joran.replay.PackageTest.class,
  ch.qos.logback.core.joran.implicitAction.PackageTest.class
 })
public class PackageTest {

}
