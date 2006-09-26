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

/** 
 * The HSQLDB dialect. 
 * 
 * @author Ceki G&uuml;lc&uuml;
*/ 
public class HSQLDBDialect implements SQLDialect { 
 public static final String SELECT_CURRVAL = "CALL IDENTITY()"; 

 public String getSelectInsertId() { 
   return SELECT_CURRVAL; 
 } 
}
