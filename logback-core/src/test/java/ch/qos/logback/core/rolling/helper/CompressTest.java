/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.rolling.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.util.Compare;
import ch.qos.logback.core.util.Constants;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * @author Ceki Gulcu
 */
public class CompressTest extends TestCase {

  Context context = new ContextBase();

  public void setUp() throws IOException {
    // Copy source files
    // Delete output files
    {
      File source = new File(Constants.TEST_DIR_PREFIX + "input/compress1.copy");
      File dest = new File(Constants.TEST_DIR_PREFIX + "input/compress1.txt");
                                                        
      copy(source, dest);
      File target = new File(Constants.OUTPUT_DIR_PREFIX
          + "compress1.txt.gz");
      target.mkdirs();
      target.delete();
    }
    {
      File source = new File(Constants.TEST_DIR_PREFIX + "input/compress2.copy");
      File dest = new File(Constants.TEST_DIR_PREFIX + "input/compress2.txt");
      copy(source, dest);
      File target = new File(Constants.OUTPUT_DIR_PREFIX
          + "compress2.txt.gz");
      target.mkdirs();
      target.delete();
    }
    {
      File source = new File(Constants.TEST_DIR_PREFIX + "input/compress3.copy");
      File dest = new File(Constants.TEST_DIR_PREFIX + "input/compress3.txt");
      copy(source, dest);
      File target = new File(Constants.OUTPUT_DIR_PREFIX
          + "compress3.txt.zip");
      target.mkdirs();
      target.delete();
    }
  }

  public void tearDown() {
  }

  public void test1() throws Exception {
    Compressor compressor = new Compressor(CompressionMode.GZ,
        Constants.TEST_DIR_PREFIX + "input/compress1.txt",
        Constants.OUTPUT_DIR_PREFIX + "compress1.txt.gz");
    compressor.setContext(context);
    compressor.compress();

    StatusPrinter.print(context.getStatusManager());
    assertEquals(0, context.getStatusManager().getCount());
    assertTrue(Compare.gzCompare(Constants.OUTPUT_DIR_PREFIX
        + "compress1.txt.gz", Constants.TEST_DIR_PREFIX
        + "witness/compress1.txt.gz"));
  }

  public void test2() throws Exception {
    Compressor compressor = new Compressor(CompressionMode.GZ,
        Constants.TEST_DIR_PREFIX + "input/compress2.txt",
        Constants.OUTPUT_DIR_PREFIX + "compress2.txt");
    compressor.setContext(context);
    compressor.compress();

    StatusPrinter.print(context.getStatusManager());
    assertEquals(0, context.getStatusManager().getCount());
    assertTrue(Compare.gzCompare(Constants.OUTPUT_DIR_PREFIX
        + "compress2.txt.gz", Constants.TEST_DIR_PREFIX
        + "witness/compress2.txt.gz"));
  }

  public void test3() throws Exception {
    Compressor compressor = new Compressor(CompressionMode.ZIP, 
        Constants.TEST_DIR_PREFIX + "input/compress3.txt",
        Constants.OUTPUT_DIR_PREFIX + "compress3.txt");
    compressor.setContext(context);
    compressor.compress();
    StatusPrinter.print(context.getStatusManager());
    assertEquals(0, context.getStatusManager().getCount());
    // assertTrue(Compare.compare("output/compress3.txt.zip",
    // "witness/compress3.txt.zip"));
  }

  private void copy(File src, File dst) throws IOException {
    InputStream in = new FileInputStream(src);
    OutputStream out = new FileOutputStream(dst);
    byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
    in.close();
    out.close();
  }

}
