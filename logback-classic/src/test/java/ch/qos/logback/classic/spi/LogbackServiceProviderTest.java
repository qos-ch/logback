/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.classic.spi;

import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogbackServiceProviderTest {

    LogbackServiceProvider provider = new LogbackServiceProvider();

    @Test
    public void testContrxtStart() {
        provider.initialize();
        LoggerContext loggerFactory = (LoggerContext) provider.getLoggerFactory();

        assertTrue(loggerFactory.isStarted());

    }
}
