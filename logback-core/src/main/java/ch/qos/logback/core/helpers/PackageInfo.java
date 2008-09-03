/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.helpers;

public class PackageInfo {

  String jarName;
  String version;
  
  public PackageInfo(String jarName, String version) {
    this.jarName = jarName;
    this.version = version;
  }

  public String getJarName() {
    return jarName;
  }

  public String getVersion() {
    return version;
  }
}
