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
package ch.qos.logback.access.db;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.EnvUtilForTests;
import ch.qos.logback.core.util.EnvUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.access.dummy.DummyAccessEventBuilder;
import ch.qos.logback.access.joran.JoranConfigurator;
import ch.qos.logback.access.spi.AccessContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.util.StatusPrinter;

public class DBAppenderIntegrationTest {

    static String LOCAL_HOST_NAME = EnvUtilForTests.getLocalHostName();
    static String[] CONFORMING_HOST_LIST = new String[] { "Orion" };

    int diff = new Random(System.nanoTime()).nextInt(10000);
    AccessContext context = new AccessContext();
    StatusChecker statusChecker = new StatusChecker(context);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }

    public void doTest(String configFile) throws JoranException {
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(configFile);

        Appender<IAccessEvent> appender = context.getAppender("DB");

        for (int i = 0; i < 10; i++) {
            IAccessEvent event = DummyAccessEventBuilder.buildNewAccessEvent();
            appender.doAppend(event);
        }

        StatusPrinter.print(context);

        // check that there were no errors
        assertEquals(Status.INFO, statusChecker.getHighestLevel(0));

    }

    static boolean isConformingHostAndJDK16OrHigher() {
        if (!EnvUtil.isJDK6OrHigher()) {
            return false;
        }
        return EnvUtilForTests.isLocalHostNameInList(CONFORMING_HOST_LIST);
    }

    @Test
    public void sqlserver() throws Exception {
        // perform test only on conforming hosts
        if (!isConformingHostAndJDK16OrHigher()) {
            return;
        }
        doTest("src/test/input/integration/db/sqlserver-with-driver.xml");
    }

    @Test
    @Ignore
    public void oracle10g() throws Exception {
        // perform test only on conforming hosts
        if (!isConformingHostAndJDK16OrHigher()) {
            return;
        }
        doTest("src/test/input/integration/db/oracle10g-with-driver.xml");
    }

    @Test
    @Ignore
    public void oracle11g() throws Exception {
        // perform test only on conforming hosts
        if (!isConformingHostAndJDK16OrHigher()) {
            return;
        }
        doTest("src/test/input/integration/db/oracle11g-with-driver.xml");
    }

    @Test
    public void mysql() throws Exception {
        // perform test only on conforming hosts
        if (!isConformingHostAndJDK16OrHigher()) {
            return;
        }
        doTest("src/test/input/integration/db/mysql-with-driver.xml");
    }

    @Test
    public void postgres() throws Exception {
        // perform test only on conforming hosts
        if (!isConformingHostAndJDK16OrHigher()) {
            return;
        }
        doTest("src/test/input/integration/db/postgresql-with-driver.xml");
    }

}
