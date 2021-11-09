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
package ch.qos.logback.access.joran;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.access.AccessTestConstants;
import ch.qos.logback.access.spi.AccessContext;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.testUtil.StatusChecker;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
@Ignore
public class ConditionalTest {

    AccessContext context = new AccessContext();
    StatusChecker checker = new StatusChecker(context);

    int diff = RandomUtil.getPositiveInt();
    String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/";

    @Before
    public void setUp() {
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
            context.putProperty("aHost", localhost.getHostName());
        } catch (final UnknownHostException e) {
            e.printStackTrace();
        }
    }

    void configure(final String file) throws JoranException {
        final JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(context);
        jc.doConfigure(file);
    }

    @Test
    public void conditionalConsoleApp_IF_THEN_True() throws JoranException, UnknownHostException {
        configure(AccessTestConstants.TEST_DIR_PREFIX + "input/joran/conditional/conditionalConsole.xml");
        final ConsoleAppender<IAccessEvent> consoleAppender = (ConsoleAppender<IAccessEvent>) context.getAppender("CON");
        assertNotNull(consoleAppender);
        assertTrue(checker.isErrorFree(0));
    }

    @Test
    public void conditionalConsoleApp_IF_THEN_False() throws JoranException, IOException, InterruptedException {
        context.putProperty("aHost", null);
        configure(AccessTestConstants.TEST_DIR_PREFIX + "input/joran/conditional/conditionalConsole.xml");

        final ConsoleAppender<IAccessEvent> consoleAppender = (ConsoleAppender<IAccessEvent>) context.getAppender("CON");
        assertNull(consoleAppender);

        final StatusChecker checker = new StatusChecker(context);
        assertTrue(checker.isErrorFree(0));
    }

    @Test
    public void conditionalConsoleApp_ELSE() throws JoranException, IOException, InterruptedException {
        configure(AccessTestConstants.TEST_DIR_PREFIX + "input/joran/conditional/conditionalConsole_ELSE.xml");
        final ConsoleAppender<IAccessEvent> consoleAppender = (ConsoleAppender<IAccessEvent>) context.getAppender("CON");
        assertNull(consoleAppender);

        final ListAppender<IAccessEvent> listAppender = (ListAppender<IAccessEvent>) context.getAppender("LIST");
        assertNotNull(listAppender);
        assertTrue(checker.isErrorFree(0));
    }
}
