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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( { ch.qos.logback.access.spi.PackageTest.class,
    ch.qos.logback.access.net.PackageTest.class,
    ch.qos.logback.access.pattern.PackageTest.class,
    ch.qos.logback.access.jetty.PackageTest.class,
    ch.qos.logback.access.filter.PackageTest.class,
    ch.qos.logback.access.sift.PackageTest.class })
public class AllAccessTest {

}
