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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ch.qos.logback.access.spi.IAccessEvent;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.access.dummy.DummyResponse;
import ch.qos.logback.access.dummy.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessContext;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.db.DriverManagerConnectionSource;
import ch.qos.logback.core.util.StatusPrinter;

public class DBAppenderHSQLTest {
    static DBAppenderHSQLTestFixture DB_APPENDER_HSQL_TEST_FIXTURE;

    AccessContext context;
    DBAppender appender;
    DriverManagerConnectionSource connectionSource;

    int existingEventTableRowCount;
    Statement stmt;

    @BeforeClass
    static public void fixtureSetUp() throws SQLException {
        DB_APPENDER_HSQL_TEST_FIXTURE = new DBAppenderHSQLTestFixture();
        DB_APPENDER_HSQL_TEST_FIXTURE.setUp();
    }

    @AfterClass
    static public void fixtureTearDown() throws SQLException {
        DB_APPENDER_HSQL_TEST_FIXTURE.tearDown();
    }

    @Before
    public void setUp() throws SQLException {
        context = new AccessContext();
        context.setName("default");
        appender = new DBAppender();
        appender.setName("DB");
        appender.setContext(context);
        connectionSource = new DriverManagerConnectionSource();
        connectionSource.setContext(context);
        connectionSource.setDriverClass(DBAppenderHSQLTestFixture.DRIVER_CLASS);
        connectionSource.setUrl(DB_APPENDER_HSQL_TEST_FIXTURE.url);
        connectionSource.setUser(DB_APPENDER_HSQL_TEST_FIXTURE.user);
        connectionSource.setPassword(DB_APPENDER_HSQL_TEST_FIXTURE.password);
        connectionSource.start();
        appender.setConnectionSource(connectionSource);

        stmt = connectionSource.getConnection().createStatement();
        existingEventTableRowCount = existingEventTableRowCount(stmt);
    }

    @After
    public void tearDown() throws SQLException {
        context = null;
        appender = null;
        connectionSource = null;
        stmt.close();
    }

    int existingEventTableRowCount(Statement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT count(*) FROM access_event");
        int result = -1;
        if (rs.next()) {
            result = rs.getInt(1);
        }
        rs.close();
        return result;
    }

    private void setInsertHeadersAndStart(boolean insert) {
        appender.setInsertHeaders(insert);
        appender.start();
    }

    @Test
    public void testAppendAccessEvent() throws SQLException {
        setInsertHeadersAndStart(false);

        IAccessEvent event = createAccessEvent();
        appender.append(event);

        Statement stmt = connectionSource.getConnection().createStatement();
        ResultSet rs = null;
        rs = stmt.executeQuery("SELECT * FROM access_event where EVENT_ID = " + existingEventTableRowCount);
        if (rs.next()) {
            assertEquals(event.getTimeStamp(), rs.getLong(1));
            assertEquals(event.getRequestURI(), rs.getString(2));
            assertEquals(event.getRequestURL(), rs.getString(3));
            assertEquals(event.getRemoteHost(), rs.getString(4));
            assertEquals(event.getRemoteUser(), rs.getString(5));
            assertEquals(event.getRemoteAddr(), rs.getString(6));
            assertEquals(event.getProtocol(), rs.getString(7));
            assertEquals(event.getMethod(), rs.getString(8));
            assertEquals(event.getServerName(), rs.getString(9));
            assertEquals(event.getRequestContent(), rs.getString(10));
        } else {
            fail("No row was inserted in the database");
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void testCheckNoHeadersAreInserted() throws Exception {
        setInsertHeadersAndStart(false);

        IAccessEvent event = createAccessEvent();
        appender.append(event);
        StatusPrinter.print(context.getStatusManager());

        // Check that no headers were inserted
        Statement stmt = connectionSource.getConnection().createStatement();
        ResultSet rs = null;
        rs = stmt.executeQuery("SELECT * FROM access_event_header where EVENT_ID = " + existingEventTableRowCount);

        assertFalse(rs.next());
        rs.close();
        stmt.close();
    }

    @Test
    public void testAppendHeaders() throws SQLException {
        setInsertHeadersAndStart(true);

        IAccessEvent event = createAccessEvent();
        appender.append(event);

        Statement stmt = connectionSource.getConnection().createStatement();
        ResultSet rs = null;
        rs = stmt.executeQuery("SELECT * FROM access_event_header");
        String key;
        String value;
        if (!rs.next()) {
            fail("There should be results to this query");
        } else {
            key = rs.getString(2);
            value = rs.getString(3);
            assertNotNull(key);
            assertNotNull(value);
            assertEquals(event.getRequestHeader(key), value);
            rs.next();
            key = rs.getString(2);
            value = rs.getString(3);
            assertNotNull(key);
            assertNotNull(value);
            assertEquals(event.getRequestHeader(key), value);
        }
        if (rs.next()) {
            fail("There should be no more rows available");
        }

        rs.close();
        stmt.close();
    }

    @Test
    public void testAppendMultipleEvents() throws SQLException {
        setInsertHeadersAndStart(false);
        String uri = "testAppendMultipleEvents";
        for (int i = 0; i < 10; i++) {
            IAccessEvent event = createAccessEvent(uri);
            appender.append(event);
        }

        StatusPrinter.print(context);

        Statement stmt = connectionSource.getConnection().createStatement();
        ResultSet rs = null;
        rs = stmt.executeQuery("SELECT * FROM access_event where requestURI='" + uri + "'");
        int count = 0;
        while (rs.next()) {
            count++;
        }
        assertEquals(10, count);

        rs.close();
        stmt.close();
    }

    private IAccessEvent createAccessEvent() {
        return createAccessEvent("");
    }

    private IAccessEvent createAccessEvent(String uri) {
        DummyRequest request = new DummyRequest();
        request.setRequestUri(uri);
        DummyResponse response = new DummyResponse();
        DummyServerAdapter adapter = new DummyServerAdapter(request, response);

        return new AccessEvent(request, response, adapter);
    }
}
