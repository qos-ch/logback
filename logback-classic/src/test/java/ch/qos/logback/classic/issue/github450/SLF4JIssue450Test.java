/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2025, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.issue.github450;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SLF4JIssue450Test {


    @Test
    public void smoke() {
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, ClassicTestConstants.INPUT_PREFIX + "issue/gh_issues_450.xml");
        System.setProperty(CoreConstants.STATUS_LISTENER_CLASS_KEY, "stdout");
        Logger logger = LoggerFactory.getLogger(SLF4JIssue450Test.class);
        logger.info("toto");
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        ListAppender listAppender = (ListAppender) root.getAppender("LIST");

        LoggingEvent le0 = (LoggingEvent) listAppender.list.get(0);

        String val = le0.getMDCPropertyMap().get("issues450");
        assertNotNull(val);
        assertEquals("12", val);

    }
}
