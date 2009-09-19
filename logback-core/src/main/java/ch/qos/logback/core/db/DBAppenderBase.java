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
package ch.qos.logback.core.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.db.dialect.DBUtil;
import ch.qos.logback.core.db.dialect.SQLDialect;

/**
 * @author Ceki G&uuml;lc&uuml;
 * @author Ray DeCampo
 * @author S&eacute;bastien Pennec
 */
public abstract class DBAppenderBase<E> extends AppenderBase<E> {

  protected ConnectionSource connectionSource;
  protected boolean cnxSupportsGetGeneratedKeys = false;
  protected boolean cnxSupportsBatchUpdates = false;
  protected SQLDialect sqlDialect;

  protected abstract Method getGeneratedKeysMethod();

  protected abstract String getInsertSQL();

  @Override
  public void start() {

    if (connectionSource == null) {
      throw new IllegalStateException(
          "DBAppender cannot function without a connection source");
    }

    System.out.println(connectionSource.supportsGetGeneratedKeys());
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
   *                The connectionSource to set.
   */
  public void setConnectionSource(ConnectionSource connectionSource) {
    this.connectionSource = connectionSource;
  }

  @Override
  public void append(E eventObject) {
    Connection connection = null;
    try {
      connection = connectionSource.getConnection();
      connection.setAutoCommit(false);
      PreparedStatement insertStatement;
      if (cnxSupportsGetGeneratedKeys) {
        insertStatement = connection.prepareStatement(getInsertSQL(),
            new String[] {"EVENT_ID"});
      } else {
        insertStatement = connection.prepareStatement(getInsertSQL());
      }
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

  protected int selectEventId(PreparedStatement insertStatement,
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
    // the first call to the method next makes the first row the current row
    rs.next();
    int eventId = rs.getInt(1);

    rs.close();

    if (idStatement != null) {
      idStatement.close();
      idStatement = null;
    }

    return eventId;
  }

  @Override
  public void stop() {
    super.stop();
  }
}
