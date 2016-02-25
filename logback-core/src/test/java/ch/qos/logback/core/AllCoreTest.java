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
package ch.qos.logback.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BasicStatusManagerTest.class, ch.qos.logback.core.status.PackageTest.class, ch.qos.logback.core.util.PackageTest.class,
        ch.qos.logback.core.helpers.PackageTest.class, ch.qos.logback.core.subst.PackageTest.class, ch.qos.logback.core.pattern.PackageTest.class,
        ch.qos.logback.core.PackageTest.class, ch.qos.logback.core.joran.PackageTest.class, ch.qos.logback.core.appender.PackageTest.class,
        ch.qos.logback.core.spi.PackageTest.class, ch.qos.logback.core.rolling.PackageTest.class, ch.qos.logback.core.net.PackageTest.class,
        ch.qos.logback.core.sift.PackageTest.class, ch.qos.logback.core.encoder.PackageTest.class, ch.qos.logback.core.recovery.PackageTest.class })
public class AllCoreTest {
}
