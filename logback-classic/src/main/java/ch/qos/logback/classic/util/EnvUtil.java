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
package ch.qos.logback.classic.util;

import java.lang.reflect.Method;
import java.util.Iterator;

import ch.qos.logback.core.util.Loader;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class EnvUtil {


  /*
   * Used to replace the ClassLoader that the ServiceLoader uses for unit testing.
   * We need this to mock the resources the ServiceLoader attempts to load from 
   * /META-INF/services thus keeping the projects src/test/resources clean 
   * (see src/test/resources/README.txt).
   */
  static ClassLoader testServiceLoaderClassLoader = null;
  
  static public boolean isGroovyAvailable() {
    ClassLoader classLoader = Loader.getClassLoaderOfClass(EnvUtil.class);
    try {
      Class<?> bindingClass = classLoader.loadClass("groovy.lang.Binding");
      return (bindingClass != null);
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
  
  
  /**
   * Take advantage of Java SE 6's java.util.ServiceLoader API.
   * Using reflection so that there is no compile-time dependency on SE 6.
   */
  static private final Method serviceLoaderLoadMethod;
  static private final Method serviceLoaderIteratorMethod;
  static {
    Method tLoadMethod = null;
    Method tIteratorMethod = null;
    try {
      Class<?> clazz = Class.forName("java.util.ServiceLoader");
      tLoadMethod = clazz.getMethod("load", Class.class, ClassLoader.class);
      tIteratorMethod = clazz.getMethod("iterator");
    } catch (ClassNotFoundException ce) {
      // Running on Java SE 5
    } catch (NoSuchMethodException ne) {
      // Shouldn't happen
    }
    serviceLoaderLoadMethod = tLoadMethod;
    serviceLoaderIteratorMethod = tIteratorMethod;
  }

  static public boolean isServiceLoaderAvailable() {
    return (serviceLoaderLoadMethod != null && serviceLoaderIteratorMethod != null);
  }
  
  private static ClassLoader getServiceLoaderClassLoader() {
    return testServiceLoaderClassLoader == null ? Loader.getClassLoaderOfClass(EnvUtil.class) : testServiceLoaderClassLoader;
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T loadFromServiceLoader(Class<T> c) {
    if (isServiceLoaderAvailable()) {
      Object loader;
      try {
        loader = serviceLoaderLoadMethod.invoke(null, c, getServiceLoaderClassLoader() );
      } catch (Exception e) {
        throw new IllegalStateException("Cannot invoke java.util.ServiceLoader#load()", e);
      }

      Iterator<T> it;
      try {
        it = (Iterator<T>) serviceLoaderIteratorMethod.invoke(loader);
      } catch (Exception e) {
        throw new IllegalStateException("Cannot invoke java.util.ServiceLoader#iterator()", e);
      }
      if (it.hasNext())
        return it.next();
    }
    return null;
  }
  

}
