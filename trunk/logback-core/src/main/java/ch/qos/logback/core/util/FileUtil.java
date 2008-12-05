/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.util;

import java.io.File;

public class FileUtil {


  public static boolean mustCreateParentDirectories(File file) {
    File parent = file.getParentFile();
    if(parent != null && !parent.exists()) {
      return true;
    } else {
      return false;
    }
  }
  
  public static boolean  createMissingParentDirectories(File file) {
    File parent = file.getParentFile();
    if(parent == null || parent.exists()) {
      throw new IllegalStateException(file + " should not have a null parent");
    } 
    if(parent.exists()) {
      throw new IllegalStateException(file + " should not have existing parent directory");
    } 
    return parent.mkdirs();
  }
}
