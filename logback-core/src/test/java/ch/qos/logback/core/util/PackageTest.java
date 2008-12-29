/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  DurationTest.class,
  FileSizeTest.class,
  FileUtilTest.class,
  OptionHelperTest.class,
  StatusPrinterTest.class,
  TimeUtilTest.class,
  ContentTypeUtilTest.class})
public class PackageTest {
}
