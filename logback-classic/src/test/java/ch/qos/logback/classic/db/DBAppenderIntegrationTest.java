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
package ch.qos.logback.classic.db;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.db.DriverManagerConnectionSource;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.EnvUtil;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DBAppenderIntegrationTest {

    static String LOCAL_HOST_NAME;
    static String[] CONFORMING_HOST_LIST = new String[] { "Orion" };
    static String[] POSTGRES_CONFORMING_HOST_LIST = new String[] { "haro" };
    static String[] MYSQL_CONFORMING_HOST_LIST = new String[] { "xharo" };
    static String[] ORACLE_CONFORMING_HOST_LIST = new String[] { "xharo" };

    int diff = RandomUtil.getPositiveInt();
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

    DriverManagerConnectionSource getConnectionSource() {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) lc.getLogger(Logger.ROOT_LOGGER_NAME);

        DBAppender dbAppender = (DBAppender) root.getAppender("DB");
        assertNotNull(dbAppender);
        return (DriverManagerConnectionSource) dbAppender.getConnectionSource();

    }

    public void doTest(String configFile) throws JoranException, SQLException {
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        configurator.doConfigure(configFile);

        Logger logger = lc.getLogger(DBAppenderIntegrationTest.class);

        // the key userid is used in SiftingAppender test
        // suffix with diff to avoid collision
        MDC.put("userid" + diff, "user" + diff);
        int runLength = 5;
        for (int i = 1; i <= runLength; i++) {
            logger.debug("This is a debug message. Message number: " + (diff + i));
        }

        Exception e = new Exception("Just testing", getCause());
        logger.error("At last an error.", e);

        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);

        long lastEventId = getLastEventId();
        verify(lastEventId);

        // check that there were no errors
        StatusChecker checker = new StatusChecker(lc);
        checker.assertIsErrorFree();
    }

    long getLastEventId() throws SQLException {
        DriverManagerConnectionSource cs = getConnectionSource();

        Connection con = cs.getConnection();
        Statement statement = con.createStatement();
        statement.setMaxRows(1);
        ResultSet rs = statement.executeQuery("select event_id from logging_event order by event_id desc");
        rs.next();
        long eventId = rs.getLong(1);
        rs.close();
        statement.close();
        return eventId;
    }

    void verify(long lastEventId) throws SQLException {
        verifyDebugMsg(lastEventId);
        verifyException(lastEventId);
        verifyProperty(lastEventId);

    }

    void verifyDebugMsg(long lastEventId) throws SQLException {
        DriverManagerConnectionSource cs = getConnectionSource();
        Connection con = cs.getConnection();
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("select formatted_message from logging_event where event_id='" + (lastEventId - 1) + "'");
        rs.next();
        String msg = rs.getString(1);
        assertEquals("This is a debug message. Message number: " + (diff + 5), msg);
    }

    @SuppressWarnings("unchecked")
    void verifyProperty(long lastEventId) throws SQLException {
        DriverManagerConnectionSource cs = getConnectionSource();
        Connection con = cs.getConnection();
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("select mapped_key, mapped_value from logging_event_property where event_id='" + (lastEventId - 1) + "'");

        Map<String, String> witness = lc.getCopyOfPropertyMap();
        witness.putAll(MDC.getCopyOfContextMap());

        Map<String, String> map = new HashMap<String, String>();
        while (rs.next()) {
            String key = rs.getString(1);
            String val = rs.getString(2);
            map.put(key, val);
        }

        assertEquals(witness, map);
    }

    void verifyException(long lastEventId) throws SQLException {
        DriverManagerConnectionSource cs = getConnectionSource();
        Connection con = cs.getConnection();
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("select trace_line from logging_event_exception where event_id='" + (lastEventId) + "' AND I='0'");
        rs.next();
        String traceLine = rs.getString(1);
        assertEquals("java.lang.Exception: Just testing", traceLine);
    }

    Throwable getCause() {
        return new IllegalStateException("test cause");
    }

    static boolean isConformingHostAndJDK16OrHigher(String[] conformingHostList) {
        if (!EnvUtil.isJDK6OrHigher()) {
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
    public void oracle10g() throws Exception {
        // perform test only on conforming hosts
        if (!isConformingHostAndJDK16OrHigher(ORACLE_CONFORMING_HOST_LIST)) {
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
        if (!isConformingHostAndJDK16OrHigher(MYSQL_CONFORMING_HOST_LIST)) {
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
        System.out.println("running postgres() test");
        doTest("src/test/input/integration/db/postgresql-with-driver.xml");
    }

}
