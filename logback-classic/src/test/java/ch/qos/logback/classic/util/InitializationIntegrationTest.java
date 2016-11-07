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
package ch.qos.logback.classic.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class InitializationIntegrationTest {

    @Test
    public void smoke() {
        Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        ListAppender la = (ListAppender) root.getAppender("LIST");
        assertNotNull(la);
    }
}
