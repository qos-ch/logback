/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.util;

import java.net.URL;

import ch.qos.logback.core.Context;

/**
 * Load resources (or images) from various sources.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class Loader {
  static final String TSTR = "Caught Exception while in Loader.getResource. This may be innocuous.";

  private static boolean ignoreTCL = false;

  static {

    String ignoreTCLProp = OptionHelper.getSystemProperty("log4j.ignoreTCL",
        null);

    if (ignoreTCLProp != null) {
      ignoreTCL = OptionHelper.toBoolean(ignoreTCLProp, true);
    }
  }

  
  /**
   * This method will search for <code>resource</code> in different places.
   * The search order is as follows:
   * 
   * <ol>
   * 
   * <p>
   * <li>Search for <code>resource</code> using the thread context class
   * loader under Java2. This step is performed only if the <code>
   skipTCL</code>
   * parameter is false.</li>
   * 
   * <p>
   * <li>If the above step fails, search for <code>resource</code> using the
   * class loader that loaded this class (<code>Loader</code>).</li>
   * 
   * <p>
   * <li>Try one last time with
   * <code>ClassLoader.getSystemResource(resource)</code>, that is is using
   * the system class loader in JDK 1.2 and virtual machine's built-in class
   * loader in JDK 1.1.
   * 
   * </ol>
   */
  public static URL getResource(String resource, ClassLoader classLoader) {
    try {
      return classLoader.getResource(resource);
    } catch (Throwable t) {
      return null;
    }
  }

  public static URL getResourceByTCL(String resource) {
    return getResource(resource, getTCL());
  }
  
  /**
   * Get the Thread Context Loader which is a JDK 1.2 feature. If we are running
   * under JDK 1.1 or anything else goes wrong the method returns
   * <code>null<code>.
   *
   */
  public static ClassLoader getTCL() {
    return Thread.currentThread().getContextClassLoader();
  }

  public static Class loadClass(String clazz, Context context) throws ClassNotFoundException {
    ClassLoader cl = context.getClass().getClassLoader();
    return cl.loadClass(clazz);
  }
  /**
   * If running under JDK 1.2 load the specified class using the
   * <code>Thread</code> <code>contextClassLoader</code> if that fails try
   * Class.forname. Under JDK 1.1 only Class.forName is used.
   * 
   */
  public static Class loadClass(String clazz) throws ClassNotFoundException {
    // Just call Class.forName(clazz) if we are running under JDK 1.1
    // or if we are instructed to ignore the TCL.
    if (ignoreTCL) {
      return Class.forName(clazz);
    } else {
      try {
        return getTCL().loadClass(clazz);
      } catch (Throwable e) {
        // we reached here because tcl was null or because of a
        // security exception, or because clazz could not be loaded...
        // In any case we now try one more time
        return Class.forName(clazz);
      }
    }
  }
}
