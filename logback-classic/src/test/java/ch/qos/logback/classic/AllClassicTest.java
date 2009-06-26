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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({org.slf4j.impl.PackageTest.class,
    ch.qos.logback.classic.PackageTest.class,
    ch.qos.logback.classic.util.PackageTest.class,
    ch.qos.logback.classic.control.PackageTest.class,
    ch.qos.logback.classic.joran.PackageTest.class,
    ch.qos.logback.classic.jmx.PackageTest.class,
    ch.qos.logback.classic.boolex.PackageTest.class,
    ch.qos.logback.classic.selector.PackageTest.class,
    ch.qos.logback.classic.html.PackageTest.class,
    ch.qos.logback.classic.net.PackageTest.class,
    ch.qos.logback.classic.pattern.PackageTest.class,
    ch.qos.logback.classic.db.PackageTest.class,
    ch.qos.logback.classic.spi.PackageTest.class,
    ch.qos.logback.classic.turbo.PackageTest.class,
    ch.qos.logback.classic.sift.PackageTest.class,
    ch.qos.logback.classic.issue.PackageTest.class})
public class AllClassicTest {


}
