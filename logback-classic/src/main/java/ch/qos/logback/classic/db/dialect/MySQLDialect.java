/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.db.dialect;

/**
 * 
 * 
 * @author Ceki
 *
 */
public class MySQLDialect implements SQLDialect {
  public static final String SELECT_LAST_INSERT_ID = "SELECT LAST_INSERT_ID()";
  
  public String getSelectInsertId() {
    return SELECT_LAST_INSERT_ID;
  }
}
