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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ch.qos.logback.classic.db.dialect.DBUtil;
import ch.qos.logback.classic.db.dialect.SQLDialect;
import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableInformation;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

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
public class DBAppender extends AppenderBase {
  static final String insertPropertiesSQL = "INSERT INTO  logging_event_property (event_id, mapped_key, mapped_value) VALUES (?, ?, ?)";
  static final String insertExceptionSQL = "INSERT INTO  logging_event_exception (event_id, i, trace_line) VALUES (?, ?, ?)";
  static final String insertSQL;
  private static final Method GET_GENERATED_KEYS_METHOD;

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

  ConnectionSource connectionSource;
  boolean cnxSupportsGetGeneratedKeys = false;
  boolean cnxSupportsBatchUpdates = false;
  SQLDialect sqlDialect;

  public DBAppender() {
  }

  @Override
  public void start() {

    if (connectionSource == null) {
      throw new IllegalStateException(
          "DBAppender cannot function without a connection source");
    }

    sqlDialect = DBUtil
        .getDialectFromCode(connectionSource.getSQLDialectCode());
    if (GET_GENERATED_KEYS_METHOD != null) {
      cnxSupportsGetGeneratedKeys = connectionSource.supportsGetGeneratedKeys();
    } else {
      cnxSupportsGetGeneratedKeys = false;
    }
    cnxSupportsBatchUpdates = connectionSource.supportsBatchUpdates();
    if (!cnxSupportsGetGeneratedKeys && (sqlDialect == null)) {
      throw new IllegalStateException(
          "DBAppender cannot function if the JDBC driver does not support getGeneratedKeys method *and* without a specific SQL dialect");
    }

    // all nice and dandy on the eastern front
    super.start();
  }

  /**
   * @return Returns the connectionSource.
   */
  public ConnectionSource getConnectionSource() {
    return connectionSource;
  }

  /**
   * @param connectionSource
   *          The connectionSource to set.
   */
  public void setConnectionSource(ConnectionSource connectionSource) {
    this.connectionSource = connectionSource;
  }

  @Override
  protected void append(Object eventObject) {
    LoggingEvent event = (LoggingEvent) eventObject;
    Connection connection = null;
    try {
      connection = connectionSource.getConnection();
      connection.setAutoCommit(false);

      PreparedStatement insertStatement = connection
          .prepareStatement(insertSQL);

      addLoggingEvent(insertStatement, event);
      // This is very expensive... should we do it every time?
      addCallerData(insertStatement, event.getCallerData());

      int updateCount = insertStatement.executeUpdate();
      if (updateCount != 1) {
        addWarn("Failed to insert loggingEvent");
      }

      int eventId = getEventId(insertStatement, connection);

      // we no longer need the insertStatement
      if (insertStatement != null) {
        insertStatement.close();
        insertStatement = null;
      }

      Map<String, String> mergedMap = mergePropertyMaps(event);
      insertProperties(mergedMap, connection, eventId);

      insertThrowable(event.getThrowableInformation(), connection, eventId);

      connection.commit();
    } catch (Throwable sqle) {
      addError("problem appending event", sqle);
    } finally {
      DBHelper.closeConnection(connection);
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

  int getEventId(PreparedStatement insertStatement, Connection connection)
      throws SQLException, InvocationTargetException {
    ResultSet rs = null;
    Statement idStatement = null;
    boolean gotGeneratedKeys = false;
    if (cnxSupportsGetGeneratedKeys) {
      try {
        rs = (ResultSet) GET_GENERATED_KEYS_METHOD.invoke(insertStatement,
            (Object[]) null);
        gotGeneratedKeys = true;
      } catch (InvocationTargetException ex) {
        Throwable target = ex.getTargetException();
        if (target instanceof SQLException) {
          throw (SQLException) target;
        }
        throw ex;
      } catch (IllegalAccessException ex) {
        addWarn(
            "IllegalAccessException invoking PreparedStatement.getGeneratedKeys",
            ex);
      }
    }

    if (!gotGeneratedKeys) {
      insertStatement.close();
      insertStatement = null;

      idStatement = connection.createStatement();
      idStatement.setMaxRows(1);
      rs = idStatement.executeQuery(sqlDialect.getSelectInsertId());
    }

    // A ResultSet cursor is initially positioned before the first row;
    // the
    // first call to the method next makes the first row the current row
    rs.next();
    int eventId = rs.getInt(1);

    rs.close();

    if (idStatement != null) {
      idStatement.close();
      idStatement = null;
    }

    return eventId;
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

  void insertProperties(Map<String, String> mergedMap, Connection connection,
      int eventId) throws SQLException {
    Set propertiesKeys = mergedMap.keySet();
    if (propertiesKeys.size() > 0) {
      PreparedStatement insertPropertiesStatement = connection
          .prepareStatement(insertPropertiesSQL);

      for (Iterator i = propertiesKeys.iterator(); i.hasNext();) {
        String key = (String) i.next();
        String value = (String) mergedMap.get(key);

        insertPropertiesStatement.setInt(1, eventId);
        insertPropertiesStatement.setString(2, key);
        insertPropertiesStatement.setString(3, value);

        if (cnxSupportsBatchUpdates) {
          insertPropertiesStatement.addBatch();
        } else {
          insertPropertiesStatement.execute();
        }
      }

      if (cnxSupportsBatchUpdates) {
        insertPropertiesStatement.executeBatch();
      }

      insertPropertiesStatement.close();
      insertPropertiesStatement = null;
    }
  }

  void insertThrowable(ThrowableInformation ti, Connection connection,
      int eventId) throws SQLException {
    String[] strRep = null;
    if (ti != null) {
      strRep = ti.getThrowableStrRep();

      PreparedStatement insertExceptionStatement = connection
          .prepareStatement(insertExceptionSQL);

      for (short i = 0; i < strRep.length; i++) {
        insertExceptionStatement.setInt(1, eventId);
        insertExceptionStatement.setShort(2, i);
        insertExceptionStatement.setString(3, strRep[i]);
        if (cnxSupportsBatchUpdates) {
          insertExceptionStatement.addBatch();
        } else {
          insertExceptionStatement.execute();
        }
      }
      if (cnxSupportsBatchUpdates) {
        insertExceptionStatement.executeBatch();
      }
      insertExceptionStatement.close();
      insertExceptionStatement = null;
    }
  }

  @Override
  public void stop() {
    super.stop();
  }

  public Layout getLayout() {
    return null;
  }

  public void setLayout(Layout layout) {
  }

}
