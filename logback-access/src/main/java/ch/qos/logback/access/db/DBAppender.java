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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Enumeration;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.db.DBAppenderBase;

/**
 * The DBAppender inserts access events into three database tables in a format
 * independent of the Java programming language. 
 * 
 * For more information about this appender, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#AccessDBAppender
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Ray DeCampo
 * @author S&eacute;bastien Pennec
 */
public class DBAppender extends DBAppenderBase<IAccessEvent> {
    protected static final String insertSQL;
    protected final String insertHeaderSQL = "INSERT INTO  access_event_header (event_id, header_key, header_value) VALUES (?, ?, ?)";
    protected static final Method GET_GENERATED_KEYS_METHOD;

    private boolean insertHeaders = false;

    static {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO access_event (");
        sql.append("timestmp, ");
        sql.append("requestURI, ");
        sql.append("requestURL, ");
        sql.append("remoteHost, ");
        sql.append("remoteUser, ");
        sql.append("remoteAddr, ");
        sql.append("protocol, ");
        sql.append("method, ");
        sql.append("serverName, ");
        sql.append("postContent) ");
        sql.append(" VALUES (?, ?, ? ,?, ?, ?, ?, ?, ?, ?)");
        insertSQL = sql.toString();

        Method getGeneratedKeysMethod;
        try {
            getGeneratedKeysMethod = PreparedStatement.class.getMethod("getGeneratedKeys", (Class[]) null);
        } catch (Exception ex) {
            getGeneratedKeysMethod = null;
        }
        GET_GENERATED_KEYS_METHOD = getGeneratedKeysMethod;
    }

    @Override
    protected void subAppend(IAccessEvent event, Connection connection, PreparedStatement insertStatement) throws Throwable {

        addAccessEvent(insertStatement, event);

        int updateCount = insertStatement.executeUpdate();
        if (updateCount != 1) {
            addWarn("Failed to insert access event");
        }
    }

    @Override
    protected void secondarySubAppend(IAccessEvent event, Connection connection, long eventId) throws Throwable {
        if (insertHeaders) {
            addRequestHeaders(event, connection, eventId);
        }
    }

    void addAccessEvent(PreparedStatement stmt, IAccessEvent event) throws SQLException {
        stmt.setLong(1, event.getTimeStamp());
        stmt.setString(2, event.getRequestURI());
        stmt.setString(3, event.getRequestURL());
        stmt.setString(4, event.getRemoteHost());
        stmt.setString(5, event.getRemoteUser());
        stmt.setString(6, event.getRemoteAddr());
        stmt.setString(7, event.getProtocol());
        stmt.setString(8, event.getMethod());
        stmt.setString(9, event.getServerName());
        stmt.setString(10, event.getRequestContent());
    }

    void addRequestHeaders(IAccessEvent event, Connection connection, long eventId) throws SQLException {
        Enumeration<String> names = event.getRequestHeaderNames();
        if (names.hasMoreElements()) {
            PreparedStatement insertHeaderStatement = connection.prepareStatement(insertHeaderSQL);

            while (names.hasMoreElements()) {
                String key = (String) names.nextElement();
                String value = (String) event.getRequestHeader(key);

                insertHeaderStatement.setLong(1, eventId);
                insertHeaderStatement.setString(2, key);
                insertHeaderStatement.setString(3, value);

                if (cnxSupportsBatchUpdates) {
                    insertHeaderStatement.addBatch();
                } else {
                    insertHeaderStatement.execute();
                }
            }

            if (cnxSupportsBatchUpdates) {
                insertHeaderStatement.executeBatch();
            }

            insertHeaderStatement.close();
        }
    }

    @Override
    protected Method getGeneratedKeysMethod() {
        return GET_GENERATED_KEYS_METHOD;
    }

    @Override
    protected String getInsertSQL() {
        return insertSQL;
    }

    public void setInsertHeaders(boolean insertHeaders) {
        this.insertHeaders = insertHeaders;
    }
}
