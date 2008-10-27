/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.rolling;

import java.io.FileInputStream;
import java.io.InputStream;


/**
 * Keep the file "output/test.log open for 10 seconds so that we can test
 * RollingFileAppender's ability to roll file open by another process.
 * @author Ceki G&uumllc&uuml;
 */
public class FileOpener {
  public static void main(String[] args) throws Exception {
    InputStream is = new FileInputStream("output/test.log");
    is.read();
    Thread.sleep(10000);
    is.close();
    System.out.println("Exiting FileOpener");
  }
}
