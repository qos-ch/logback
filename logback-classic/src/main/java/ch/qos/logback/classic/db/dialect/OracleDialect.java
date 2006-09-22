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
 * The Oracle dialect. Tested successfully on Oracle9i Release 9.2.0.3.0 by 
 * James Stauffer.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class OracleDialect implements SQLDialect {
  public static final String SELECT_CURRVAL = "SELECT logging_event_id_seq.currval from dual";

  public String getSelectInsertId() {
    return SELECT_CURRVAL;
  }

}
