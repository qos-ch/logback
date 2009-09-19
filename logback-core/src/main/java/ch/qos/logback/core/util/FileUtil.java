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
