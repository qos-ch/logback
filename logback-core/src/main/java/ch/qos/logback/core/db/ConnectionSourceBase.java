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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import ch.qos.logback.core.db.dialect.DBUtil;
import ch.qos.logback.core.db.dialect.SQLDialectCode;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public abstract class ConnectionSourceBase extends ContextAwareBase implements ConnectionSource {

    private boolean started;

    private String user = null;
    private String password = null;

    // initially we have an unknown dialect
    private SQLDialectCode dialectCode = SQLDialectCode.UNKNOWN_DIALECT;
    private boolean supportsGetGeneratedKeys = false;
    private boolean supportsBatchUpdates = false;

    /**
     * Learn relevant information about this connection source.
     *
     */
    public void discoverConnectionProperties() {
        Connection connection = null;
        try {
            connection = getConnection();
            if (connection == null) {
                addWarn("Could not get a connection");
                return;
            }
            DatabaseMetaData meta = connection.getMetaData();
            DBUtil util = new DBUtil();
            util.setContext(getContext());
            supportsGetGeneratedKeys = util.supportsGetGeneratedKeys(meta);
            supportsBatchUpdates = util.supportsBatchUpdates(meta);
            dialectCode = DBUtil.discoverSQLDialect(meta);
            addInfo("Driver name=" + meta.getDriverName());
            addInfo("Driver version=" + meta.getDriverVersion());
            addInfo("supportsGetGeneratedKeys=" + supportsGetGeneratedKeys);

        } catch (SQLException se) {
            addWarn("Could not discover the dialect to use.", se);
        } finally {
            DBHelper.closeConnection(connection);
        }
    }

    /**
     * Does this connection support the JDBC Connection.getGeneratedKeys method?
     */
    public final boolean supportsGetGeneratedKeys() {
        return supportsGetGeneratedKeys;
    }

    public final SQLDialectCode getSQLDialectCode() {
        return dialectCode;
    }

    /**
     * Get the password for this connection source.
     */
    public final String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     * @param password The password to set
     */
    public final void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Get the user for this connection source.
     */
    public final String getUser() {
        return user;
    }

    /**
     * Sets the username.
     * @param username The username to set
     */
    public final void setUser(final String username) {
        this.user = username;
    }

    /**
     * Does this connection support batch updates?
     */
    public final boolean supportsBatchUpdates() {
        return supportsBatchUpdates;
    }

    public boolean isStarted() {
        return started;
    }

    public void start() {
        started = true;
    }

    public void stop() {
        started = false;
    }

}
