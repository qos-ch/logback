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
package org.slf4j.impl;

import static org.junit.Assert.assertEquals;

import ch.qos.logback.core.status.StatusChecker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusPrinter;

public class RecursiveInitializationTest {

    int diff = RandomUtil.getPositiveInt();

    @Before
    public void setUp() throws Exception {
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "recursiveInit.xml");
        StaticLoggerBinderFriend.reset();
        LoggerFactoryFriend.reset();
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
    }

    @Test
    public void recursiveLogbackInitialization() {
        Logger logger = LoggerFactory.getLogger("RecursiveInitializationTest" + diff);
        logger.info("RecursiveInitializationTest");

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
        StatusChecker statusChecker = new StatusChecker(loggerContext);
        assertEquals("Was expecting no errors", Status.WARN, statusChecker.getHighestLevel(0));
    }

}
