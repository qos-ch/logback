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
import java.sql.Statement;

/**
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class DBHelper {

  static public void closeConnection(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException sqle) {
        // static utility classes should not log without an explicit repository
        // reference
      }
    }
  }

  public static void closeStatement(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException sqle) {
      }
    }
  }
}
