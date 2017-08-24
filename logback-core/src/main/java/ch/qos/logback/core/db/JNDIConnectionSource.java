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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

// PortableRemoteObject was introduced in JDK 1.3. We won't use it.
// import javax.rmi.PortableRemoteObject;
import javax.sql.DataSource;

/**
 * The <id>JNDIConnectionSource</id> is an implementation of
 * {@link ConnectionSource} that obtains a {@link javax.sql.DataSource} from a
 * JNDI provider and uses it to obtain a {@link java.sql.Connection}. It is
 * primarily designed to be used inside of J2EE application servers or
 * application server clients, assuming the application server supports remote
 * access of {@link javax.sql.DataSource}s. In this way one can take advantage
 * of connection pooling and whatever other goodies the application server
 * provides.
 * <p>
 * For more information about this component, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#DBAppender
 * 
 * @author <a href="mailto:rdecampo@twcny.rr.com">Ray DeCampo</a>
 */
public class JNDIConnectionSource extends ConnectionSourceBase {
    private String jndiLocation = null;
    private DataSource dataSource = null;

    public void start() {
        if (jndiLocation == null) {
            addError("No JNDI location specified for JNDIConnectionSource.");
        }
        discoverConnectionProperties();
    }

    public Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
            if (dataSource == null) {
                dataSource = lookupDataSource();
            }
            if (getUser() != null) {
                addWarn("Ignoring property [user] with value [" + getUser() + "] for obtaining a connection from a DataSource.");
            }
            conn = dataSource.getConnection();
        } catch (final NamingException ne) {
            addError("Error while getting data source", ne);
            throw new SQLException("NamingException while looking up DataSource: " + ne.getMessage());
        } catch (final ClassCastException cce) {
            addError("ClassCastException while looking up DataSource.", cce);
            throw new SQLException("ClassCastException while looking up DataSource: " + cce.getMessage());
        }

        return conn;
    }

    /**
     * Returns the jndiLocation.
     * 
     * @return String
     */
    public String getJndiLocation() {
        return jndiLocation;
    }

    /**
     * Sets the jndiLocation.
     * 
     * @param jndiLocation
     *          The jndiLocation to set
     */
    public void setJndiLocation(String jndiLocation) {
        this.jndiLocation = jndiLocation;
    }

    private DataSource lookupDataSource() throws NamingException, SQLException {
        addInfo("Looking up [" + jndiLocation + "] in JNDI");
        DataSource ds;
        Context initialContext = new InitialContext();
        Object obj = initialContext.lookup(jndiLocation);

        // PortableRemoteObject was introduced in JDK 1.3. We won't use it.
        // ds = (DataSource)PortableRemoteObject.narrow(obj, DataSource.class);
        ds = (DataSource) obj;

        if (ds == null) {
            throw new SQLException("Failed to obtain data source from JNDI location " + jndiLocation);
        } else {
            return ds;
        }
    }
}
