/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.testUtil;

public class Env {

  static public boolean isWindows() {
    return System.getProperty("os.name").indexOf("Windows") != -1;
  }

  static public boolean isLinux() {
    return System.getProperty("os.name").indexOf("Linux") != -1;
  }

  static public boolean isJDK6OrHigher() {
    String javaVersion = System.getProperty("java.version");
    if (javaVersion == null) {
      return false;
    }
    if (javaVersion.startsWith("1.6") || javaVersion.startsWith("1.7")) {
      return true;
    } else {
      return false;
    }
  }

}
