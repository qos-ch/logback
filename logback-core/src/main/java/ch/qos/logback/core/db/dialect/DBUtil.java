/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.db.dialect;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import ch.qos.logback.core.db.ConnectionSource;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * 
 * @author Ceki Gulcu
 * 
 */
public class DBUtil extends ContextAwareBase {
  private static final String POSTGRES_PART = "postgresql";
  private static final String MYSQL_PART = "mysql";
  private static final String ORACLE_PART = "oracle";
  // private static final String MSSQL_PART = "mssqlserver4";
  private static final String MSSQL_PART = "microsoft";
  private static final String HSQL_PART = "hsql";

  public static int discoverSQLDialect(DatabaseMetaData meta) {
    int dialectCode = 0;

    try {

      String dbName = meta.getDatabaseProductName().toLowerCase();

      if (dbName.indexOf(POSTGRES_PART) != -1) {
        return ConnectionSource.POSTGRES_DIALECT;
      } else if (dbName.indexOf(MYSQL_PART) != -1) {
        return ConnectionSource.MYSQL_DIALECT;
      } else if (dbName.indexOf(ORACLE_PART) != -1) {
        return ConnectionSource.ORACLE_DIALECT;
      } else if (dbName.indexOf(MSSQL_PART) != -1) {
        return ConnectionSource.MSSQL_DIALECT;
      } else if (dbName.indexOf(HSQL_PART) != -1) {
        return ConnectionSource.HSQL_DIALECT;
      } else {
        return ConnectionSource.UNKNOWN_DIALECT;
      }
    } catch (SQLException sqle) {
      // we can't do much here
    }

    return dialectCode;
  }

  public static SQLDialect getDialectFromCode(int dialectCode) {
    SQLDialect sqlDialect = null;

    switch (dialectCode) {
    case ConnectionSource.POSTGRES_DIALECT:
      sqlDialect = new PostgreSQLDialect();

      break;
    case ConnectionSource.MYSQL_DIALECT:
      sqlDialect = new MySQLDialect();

      break;
    case ConnectionSource.ORACLE_DIALECT:
      sqlDialect = new OracleDialect();

      break;
    case ConnectionSource.MSSQL_DIALECT:
      sqlDialect = new MsSQLDialect();

      break;
    case ConnectionSource.HSQL_DIALECT:
      sqlDialect = new HSQLDBDialect();

      break;
    }
    return sqlDialect;
  }

  /**
   * This method handles cases where the
   * {@link DatabaseMetaData#supportsGetGeneratedKeys} method is missing in the
   * JDBC driver implementation.
   */
  public boolean supportsGetGeneratedKeys(DatabaseMetaData meta) {
    try {
      //
      // invoking JDK 1.4 method by reflection
      //
      return ((Boolean) DatabaseMetaData.class.getMethod(
          "supportsGetGeneratedKeys", (Class[]) null).invoke(meta,
          (Object[]) null)).booleanValue();
    } catch (Throwable e) {
      addInfo("Could not call supportsGetGeneratedKeys method. This may be recoverable");
      return false;
    }
  }

  /**
   * This method handles cases where the
   * {@link DatabaseMetaData#supportsBatchUpdates} method is missing in the JDBC
   * driver implementation.
   */
  public boolean supportsBatchUpdates(DatabaseMetaData meta) {
    try {
      return meta.supportsBatchUpdates();
    } catch (Throwable e) {
      addInfo("Missing DatabaseMetaData.supportsBatchUpdates method.");
      return false;
    }
  }
}
