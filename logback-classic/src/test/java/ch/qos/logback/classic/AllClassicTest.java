/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ org.slf4j.impl.PackageTest.class, ch.qos.logback.classic.PackageTest.class, ch.qos.logback.classic.util.PackageTest.class,
        ch.qos.logback.classic.control.PackageTest.class, ch.qos.logback.classic.joran.PackageTest.class, ch.qos.logback.classic.rolling.PackageTest.class,
        ch.qos.logback.classic.jmx.PackageTest.class, ch.qos.logback.classic.boolex.PackageTest.class, ch.qos.logback.classic.selector.PackageTest.class,
        ch.qos.logback.classic.html.PackageTest.class, ch.qos.logback.classic.net.PackageTest.class, ch.qos.logback.classic.pattern.PackageTest.class,
        ch.qos.logback.classic.encoder.PackageTest.class, ch.qos.logback.classic.db.PackageTest.class, ch.qos.logback.classic.spi.PackageTest.class,
        ch.qos.logback.classic.turbo.PackageTest.class, ch.qos.logback.classic.sift.PackageTest.class, ch.qos.logback.classic.jul.PackageTest.class,
        ch.qos.logback.classic.issue.PackageTest.class })
public class AllClassicTest {

}
