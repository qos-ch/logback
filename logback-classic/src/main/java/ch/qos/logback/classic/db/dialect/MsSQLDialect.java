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
* The MS SQL Server dialect is untested. 
* 
* Note that the dialect is not needed if your JDBC driver supports 
* the getGeneratedKeys method introduced in JDBC 3.0 specification.
* 
* @author James Stauffer 
*/ 
public class MsSQLDialect implements SQLDialect { 
 public static final String SELECT_CURRVAL = "SELECT @@identity id"; 

 public String getSelectInsertId() { 
   return SELECT_CURRVAL; 
 } 
}
