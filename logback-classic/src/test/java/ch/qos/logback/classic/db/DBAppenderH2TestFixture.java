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

import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.h2.Driver;

import ch.qos.logback.core.testUtil.RandomUtil;

public class DBAppenderH2TestFixture  {

  public enum H2Mode {
    MEM, FILE, NET;
  }
  
  public static final String H2_DRIVER_CLASS = "org.h2.Driver";
  // String serverProps;
  String url = null;
  String user = "sa";
  String password = "";

  // boolean isNetwork = true;
  H2Mode mode = H2Mode.MEM;

  int diff = RandomUtil.getPositiveInt();
  
  public DBAppenderH2TestFixture() {
  }

  Connection connection;
  
  public void setUp() throws SQLException {

    switch (mode) {
    case NET:
      url = "jdbc:h2:tcp://localhost:4808/test";
      break;
    case MEM:
      url = "jdbc:h2:mem:test"+diff;
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
    org.h2.Driver driver = Driver.load();
    Properties props = new Properties();
    props.setProperty("user", user);
    props.setProperty("password", password);
    return driver.connect(url, props);
  }

  private void createTables() throws SQLException {
    assertNotNull(connection);
    StringBuffer buf = new StringBuffer();
    buf.append("CREATE TABLE logging_event (");
    buf.append("timestmp BIGINT NOT NULL,");
    buf.append("formatted_message LONGVARCHAR NOT NULL,");
    buf.append("logger_name VARCHAR(256) NOT NULL,");
    buf.append("level_string VARCHAR(256) NOT NULL,");
    buf.append("thread_name VARCHAR(256),");
    buf.append("reference_flag SMALLINT,");
    buf.append("caller_filename VARCHAR(256), ");
    buf.append("caller_class VARCHAR(256), ");
    buf.append("caller_method VARCHAR(256), ");
    buf.append("caller_line CHAR(4), ");
    buf.append("event_id INT NOT NULL IDENTITY);");
    executeQuery(connection, buf.toString());

    buf = new StringBuffer();
    buf.append("CREATE TABLE logging_event_property (");
    buf.append("event_id INT NOT NULL,");
    buf.append("mapped_key  VARCHAR(254) NOT NULL,");
    buf.append("mapped_value LONGVARCHAR,");
    buf.append("PRIMARY KEY(event_id, mapped_key),");
    buf.append("FOREIGN KEY (event_id) REFERENCES logging_event(event_id));");
    executeQuery(connection, buf.toString());

    buf = new StringBuffer();
    buf.append("CREATE TABLE logging_event_exception (");
    buf.append("event_id INT NOT NULL,");
    buf.append("i SMALLINT NOT NULL,");
    buf.append("trace_line VARCHAR(256) NOT NULL,");
    buf.append("PRIMARY KEY(event_id, i),");
    buf.append("FOREIGN KEY (event_id) REFERENCES logging_event(event_id));");
    executeQuery(connection, buf.toString());
  }

  private  void dropTables() throws SQLException {
    StringBuffer buf = new StringBuffer();
    buf.append("DROP TABLE logging_event_exception IF EXISTS;");
    executeQuery(connection, buf.toString());

    buf = new StringBuffer();
    buf.append("DROP TABLE logging_event_property IF EXISTS;");
    executeQuery(connection, buf.toString());

    buf = new StringBuffer();
    buf.append("DROP TABLE logging_event IF EXISTS;");
    executeQuery(connection, buf.toString());
  }

  private  void executeQuery(Connection conn, String expression) throws SQLException {
    Statement st = null;
    st = conn.createStatement();
    int i = st.executeUpdate(expression);
    if (i == -1) {
      System.out.println("db error : " + expression);
    }
    st.close();
  }


}
