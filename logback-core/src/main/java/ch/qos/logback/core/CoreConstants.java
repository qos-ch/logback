/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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
package ch.qos.logback.core;

public class CoreConstants {

  // Note that the line.separator property can be looked up even by
  // applets.
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");
  public static final int LINE_SEPARATOR_LEN = LINE_SEPARATOR.length();


  public static final String CODES_URL = "http://logback.qos.ch/codes.html";

  /**
   * The default context name.
   */
  public static final String DEFAULT_CONTEXT_NAME = "default";
  /**
   * Customized pattern conversion rules are stored under this key in the
   * {@link Context} object store.
   */
  public static final String PATTERN_RULE_REGISTRY = "PATTERN_RULE_REGISTRY";

  public static final String ISO8601_STR = "ISO8601";
  public static final String ISO8601_PATTERN = "yyyy-MM-dd HH:mm:ss,SSS";
  public static final String DAILY_DATE_PATTERN = "yyyy-MM-dd";

  /**
   * Time format used in Common Log Format
   */
  static public final String CLF_DATE_PATTERN = "dd/MM/yyyy:HH:mm:ss Z";

  /**
   * The key used in locating the evaluator map in context's object map.
   */
  static public final String EVALUATOR_MAP = "EVALUATOR_MAP";

  /**
   * By convention, we assume that the static method named "valueOf" taking
   * a string argument can restore a given object from its string
   * representation.
   */
  static public final String VALUE_OF = "valueOf";

  /**
   * An empty string.
   */
  public final static String EMPTY_STRING = "";

  /**
   * An empty string array.
   */
  public final static String[] EMPTY_STRING_ARRAY = new String[]{};

  /**
   * An empty Class array.
   */
  public final static Class<?>[] EMPTY_CLASS_ARRAY = new Class[]{};
  public final static String CAUSED_BY = "Caused by: ";
  public final static String WRAPPED_BY = "Wrapped by: ";

  public final static char PERCENT_CHAR = '%';
  public static final char LEFT_PARENTHESIS_CHAR = '(';
  public static final char RIGHT_PARENTHESIS_CHAR = ')';

  public static final char ESCAPE_CHAR = '\\';
  public static final char CURLY_LEFT = '{';
  public static final char CURLY_RIGHT = '}';
  public static final char COMMA_CHAR = ',';
  public static final char DOUBLE_QUOTE_CHAR = '"';
  public static final char SINGLE_QUOTE_CHAR = '\'';


  /**
   * Number of rows before in an HTML table before,
   * we close the table and create a new one
   */
  public static final int TABLE_ROW_LIMIT = 10000;


  // reset the ObjectOutputStream every OOS_RESET_FREQUENCY calls
  // this avoid serious memory leaks
  public static final int OOS_RESET_FREQUENCY = 70;

  /**
   * The reference bogo instructions per second on
   * Ceki's machine (Orion)
   */
  public static long REFERENCE_BIPS = 9000;


  // the max number of times an error should be reported
  static public final int MAX_ERROR_COUNT = 4;


  static public final char DOT = '.';
  static public final char TAB = '\t';
  static public final char DOLLAR = '$';

  static public final String SEE_FNP_NOT_SET = "See also http://logback.qos.ch/codes.html#tbr_fnp_not_set";

  static public final String CONFIGURATION_WATCH_LIST = "CONFIGURATION_WATCH_LIST";
  static public final String CONFIGURATION_WATCH_LIST_RESET = "CONFIGURATION_WATCH_LIST_RESET";

  static public final String SAFE_JORAN_CONFIGURATION = "SAFE_JORAN_CONFIGURATION";
  static public final String XML_PARSING = "XML_PARSING";



  /**
   * The key under which the local host name is registered in the logger
   * context.
   */
  public static final String HOSTNAME_KEY = "HOSTNAME";

  /**
   * The key under which the current context name is registered in the logger
   * context.
   */
  public static final String CONTEXT_NAME_KEY = "CONTEXT_NAME";


  public static int BYTES_PER_INT = 4;
  public static final int MILLIS_IN_ONE_SECOND = 1000;
  public static final int MILLIS_IN_ONE_MINUTE = MILLIS_IN_ONE_SECOND*60;
  public static final int MILLIS_IN_ONE_HOUR = MILLIS_IN_ONE_MINUTE*60;
  public static final int MILLIS_IN_ONE_DAY = MILLIS_IN_ONE_HOUR*24;
  public static final int MILLIS_IN_ONE_WEEK = MILLIS_IN_ONE_DAY*7;

}
