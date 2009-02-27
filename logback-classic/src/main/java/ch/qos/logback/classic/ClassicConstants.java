/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic;

public class ClassicConstants {
  static public final char LOGGER_SEPARATOR = '.';
  static public final String USER_MDC_KEY = "user";
  
  public static final String LOGBACK_CONTEXT_SELECTOR = "logback.ContextSelector";
  public static String JNDI_CONFIGURATION_RESOURCE = "java:comp/env/logback/configuration-resource";
  public static String JNDI_CONTEXT_NAME = "java:comp/env/logback/context-name";
  
  
  /**
   * The maximum number of package separators (dots) that abbreviation algorithms
   * can handle. Class or logger names  with more separators will have their first
   * MAX_DOTS parts shortened.
   * 
   */
  public static final int MAX_DOTS = 16;
}
