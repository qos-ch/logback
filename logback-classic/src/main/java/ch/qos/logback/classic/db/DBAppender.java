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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.db.DBAppenderBase;

/**
 * The DBAppender inserts logging events into three database tables in a format
 * independent of the Java programming language. 
 * 
 * For more information about this appender, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#DBAppender
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Ray DeCampo
 * @author S&eacute;bastien Pennec
 */
public class DBAppender extends DBAppenderBase<ILoggingEvent> {
  protected final String insertPropertiesSQL = "INSERT INTO  logging_event_property (event_id, mapped_key, mapped_value) VALUES (?, ?, ?)";
  protected final String insertExceptionSQL = "INSERT INTO  logging_event_exception (event_id, i, trace_line) VALUES (?, ?, ?)";
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
    
    // PreparedStatement.getGeneratedKeys() method was added in JDK 1.4
    Method getGeneratedKeysMethod;
    try {
      // the 
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
    ILoggingEvent event = (ILoggingEvent) eventObject;

    bindLoggingEventWithInsertStatement(insertStatement, event);
    // This is expensive... should we do it every time?
    bindCallerDataWithPreparedStatement(insertStatement, event.getCallerData());

    int updateCount = insertStatement.executeUpdate();
    if (updateCount != 1) {
      addWarn("Failed to insert loggingEvent");
    }

    int eventId = selectEventId(insertStatement, connection);

    Map<String, String> mergedMap = mergePropertyMaps(event);
    insertProperties(mergedMap, connection, eventId);

    if (event.getThrowableProxy() != null) {
      insertThrowable(event.getThrowableProxy().getStackTraceElementProxyArray(), connection, eventId);
    }
  }

  void bindLoggingEventWithInsertStatement(PreparedStatement stmt, ILoggingEvent event)
      throws SQLException {
    stmt.setLong(1, event.getTimeStamp());
    stmt.setString(2, event.getFormattedMessage());
    stmt.setString(3, event.getLoggerName());
    stmt.setString(4, event.getLevel().toString());
    stmt.setString(5, event.getThreadName());
    stmt.setShort(6, DBHelper.computeReferenceMask(event));
  }

  void bindCallerDataWithPreparedStatement(PreparedStatement stmt, StackTraceElement[] callerDataArray)
      throws SQLException {
    StackTraceElement callerData = callerDataArray[0];
    if (callerData != null) {
      stmt.setString(7, callerData.getFileName());
      stmt.setString(8, callerData.getClassName());
      stmt.setString(9, callerData.getMethodName());
      stmt.setString(10, Integer.toString(callerData.getLineNumber()));
    }
  }

  Map<String, String> mergePropertyMaps(ILoggingEvent event) {
    Map<String, String> mergedMap = new HashMap<String, String>();
    // we add the context properties first, then the event properties, since
    // we consider that event-specific properties should have priority over
    // context-wide
    // properties.
    Map<String, String> loggerContextMap = event.getLoggerContextVO().getPropertyMap();
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
  protected String getInsertSQL() {
    return insertSQL;
  }
  
  protected void insertProperties(Map<String, String> mergedMap,
      Connection connection, int eventId) throws SQLException {
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
  
  protected void insertThrowable(StackTraceElementProxy[] stepArray, Connection connection,
      int eventId) throws SQLException {

    PreparedStatement insertExceptionStatement = connection
        .prepareStatement(insertExceptionSQL);

    for (short i = 0; i < stepArray.length; i++) {
      insertExceptionStatement.setInt(1, eventId);
      insertExceptionStatement.setShort(2, i);
      insertExceptionStatement.setString(3, stepArray[i].toString());
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
