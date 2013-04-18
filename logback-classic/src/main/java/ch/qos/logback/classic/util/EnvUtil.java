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

import ch.qos.logback.core.util.Loader;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class EnvUtil {


  static public boolean isGroovyAvailable() {
    ClassLoader classLoader = Loader.getClassLoaderOfClass(EnvUtil.class);
    try {
      Class bindingClass = classLoader.loadClass("groovy.lang.Binding");
      return (bindingClass != null);
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

}
