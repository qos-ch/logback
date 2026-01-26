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

import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;

import ch.qos.logback.classic.testUtil.StringPrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.helpers.Reporter.SLF4J_INTERNAL_VERBOSITY_KEY;

public class InvocationTest {

    private final PrintStream oldErr = System.err;
    final String loggerName = this.getClass().getName();
    StringPrintStream sps = new StringPrintStream(oldErr, true);

    String CONNECTED_WITH_MESSAGE = "SLF4J(D): Connected with provider of type [ch.qos.logback.classic.spi.LogbackServiceProvider]";

    @BeforeEach
    public void setUp() throws Exception {
        System.setProperty(SLF4J_INTERNAL_VERBOSITY_KEY, "debug");
        System.setErr(sps);
    }

    @AfterEach
    public void tearDown() throws Exception {
        LoggerFactoryFriend.reset();
        System.setErr(oldErr);
        System.clearProperty(SLF4J_INTERNAL_VERBOSITY_KEY);
    }

    // https://jira.qos.ch/browse/LOGBACK-1568 would have been prevented
    // had this silly test existed.
    @Test
    public void smoke() {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.debug("Hello world.");

        assertEquals(1, sps.stringList.size());
        assertEquals(CONNECTED_WITH_MESSAGE, sps.stringList.get(0));

    }

}
