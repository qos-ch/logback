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

import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.h2.Driver;

import ch.qos.logback.core.testUtil.RandomUtil;

public class DBAppenderH2TestFixture {

    public enum H2Mode {
        MEM, FILE, NET;
    }

    public static final String H2_DRIVER_CLASS = "org.h2.Driver";
    String url = null;
    String user = "sa";
    String password = "";

    // boolean isNetwork = true;
    H2Mode mode = H2Mode.MEM;

    int diff = RandomUtil.getPositiveInt();

    Connection connection;

    public void setUp() throws SQLException {

        switch (mode) {
        case NET:
            url = "jdbc:h2:tcp://localhost:4808/test";
            break;
        case MEM:
            url = "jdbc:h2:mem:test" + diff;
            break;
        case FILE:
            url = "jdbc:hsqldb:file:test;sql.enforce_strict_size=true";
            break;

        }
        connection = newConnection();
        createTables();
    }

    public void tearDown() throws SQLException {
        dropTables();
        connection.close();
    }

    Connection newConnection() throws SQLException {
        System.out.println("url=" + url);
        org.h2.Driver driver = Driver.load();
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        return driver.connect(url, props);
    }

    private void createTables() throws SQLException {
        assertNotNull(connection);
        StringBuilder buf = new StringBuilder();
        buf.append("CREATE TABLE LOGGING_EVENT (");
        buf.append("TIMESTMP BIGINT NOT NULL,");
        buf.append("FORMATTED_MESSAGE LONGVARCHAR NOT NULL,");
        buf.append("LOGGER_NAME VARCHAR(256) NOT NULL,");
        buf.append("LEVEL_STRING VARCHAR(256) NOT NULL,");
        buf.append("THREAD_NAME VARCHAR(256),");
        buf.append("REFERENCE_FLAG SMALLINT,");
        buf.append("ARG0 VARCHAR(256),");
        buf.append("ARG1 VARCHAR(256),");
        buf.append("ARG2 VARCHAR(256),");
        buf.append("ARG3 VARCHAR(256),");
        buf.append("CALLER_FILENAME VARCHAR(256), ");
        buf.append("CALLER_CLASS VARCHAR(256), ");
        buf.append("CALLER_METHOD VARCHAR(256), ");
        buf.append("CALLER_LINE CHAR(4), ");
        buf.append("EVENT_ID IDENTITY NOT NULL);");
        executeQuery(connection, buf.toString());

        buf = new StringBuilder();
        buf.append("CREATE TABLE LOGGING_EVENT_PROPERTY (");
        buf.append("EVENT_ID BIGINT NOT NULL,");
        buf.append("MAPPED_KEY  VARCHAR(254) NOT NULL,");
        buf.append("MAPPED_VALUE LONGVARCHAR,");
        buf.append("PRIMARY KEY(EVENT_ID, MAPPED_KEY),");
        buf.append("FOREIGN KEY (EVENT_ID) REFERENCES LOGGING_EVENT(EVENT_ID));");
        executeQuery(connection, buf.toString());

        buf = new StringBuilder();
        buf.append("CREATE TABLE LOGGING_EVENT_EXCEPTION (");
        buf.append("EVENT_ID BIGINT NOT NULL,");
        buf.append("I SMALLINT NOT NULL,");
        buf.append("TRACE_LINE VARCHAR(256) NOT NULL,");
        buf.append("PRIMARY KEY(EVENT_ID, I),");
        buf.append("FOREIGN KEY (EVENT_ID) REFERENCES LOGGING_EVENT(EVENT_ID));");
        executeQuery(connection, buf.toString());
    }

    private void dropTables() throws SQLException {
        StringBuilder buf = new StringBuilder();
        buf.append("DROP TABLE LOGGING_EVENT_EXCEPTION IF EXISTS;");
        executeQuery(connection, buf.toString());

        buf = new StringBuilder();
        buf.append("DROP TABLE LOGGING_EVENT_PROPERTY IF EXISTS;");
        executeQuery(connection, buf.toString());

        buf = new StringBuilder();
        buf.append("DROP TABLE LOGGING_EVENT IF EXISTS;");
        executeQuery(connection, buf.toString());
    }

    private void executeQuery(Connection conn, String expression) throws SQLException {
        Statement st = null;
        st = conn.createStatement();
        int i = st.executeUpdate(expression);
        if (i == -1) {
            throw new IllegalStateException("db error : " + expression);
        }
        st.close();
    }

}
