/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.db;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.db.DBAppenderBase;
import ch.qos.logback.core.db.dialect.SQLDialect;

/**
 * The DBAppender inserts loggin events into three database tables in a format
 * independent of the Java programming language. The three tables that
 * DBAppender inserts to must exists before DBAppender can be used. These tables
 * may be created with the help of SQL scripts found in the
 * <em>src/main/java/ch/qos/logback/classic/db/dialect</em> directory. There
 * is a specific script for each of the most popular database systems. If the
 * script for your particular type of database system is missing, it should be
 * quite easy to write one, taking example on the already existing scripts. If
 * you send them to us, we will gladly include missing scripts in future
 * releases.
 * 
 * <p>
 * If the JDBC driver you are using supports the
 * {@link java.sql.Statement#getGeneratedKeys}method introduced in JDBC 3.0
 * specification, then you are all set. Otherwise, there must be an
 * {@link SQLDialect}appropriate for your database system. Currently, we have
 * dialects for PostgreSQL, MySQL, Oracle and MsSQL. As mentioed previously, an
 * SQLDialect is required only if the JDBC driver for your database system does
 * not support the {@link java.sql.Statement#getGeneratedKeys getGeneratedKeys}
 * method.
 * </p>
 * 
 * <table border="1" cellpadding="4">
 * <tr>
 * <th>RDBMS</th>
 * <th>supports <br/><code>getGeneratedKeys()</code> method</th>
 * <th>specific <br/>SQLDialect support</th>
 * <tr>
 * <tr>
 * <td>PostgreSQL</td>
 * <td align="center">NO</td>
 * <td>present and used</td>
 * <tr>
 * <tr>
 * <td>MySQL</td>
 * <td align="center">YES</td>
 * <td>present, but not actually needed or used</td>
 * <tr>
 * <tr>
 * <td>Oracle</td>
 * <td align="center">YES</td>
 * <td>present, but not actually needed or used</td>
 * <tr>
 * <tr>
 * <td>DB2</td>
 * <td align="center">YES</td>
 * <td>not present, and not needed or used</td>
 * <tr>
 * <tr>
 * <td>MsSQL</td>
 * <td align="center">YES</td>
 * <td>not present, and not needed or used</td>
 * <tr>
 * <tr>
 * <td>HSQL</td>
 * <td align="center">NO</td>
 * <td>present and used</td>
 * <tr>
 * 
 * </table>
 * <p>
 * <b>Performance: </b> Experiments show that writing a single event into the
 * database takes approximately 50 milliseconds, on a "standard" PC. If pooled
 * connections are used, this figure drops to under 10 milliseconds. Note that
 * most JDBC drivers already ship with connection pooling support.
 * </p>
 * 
 * 
 * 
 * <p>
 * <b>Configuration </b> DBAppender can be configured programmatically, or using
 * {@link ch.qos.logback.classic.joran.JoranConfigurator JoranConfigurator}.
 * Example scripts can be found in the <em>tests/input/db</em> directory.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Ray DeCampo
 * @author S&eacute;bastien Pennec
 */
public class DBAppender extends DBAppenderBase {
  protected static final String insertPropertiesSQL = "INSERT INTO  logging_event_property (event_id, mapped_key, mapped_value) VALUES (?, ?, ?)";
  protected static final String insertExceptionSQL = "INSERT INTO  logging_event_exception (event_id, i, trace_line) VALUES (?, ?, ?)";
  protected static final String insertSQL;
  protected static final Method GET_GENERATED_KEYS_METHOD;

  static {
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO logging_event (");
    sql.append("timestmp, ");
    sql.append("formatted_message, ");
    sql.append("logger_name, ");
    sql.append("level_string, ");
    sql.append("thread_name, ");
    sql.append("reference_flag, ");
    sql.append("caller_filename, ");
    sql.append("caller_class, ");
    sql.append("caller_method, ");
    sql.append("caller_line) ");
    sql.append(" VALUES (?, ?, ? ,?, ?, ?, ?, ?, ?,?)");
    insertSQL = sql.toString();
    //
    // PreparedStatement.getGeneratedKeys added in JDK 1.4
    //
    Method getGeneratedKeysMethod;
    try {
      getGeneratedKeysMethod = PreparedStatement.class.getMethod(
          "getGeneratedKeys", (Class[]) null);
    } catch (Exception ex) {
      getGeneratedKeysMethod = null;
    }
    GET_GENERATED_KEYS_METHOD = getGeneratedKeysMethod;
  }
  
  public DBAppender() {
  }

  @Override
  protected void subAppend(Object eventObject, Connection connection,
      PreparedStatement insertStatement) throws Throwable {
    LoggingEvent event = (LoggingEvent) eventObject;

    addLoggingEvent(insertStatement, event);
    // This is very expensive... should we do it every time?
    addCallerData(insertStatement, event.getCallerData());

    int updateCount = insertStatement.executeUpdate();
    if (updateCount != 1) {
      addWarn("Failed to insert loggingEvent");
    }

    int eventId = getEventId(insertStatement, connection);

    Map<String, String> mergedMap = mergePropertyMaps(event);
    insertProperties(mergedMap, connection, eventId);

    if (event.getThrowableInformation() != null) {
      insertThrowable(event.getThrowableInformation().getThrowableStrRep(), connection, eventId);
    }
  }

  void addLoggingEvent(PreparedStatement stmt, LoggingEvent event)
      throws SQLException {
    stmt.setLong(1, event.getTimeStamp());
    stmt.setString(2, event.getFormattedMessage());
    stmt.setString(3, event.getLoggerRemoteView().getName());
    stmt.setString(4, event.getLevel().toString());
    stmt.setString(5, event.getThreadName());
    stmt.setShort(6, DBHelper.computeReferenceMask(event));
  }

  void addCallerData(PreparedStatement stmt, CallerData[] callerDataArray)
      throws SQLException {
    CallerData callerData = callerDataArray[0];
    if (callerData != null) {
      stmt.setString(7, callerData.getFileName());
      stmt.setString(8, callerData.getClassName());
      stmt.setString(9, callerData.getMethodName());
      stmt.setString(10, Integer.toString(callerData.getLineNumber()));
    }
  }

  Map<String, String> mergePropertyMaps(LoggingEvent event) {
    Map<String, String> mergedMap = new HashMap<String, String>();
    // we add the context properties first, then the event properties, since
    // we consider that event-specific properties should have priority over
    // context-wide
    // properties.
    Map<String, String> loggerContextMap = event.getLoggerRemoteView()
        .getLoggerContextView().getPropertyMap();
    Map<String, String> mdcMap = event.getMDCPropertyMap();
    if (loggerContextMap != null) {
      mergedMap.putAll(loggerContextMap);
    }
    if (mdcMap != null) {
      mergedMap.putAll(mdcMap);
    }

    return mergedMap;
  }

  @Override
  protected Method getGeneratedKeysMethod() {
    return GET_GENERATED_KEYS_METHOD;
  }

  @Override
  protected String getInsertExceptionSQL() {
    return insertExceptionSQL;
  }

  @Override
  protected String getInsertPropertiesSQL() {
    return insertPropertiesSQL;
  }

  @Override
  protected String getInsertSQL() {
    return insertSQL;
  }
}
