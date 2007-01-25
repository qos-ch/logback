/** 
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2005, QOS.ch, LOGBack.com
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic;

public class ClassicGlobal {
  static public final char LOGGER_SEPARATOR = '.';
  static public final String CAUSED_BY = "Caused by: ";
  static public final char DOT = '.';
  static public final String USER_MDC_KEY = "user";
  
  public static final String LOGBACK_CONTEXT_SELECTOR = "logback.ContextSelector";
  public static String JNDI_CONFIGURATION_RESOURCE = "java:comp/env/logback/configuration-resource";
  public static String JNDI_CONTEXT_NAME = "java:comp/env/logback/context-name";
}
