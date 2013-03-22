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
package ch.qos.logback.core.testUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.util.CoreTestConstants;

public class Env {

  static public boolean isWindows() {
    return System.getProperty("os.name").indexOf("Windows") != -1;
  }

  static public boolean isMac() {
    return System.getProperty("os.name").indexOf("Mac") != -1;
  }

  static public boolean isLinux() {
    return System.getProperty("os.name").indexOf("Linux") != -1;
  }

  static private boolean isJDK_N_OrHigher(int n) {
    List<String> versionList = new ArrayList<String>();

    // this code should work at least until JDK 10 (assuming n parameter is
    // always 6 or more)
    for(int i = 0; i < 5; i++) {
      versionList.add("1."+(n+i));

    }

    String javaVersion = System.getProperty("java.version");
    if (javaVersion == null) {
      return false;
    }
    for(String v: versionList) {
      if(javaVersion.startsWith(v))
        return true;
    }

    //
    return false;
  }

  static public boolean isJDK6OrHigher() {
    return isJDK_N_OrHigher(6);
  }

  static public String getLocalHostName() {
    InetAddress localhostIA;
    try {
      localhostIA = InetAddress.getLocalHost();
      return localhostIA.getHostName();
    } catch (UnknownHostException e) {
      return null;
    }
  }

  static public boolean isLocalHostNameInList(String[] hostList) {
    String localHostName = getLocalHostName();
    if (localHostName == null) {
      return false;
    }
    for (String host : hostList) {
      if (host.equalsIgnoreCase(localHostName)) {
        return true;
      }
    }
    return false;
  }
  

  public static String getPathToBash() {
    if(Env.isLinux()) {
      return CoreTestConstants.BASH_PATH_ON_LINUX;
    }
    if(Env.isLocalHostNameInList(new String[] {"hetz", "het"})) {
      return CoreTestConstants.BASH_PATH_ON_CYGWIN;
    }
    return null;
  }
}
