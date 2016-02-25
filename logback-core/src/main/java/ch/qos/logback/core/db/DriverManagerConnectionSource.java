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
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The DriverManagerConnectionSource is an implementation of
 * {@link ConnectionSource} that obtains the Connection in the traditional JDBC
 * manner based on the connection URL.
 * <p>
 * For more information about this component, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#DBAppender
 * 
 * @author <a href="mailto:rdecampo@twcny.rr.com">Ray DeCampo</a>
 */
public class DriverManagerConnectionSource extends ConnectionSourceBase {
    private String driverClass = null;
    private String url = null;

    public void start() {
        try {
            if (driverClass != null) {
                Class.forName(driverClass);
                discoverConnectionProperties();
            } else {
                addError("WARNING: No JDBC driver specified for logback DriverManagerConnectionSource.");
            }
        } catch (final ClassNotFoundException cnfe) {
            addError("Could not load JDBC driver class: " + driverClass, cnfe);
        }
    }

    /**
     * @see ch.qos.logback.core.db.ConnectionSource#getConnection()
     */
    public Connection getConnection() throws SQLException {
        if (getUser() == null) {
            return DriverManager.getConnection(url);
        } else {
            return DriverManager.getConnection(url, getUser(), getPassword());
        }
    }

    /**
     * Returns the url.
     * 
     * @return String
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the url.
     * 
     * @param url
     *          The url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the name of the driver class.
     * 
     * @return String
     */
    public String getDriverClass() {
        return driverClass;
    }

    /**
     * Sets the driver class.
     * 
     * @param driverClass
     *          The driver class to set
     */
    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }
}
