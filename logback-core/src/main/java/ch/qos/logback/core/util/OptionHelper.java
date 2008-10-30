/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;

/**
 * @author Ceki Gulcu
 */
public class OptionHelper {

  public static Object instantiateByClassName(String className,
      Class superClass, Context context) throws IncompatibleClassException, DynamicClassLoadingException {
    ClassLoader classLoader = context.getClass().getClassLoader();
    return instantiateByClassName(className, superClass, classLoader);
  }
  
  @SuppressWarnings("unchecked")
  public static Object instantiateByClassName(String className,
      Class superClass, ClassLoader classLoader)
      throws IncompatibleClassException, DynamicClassLoadingException {

    if (className == null) {
      throw new NullPointerException();
    }

    try {
      Class classObj = null;
      classObj = classLoader.loadClass(className);
      if (!superClass.isAssignableFrom(classObj)) {
        throw new IncompatibleClassException(superClass, classObj);
      }
      return classObj.newInstance();
    } catch (IncompatibleClassException ice) {
      throw ice;
    } catch (Throwable t) {
      throw new DynamicClassLoadingException("Failed to instantiate type "
          + className, t);
    }
  }

  /**
   * Find the value corresponding to <code>key</code> in <code>props</code>.
   * Then perform variable substitution on the found value.
   * 
   */
  // public static String findAndSubst(String key, Properties props) {
  // String value = props.getProperty(key);
  //
  // if (value == null) {
  // return null;
  // }
  //
  // try {
  // return substVars(value, props);
  // } catch (IllegalArgumentException e) {
  // return value;
  // }
  // }
  final static String DELIM_START = "${";
  final static char DELIM_STOP = '}';
  final static int DELIM_START_LEN = 2;
  final static int DELIM_STOP_LEN = 1;
  final static String _IS_UNDEFINED = "_IS_UNDEFINED";
  
  /**
   * Perform variable substitution in string <code>val</code> from the values
   * of keys found in context property map, and if that fails, then in the
   * system properties.
   * 
   * <p>
   * The variable substitution delimeters are <b>${</b> and <b>}</b>.
   * 
   * <p>
   * For example, if the context property map contains a property "key1" set as
   * "value1", then the call
   * 
   * <pre>
   * String s = OptionConverter.substituteVars(&quot;Value of key is ${key1}.&quot;, context);</pre>
   * will set the variable <code>s</code> to "Value of key is value1.".
   * 
   * <p>
   * If no value could be found for the specified key in the context map, then 
   * the system properties are searched, if that fails, then substitution defaults 
   * to appending "_IS_UNDEFINED" to the key name.
   * 
   * <p>
   * For example, if not the context not the system properties contains no value for the key
   * "inexistentKey", then the call
   * 
   * <pre>
   * String s = OptionConverter.subsVars(
   *     &quot;Value of inexistentKey is [${inexistentKey}]&quot;, context);</pre>
   * will set <code>s</code> to "Value of inexistentKey is [inexistentKey_IS_UNDEFINED]".
   * 
   * <p>
   * Nevertheless, it is possible to specify a default substitution value using
   * the ":-" operator. For example, the call
   * 
   * <pre>
   * String s = OptionConverter.subsVars(&quot;Value of key is [${key2:-val2}]&quot;, context);</pre>
   * will set <code>s</code> to "Value of key is [val2]" even if the "key2"
   * property is not set.
   * 
   * <p>
   * An {@link java.lang.IllegalArgumentException} is thrown if <code>val</code>
   * contains a start delimeter "${" which is not balanced by a stop delimeter
   * "}".
   * </p>

   * 
   * @param val
   *          The string on which variable substitution is performed.
   * @throws IllegalArgumentException
   *           if <code>val</code> is malformed.
   */
  public static String substVars(String val, Context context) {

    StringBuffer sbuf = new StringBuffer();

    int i = 0;
    int j;
    int k;

    while (true) {
      j = val.indexOf(DELIM_START, i);

      if (j == -1) {
        // no more variables
        if (i == 0) { // this is a simple string

          return val;
        } else { // add the tail string which contails no variables and return
          // the result.
          sbuf.append(val.substring(i, val.length()));

          return sbuf.toString();
        }
      } else {
        sbuf.append(val.substring(i, j));
        k = val.indexOf(DELIM_STOP, j);

        if (k == -1) {
          throw new IllegalArgumentException('"' + val
              + "\" has no closing brace. Opening brace at position " + j + '.');
        } else {
          j += DELIM_START_LEN;

          String rawKey = val.substring(j, k);

          // Massage the key to extract a default replacement if there is one
          String[] extracted = extractDefaultReplacement(rawKey);
          String key = extracted[0];
          String defaultReplacement = extracted[1]; // can be null

          String replacement = null;

          // first try the props passed as parameter
          replacement = context.getProperty(key);

          // then try in System properties
          if (replacement == null) {
            replacement = getSystemProperty(key, null);
          }

          // if replacement is still null, use the defaultReplacement which
          // can be null as well
          if (replacement == null) {
            replacement = defaultReplacement;
          }

          if (replacement != null) {
            // Do variable substitution on the replacement string
            // such that we can solve "Hello ${x2}" as "Hello p1"
            // where the properties are
            // x1=p1
            // x2=${x1}
            String recursiveReplacement = substVars(replacement, context);
            sbuf.append(recursiveReplacement);
          } else {
            // if we could not find a replacement, then signal the error
            sbuf.append(key+"_IS_UNDEFINED");
          }

          i = k + DELIM_STOP_LEN;
        }
      }
    }
  }

  /**
   * Very similar to <code>System.getProperty</code> except that the
   * {@link SecurityException} is hidden.
   * 
   * @param key
   *          The key to search for.
   * @param def
   *          The default value to return.
   * @return the string value of the system property, or the default value if
   *         there is no property with that key.
   */
  public static String getSystemProperty(String key, String def) {
    try {
      return System.getProperty(key, def);
    } catch (Throwable e) { // MS-Java throws
      // com.ms.security.SecurityExceptionEx
      return def;
    }
  }

  static public String[] extractDefaultReplacement(String key) {
    String[] result = new String[2];
    result[0] = key;
    int d = key.indexOf(":-");
    if (d != -1) {
      result[0] = key.substring(0, d);
      result[1] = key.substring(d + 2);
    }
    return result;
  }

  /**
   * If <code>value</code> is "true", then <code>true</code> is returned. If
   * <code>value</code> is "false", then <code>true</code> is returned.
   * Otherwise, <code>default</code> is returned.
   * 
   * <p>
   * Case of value is unimportant.
   */
  public static boolean toBoolean(String value, boolean dEfault) {
    if (value == null) {
      return dEfault;
    }

    String trimmedVal = value.trim();

    if ("true".equalsIgnoreCase(trimmedVal)) {
      return true;
    }

    if ("false".equalsIgnoreCase(trimmedVal)) {
      return false;
    }

    return dEfault;
  }

  public static boolean isEmpty(String val) {
    return ((val == null) || CoreConstants.EMPTY_STRING.equals(val));
  }

}
