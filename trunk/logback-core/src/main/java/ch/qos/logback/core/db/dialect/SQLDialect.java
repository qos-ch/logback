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
 * @author Ceki G&uuml;c&uuml;
 *
 */
public interface SQLDialect {
  public String getSelectInsertId();
}
