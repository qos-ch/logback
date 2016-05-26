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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.hsqldb.Server;
import org.hsqldb.ServerConstants;
import org.hsqldb.jdbcDriver;

public class DBAppenderHSQLTestFixture {

    public static final String HSQLDB_DRIVER_CLASS = "org.hsqldb.jdbcDriver";
    // String serverProps;
    String url = null;
    String user = "sa";
    String password = "";
    Server server;

    // boolean isNetwork = true;
    HsqlMode mode = HsqlMode.MEM;

    public void setUp() throws SQLException {

        switch (mode) {
        case NET:
            url = "jdbc:hsqldb:hsql://localhost:4808/test";
            break;
        case MEM:
            url = "jdbc:hsqldb:mem:test;sql.enforce_strict_size=true";
            server = new Server();
            server.setDatabaseName(0, "test");
            server.setDatabasePath(0, url);
            server.setLogWriter(new PrintWriter(System.out));
            server.setErrWriter(new PrintWriter(System.out));
            server.setTrace(false);
            server.setSilent(false);
            server.start();

            break;
        case FILE:
            url = "jdbc:hsqldb:file:test;sql.enforce_strict_size=true";
            break;

        }

        // try {
        // Class.forName(DRIVER_CLASS);
        // } catch (Exception e) {
        // e.printStackTrace();
        // System.out.println(this + ".setUp() error: " + e.getMessage());
        // }
        // Thread.yield();
        System.out.println(server.getState());

        int waitCount = 0;
        while (server.getState() != ServerConstants.SERVER_STATE_ONLINE && waitCount < 5) {
            try {
                waitCount++;
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }
        createTables();
    }

    public void tearDown() throws SQLException {
        dropTables();

        if (mode == HsqlMode.MEM) {
            server.stop();
            server = null;
        }
    }

    Connection newConnection() throws SQLException {
        jdbcDriver driver = new jdbcDriver();
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        return driver.connect(url, props);

        // return DriverManager.getConnection(url, user, password);
    }

    private void createTables() throws SQLException {
        Connection conn = newConnection();
        assertNotNull(conn);
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
        buf.append("EVENT_ID BIGINT NOT NULL IDENTITY);");
        query(conn, buf.toString());

        buf = new StringBuilder();
        buf.append("CREATE TABLE LOGGING_EVENT_PROPERTY (");
        buf.append("EVENT_ID BIGINT NOT NULL,");
        buf.append("MAPPED_KEY  VARCHAR(254) NOT NULL,");
        buf.append("MAPPED_VALUE LONGVARCHAR,");
        buf.append("PRIMARY KEY(EVENT_ID, MAPPED_KEY),");
        buf.append("FOREIGN KEY (EVENT_ID) REFERENCES LOGGING_EVENT(EVENT_ID));");
        query(conn, buf.toString());

        buf = new StringBuilder();
        buf.append("CREATE TABLE LOGGING_EVENT_EXCEPTION (");
        buf.append("EVENT_ID BIGINT NOT NULL,");
        buf.append("I SMALLINT NOT NULL,");
        buf.append("TRACE_LINE VARCHAR(256) NOT NULL,");
        buf.append("PRIMARY KEY(EVENT_ID, I),");
        buf.append("FOREIGN KEY (EVENT_ID) REFERENCES LOGGING_EVENT(EVENT_ID));");
        query(conn, buf.toString());
    }

    private void dropTables() throws SQLException {
        Connection conn = newConnection();
        StringBuilder buf = new StringBuilder();
        buf.append("DROP TABLE LOGGING_EVENT_EXCEPTION IF EXISTS;");
        query(conn, buf.toString());

        buf = new StringBuilder();
        buf.append("DROP TABLE LOGGING_EVENT_PROPERTY IF EXISTS;");
        query(conn, buf.toString());

        buf = new StringBuilder();
        buf.append("DROP TABLE LOGGING_EVENT IF EXISTS;");
        query(conn, buf.toString());
    }

    private void query(Connection conn, String expression) throws SQLException {

        Statement st = null;
        st = conn.createStatement();
        int i = st.executeUpdate(expression);
        if (i == -1) {
            System.out.println("db error : " + expression);
        }
        st.close();
    }

    public enum HsqlMode {
        MEM, FILE, NET;
    }
}
