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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The DriverManagerConnectionSource is an implementation of
 * {@link ConnectionSource} that obtains the Connection in the traditional JDBC
 * manner based on the connection URL.
 * <p>
 * Note that this class will establish a new Connection for each call to
 * {@link #getConnection()}. It is recommended that you either use a JDBC
 * driver that natively supported Connection pooling or that you create your own
 * implementation of {@link ConnectionSource} that taps into whatever pooling
 * mechanism you are already using. (If you have access to a JNDI implementation
 * that supports {@link javax.sql.DataSource}s, e.g. within a J2EE application
 * server, see {@link JNDIConnectionSource}). See <a href="#dbcp">below</a>
 * for a configuration example that uses the <a
 * href="http://jakarta.apache.org/commons/dbcp/index.html">commons-dbcp</a>
 * package from Apache.
 * <p>
 * Sample configuration:<br>
 * 
 * <pre>
 *      &lt;connectionSource class=&quot;org.apache.log4j.jdbc.DriverManagerConnectionSource&quot;&gt;
 *         &lt;param name=&quot;driver&quot; value=&quot;com.mysql.jdbc.Driver&quot; /&gt;
 *         &lt;param name=&quot;url&quot; value=&quot;jdbc:mysql://localhost:3306/mydb&quot; /&gt;
 *         &lt;param name=&quot;username&quot; value=&quot;myUser&quot; /&gt;
 *         &lt;param name=&quot;password&quot; value=&quot;myPassword&quot; /&gt;
 *      &lt;/connectionSource&gt;
 * </pre>
 * 
 * <p>
 * <a name="dbcp">If</a> you do not have another connection pooling mechanism
 * built into your application, you can use the <a
 * href="http://jakarta.apache.org/commons/dbcp/index.html">commons-dbcp</a>
 * package from Apache:<br>
 * 
 * <pre>
 *      &lt;connectionSource class=&quot;org.apache.log4j.jdbc.DriverManagerConnectionSource&quot;&gt;
 *         &lt;param name=&quot;driver&quot; value=&quot;org.apache.commons.dbcp.PoolingDriver&quot; /&gt;
 *         &lt;param name=&quot;url&quot; value=&quot;jdbc:apache:commons:dbcp:/myPoolingDriver&quot; /&gt;
 *      &lt;/connectionSource&gt;
 * </pre>
 * 
 * Then the configuration information for the commons-dbcp package goes into the
 * file myPoolingDriver.jocl and is placed in the classpath. See the <a
 * href="http://jakarta.apache.org/commons/dbcp/index.html">commons-dbcp</a>
 * documentation for details.
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
        discoverConnnectionProperties();
      } else {
        addError("WARNING: No JDBC driver specified for log4j DriverManagerConnectionSource.");
      }
    } catch (final ClassNotFoundException cnfe) {
      addError("Could not load JDBC driver class: " + driverClass, cnfe);
    }
  }

  /**
   * @see org.apache.log4j.db.ConnectionSource#getConnection()
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
