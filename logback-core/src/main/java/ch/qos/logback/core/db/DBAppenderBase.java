/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.db.dialect.DBUtil;
import ch.qos.logback.core.db.dialect.SQLDialect;

/**
 * @author Ceki G&uuml;lc&uuml;
 * @author Ray DeCampo
 * @author S&eacute;bastien Pennec
 */
public abstract class DBAppenderBase extends AppenderBase {

  protected ConnectionSource connectionSource;
  protected boolean cnxSupportsGetGeneratedKeys = false;
  protected boolean cnxSupportsBatchUpdates = false;
  protected SQLDialect sqlDialect;

  protected abstract Method getGeneratedKeysMethod();
  protected abstract String getInsertSQL();
  protected abstract String getInsertPropertiesSQL();
  protected abstract String getInsertExceptionSQL();
  
  @Override
  public void start() {

    if (connectionSource == null) {
      throw new IllegalStateException(
          "DBAppender cannot function without a connection source");
    }

    sqlDialect = DBUtil
        .getDialectFromCode(connectionSource.getSQLDialectCode());
    if (getGeneratedKeysMethod() != null) {
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
  public void append(Object eventObject) {
    Connection connection = null;
    try {
      connection = connectionSource.getConnection();
      connection.setAutoCommit(false);

      PreparedStatement insertStatement = connection
          .prepareStatement(getInsertSQL());

      subAppend(eventObject, connection, insertStatement);

      // we no longer need the insertStatement
      if (insertStatement != null) {
        insertStatement.close();
        insertStatement = null;
      }

      connection.commit();
    } catch (Throwable sqle) {
      addError("problem appending event", sqle);
    } finally {
      DBHelper.closeConnection(connection);
    }
  }

  protected abstract void subAppend(Object eventObject, Connection connection,
      PreparedStatement statement) throws Throwable;

  protected int getEventId(PreparedStatement insertStatement,
      Connection connection) throws SQLException, InvocationTargetException {
    ResultSet rs = null;
    Statement idStatement = null;
    boolean gotGeneratedKeys = false;
    if (cnxSupportsGetGeneratedKeys) {
      try {
        rs = (ResultSet) getGeneratedKeysMethod().invoke(insertStatement,
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

  protected void insertProperties(Map<String, String> mergedMap,
      Connection connection, int eventId) throws SQLException {
    Set propertiesKeys = mergedMap.keySet();
    if (propertiesKeys.size() > 0) {
      PreparedStatement insertPropertiesStatement = connection
          .prepareStatement(getInsertPropertiesSQL());

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

  protected void insertThrowable(String[] strRep, Connection connection,
      int eventId) throws SQLException {

    PreparedStatement insertExceptionStatement = connection
        .prepareStatement(getInsertExceptionSQL());

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
