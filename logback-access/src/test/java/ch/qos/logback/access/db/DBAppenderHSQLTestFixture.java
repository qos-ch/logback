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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.hsqldb.Server;

public class DBAppenderHSQLTestFixture {

    public static final String DRIVER_CLASS = "org.hsqldb.jdbcDriver";
    String serverProps;
    String url;
    String user = "sa";
    String password = "";
    Server server;
    boolean isNetwork = true;

    void setUp() throws SQLException {
        if (isNetwork) {
            if (url == null) {
                url = "jdbc:hsqldb:hsql://localhost/test";
            }

            server = new Server();

            server.setDatabaseName(0, "test");
            server.setDatabasePath(0, "mem:test;sql.enforce_strict_size=true");
            server.setLogWriter(null);
            server.setErrWriter(null);
            server.setTrace(false);
            server.setSilent(true);
            server.start();
        } else {
            if (url == null) {
                url = "jdbc:hsqldb:file:test;sql.enforce_strict_size=true";
            }
        }

        try {
            Class.forName(DRIVER_CLASS);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(this + ".setUp() error: " + e.getMessage());
        }
        Thread.yield();

        createTables();
    }

    void tearDown() throws SQLException {
        dropTables();
        if (isNetwork) {
            server.stop();
            server = null;
        }
    }

    Connection newConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private void createTables() throws SQLException {
        Connection conn = newConnection();
        StringBuilder buf = new StringBuilder();
        buf.append("CREATE TABLE access_event (");
        buf.append("timestmp BIGINT NOT NULL,");
        buf.append("requestURI VARCHAR(254),");
        buf.append("requestURL VARCHAR(254),");
        buf.append("remoteHost VARCHAR(254),");
        buf.append("remoteUser VARCHAR(254),");
        buf.append("remoteAddr VARCHAR(254),");
        buf.append("protocol VARCHAR(254),");
        buf.append("method VARCHAR(254),");
        buf.append("serverName VARCHAR(254),");
        buf.append("postContent VARCHAR(254),");
        buf.append("event_id INT NOT NULL IDENTITY);");
        query(conn, buf.toString());

        buf = new StringBuilder();
        buf.append("CREATE TABLE access_event_header (");
        buf.append("event_id INT NOT NULL,");
        buf.append("header_key  VARCHAR(254) NOT NULL,");
        buf.append("header_value LONGVARCHAR,");
        buf.append("PRIMARY KEY(event_id, header_key),");
        buf.append("FOREIGN KEY (event_id) REFERENCES access_event(event_id));");
        query(conn, buf.toString());
    }

    private void dropTables() throws SQLException {
        Connection conn = newConnection();

        StringBuilder buf = new StringBuilder();
        buf.append("DROP TABLE access_event_header IF EXISTS;");
        query(conn, buf.toString());

        buf = new StringBuilder();
        buf.append("DROP TABLE access_event IF EXISTS;");
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
}
