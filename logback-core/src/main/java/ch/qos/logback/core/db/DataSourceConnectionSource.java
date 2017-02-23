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
import java.sql.SQLException;

import javax.sql.DataSource;

import ch.qos.logback.core.db.dialect.SQLDialectCode;

/**
 * The DataSourceConnectionSource is an implementation of
 * {@link ConnectionSource} that obtains the Connection in the recommended JDBC
 * manner based on a {@link javax.sql.DataSource DataSource}.
 * <p>
 * 
 * For more information about this component, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#DBAppender
 * 
 * @author Ray DeCampo
 * @author Ceki G&uuml;lc&uuml;
 */
public class DataSourceConnectionSource extends ConnectionSourceBase {

    private DataSource dataSource;

    @Override
    public void start() {
        if (dataSource == null) {
            addWarn("WARNING: No data source specified");
        } else {
            discoverConnectionProperties();
            if (!supportsGetGeneratedKeys() && getSQLDialectCode() == SQLDialectCode.UNKNOWN_DIALECT) {
                addWarn("Connection does not support GetGeneratedKey method and could not discover the dialect.");
            }
        }
        super.start();
    }

    /**
     * @see ch.qos.logback.core.db.ConnectionSource#getConnection()
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            addError("WARNING: No data source specified");
            return null;
        }

        if (getUser() == null) {
            return dataSource.getConnection();
        } else {
            return dataSource.getConnection(getUser(), getPassword());
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
