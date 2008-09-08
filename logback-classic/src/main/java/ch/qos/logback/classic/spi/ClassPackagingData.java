/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.spi;

import java.io.Serializable;

public class ClassPackagingData implements Serializable {

  private static final long serialVersionUID = 637783570208674312L;

  final String codeLocation;
  final String version;
  private final boolean exact;
  
  public ClassPackagingData(String codeLocation, String version) {
    this.codeLocation = codeLocation;
    this.version = version;
    this.exact = true;
  }

  public ClassPackagingData(String classLocation, String version, boolean exact) {
    this.codeLocation = classLocation;
    this.version = version;
    this.exact = exact;
  }
  
  public String getCodeLocation() {
    return codeLocation;
  }

  public String getVersion() {
    return version;
  }

  public boolean isExact() {
    return exact;
  }
  
}
