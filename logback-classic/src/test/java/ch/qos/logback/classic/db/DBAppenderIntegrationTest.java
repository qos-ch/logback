/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.db;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.MDC;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.Env;
import ch.qos.logback.core.util.StatusPrinter;

public class DBAppenderIntegrationTest {

  static String LOCAL_HOST_NAME;
  static String[] CONFORMING_HOST_LIST = new String[] { "Orion" };
  static String[] POSTGRES_CONFORMING_HOST_LIST = new String[] { "haro" };

  int diff = new Random(System.nanoTime()).nextInt(10000);
  LoggerContext lc = new LoggerContext();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    InetAddress localhostIA = InetAddress.getLocalHost();
    LOCAL_HOST_NAME = localhostIA.getHostName();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    lc.setName("lc" + diff);
  }

  @After
  public void tearDown() throws Exception {
    // lc will never be used again
    lc.stop();
  }

  public void doTest(String configFile) throws JoranException {
    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(lc);
    configurator.doConfigure(configFile);

    Logger logger = lc.getLogger(DBAppenderIntegrationTest.class);

    MDC.put("userid", "user" + diff);
    int runLength = 5;
    for (int i = 1; i <= runLength; i++) {
      logger.debug("This is a debug message. Message number: " + i);
    }

    Exception e = new Exception("Just testing", getCause());
    logger
        .error("At last an error.", e);
    e.printStackTrace();
    // check that there were no errors
    StatusPrinter.print(lc);
    assertEquals(Status.INFO, lc.getStatusManager().getLevel());

  }

  Throwable getCause() {
    return new IllegalStateException("test cause");
  }

  static boolean isConformingHostAndJDK16OrHigher(String[] conformingHostList) {
    if (!Env.isJDK6OrHigher()) {
      return false;
    }
    for (String conformingHost : conformingHostList) {
      if (conformingHost.equalsIgnoreCase(LOCAL_HOST_NAME)) {
        return true;
      }
    }
    return false;
  }

  static boolean isConformingHostAndJDK16OrHigher() {
    return isConformingHostAndJDK16OrHigher(CONFORMING_HOST_LIST);
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
    if (!isConformingHostAndJDK16OrHigher(POSTGRES_CONFORMING_HOST_LIST)) {
      return;
    }
    System.out.println("doing test");
    doTest("src/test/input/integration/db/postgresql-with-driver.xml");
  }

}
