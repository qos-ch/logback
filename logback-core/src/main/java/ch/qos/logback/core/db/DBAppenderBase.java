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
package ch.qos.logback.core.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.db.dialect.DBUtil;
import ch.qos.logback.core.db.dialect.SQLDialect;
import ch.qos.logback.core.db.dialect.SQLDialectCode;

/**
 * @author Ceki G&uuml;lc&uuml;
 * @author Ray DeCampo
 * @author S&eacute;bastien Pennec
 */
public abstract class DBAppenderBase<E> extends UnsynchronizedAppenderBase<E> {

    protected ConnectionSource connectionSource;
    protected boolean cnxSupportsGetGeneratedKeys = false;
    protected boolean cnxSupportsBatchUpdates = false;
    protected SQLDialect sqlDialect;

    protected abstract Method getGeneratedKeysMethod();

    protected abstract String getInsertSQL();

    @Override
    public void start() {

        if (connectionSource == null) {
            throw new IllegalStateException("DBAppender cannot function without a connection source");
        }

        sqlDialect = DBUtil.getDialectFromCode(connectionSource.getSQLDialectCode());
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
    public void append(E eventObject) {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            connection = connectionSource.getConnection();
            connection.setAutoCommit(false);

            if (cnxSupportsGetGeneratedKeys) {
                String EVENT_ID_COL_NAME = "EVENT_ID";
                // see
                if (connectionSource.getSQLDialectCode() == SQLDialectCode.POSTGRES_DIALECT) {
                    EVENT_ID_COL_NAME = EVENT_ID_COL_NAME.toLowerCase();
                }
                insertStatement = connection.prepareStatement(getInsertSQL(), new String[] { EVENT_ID_COL_NAME });
            } else {
                insertStatement = connection.prepareStatement(getInsertSQL());
            }

            long eventId;
            // inserting an event and getting the result must be exclusive
            synchronized (this) {
                subAppend(eventObject, connection, insertStatement);
                eventId = selectEventId(insertStatement, connection);
            }
            secondarySubAppend(eventObject, connection, eventId);

            connection.commit();
        } catch (Throwable sqle) {
            addError("problem appending event", sqle);
        } finally {
            DBHelper.closeStatement(insertStatement);
            DBHelper.closeConnection(connection);
        }
    }

    protected abstract void subAppend(E eventObject, Connection connection, PreparedStatement statement) throws Throwable;

    protected abstract void secondarySubAppend(E eventObject, Connection connection, long eventId) throws Throwable;

    @SuppressWarnings("resource")
    protected long selectEventId(PreparedStatement insertStatement, Connection connection) throws SQLException, InvocationTargetException {
        ResultSet rs = null;
        Statement idStatement = null;
        try {
            boolean gotGeneratedKeys = false;
            if (cnxSupportsGetGeneratedKeys) {
                try {
                    rs = (ResultSet) getGeneratedKeysMethod().invoke(insertStatement, (Object[]) null);
                    gotGeneratedKeys = true;
                } catch (InvocationTargetException ex) {
                    Throwable target = ex.getTargetException();
                    if (target instanceof SQLException) {
                        throw (SQLException) target;
                    }
                    throw ex;
                } catch (IllegalAccessException ex) {
                    addWarn("IllegalAccessException invoking PreparedStatement.getGeneratedKeys", ex);
                }
            }

            if (!gotGeneratedKeys) {
                idStatement = connection.createStatement();
                idStatement.setMaxRows(1);
                String selectInsertIdStr = sqlDialect.getSelectInsertId();
                rs = idStatement.executeQuery(selectInsertIdStr);
            }

            // A ResultSet cursor is initially positioned before the first row;
            // the first call to the method next makes the first row the current row
            rs.next();
            long eventId = rs.getLong(1);
            return eventId;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            DBHelper.closeStatement(idStatement);
        }
    }

    @Override
    public void stop() {
        super.stop();
    }
}
