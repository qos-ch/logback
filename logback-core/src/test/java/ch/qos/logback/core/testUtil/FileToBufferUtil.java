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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class FileToBufferUtil {

  static public void readIntoList(File file, List<String> stringList)
      throws IOException {

    if(file.getName().endsWith(".gz")) {
      gzFileReadIntoList(file, stringList);
    } else {
      regularReadIntoList(file, stringList);
    }
  }
  
  static public void regularReadIntoList(File file, List<String> stringList) throws IOException {
    FileInputStream fis = new FileInputStream(file);
    BufferedReader in = new BufferedReader(new InputStreamReader(fis));
    String line;
    while( (line = in.readLine()) != null) {
      stringList.add(line);
    }
    in.close();
  }

  static public void gzFileReadIntoList(File file, List<String> stringList) throws IOException {
    FileInputStream fis = new FileInputStream(file);
    GZIPInputStream gzis = new GZIPInputStream(fis);
    BufferedReader in = new BufferedReader(new InputStreamReader(gzis));
    String line;
    while( (line = in.readLine()) != null) {
      stringList.add(line);
    }
    in.close();
  }

}
