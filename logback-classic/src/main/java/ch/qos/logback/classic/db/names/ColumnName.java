/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.db.names;

public enum ColumnName {

  EVENT_ID,
  
  TIMESTMP,
  FORMATTED_MESSAGE,
  LOGGER_NAME,
  LEVEL_STRING,
  THREAD_NAME,
  REFERENCE_FLAG,
  ARG0,
  ARG1,
  ARG2,
  ARG3,
  CALLER_FILENAME,
  CALLER_CLASS,
  CALLER_METHOD,
  CALLER_LINE,
  
  // MDC
  MAPPED_KEY,
  MAPPED_VALUE,

  I,
  TRACE_LINE;
}
