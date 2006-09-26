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
import java.sql.Connection;
import java.sql.SQLException;

import ch.qos.logback.core.spi.LifeCycle;


/**
 *  The <id>ConnectionSource</id> interface provides a pluggable means of
 *  transparently obtaining JDBC {@link java.sql.Connection}s for logback classes
 *  that require the use of a {@link java.sql.Connection}.
 *
 *  @author <a href="mailto:rdecampo@twcny.rr.com">Ray DeCampo</a>
 */
public interface ConnectionSource extends LifeCycle {

  final int UNKNOWN_DIALECT = 0;
  final int POSTGRES_DIALECT = 1;
  final int MYSQL_DIALECT = 2;
  final int ORACLE_DIALECT = 3;
  final int MSSQL_DIALECT = 4;
  final int HSQL_DIALECT = 5;  
  /**
   *  Obtain a {@link java.sql.Connection} for use.  The client is
   *  responsible for closing the {@link java.sql.Connection} when it is no
   *  longer required.
   *
   *  @throws SQLException  if a {@link java.sql.Connection} could not be
   *                        obtained
   */
  Connection getConnection() throws SQLException;

  /**
   * Get the SQL dialect that should be used for this connection. Note that the
   * dialect is not needed if the JDBC driver supports the getGeneratedKeys 
   * method.
   */
  int getSQLDialectCode();
  
  /**
   * If the connection supports the JDBC 3.0 getGeneratedKeys method, then
   * we do not need any specific dialect support.
   */
  boolean supportsGetGeneratedKeys();
  
  /**
   * If the connection does not support batch updates, we will avoid using them.
   */  
  public boolean supportsBatchUpdates();
}
