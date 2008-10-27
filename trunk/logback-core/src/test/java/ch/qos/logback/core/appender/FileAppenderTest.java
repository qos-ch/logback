/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.appender;

import java.io.File;
import java.util.Random;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.layout.DummyLayout;
import ch.qos.logback.core.layout.NopLayout;
import ch.qos.logback.core.util.Constants;
import ch.qos.logback.core.util.FileUtil;


public class FileAppenderTest extends AbstractAppenderTest {

  public FileAppenderTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  protected AppenderBase getAppender() {
    return new FileAppender();
  }

  protected AppenderBase getConfiguredAppender() {
    FileAppender<Object> appender = new FileAppender<Object>();
    appender.setLayout(new NopLayout<Object>());
    appender.setFile("temp.log");
    appender.setName("temp.log");
    appender.setContext(new ContextBase());
    appender.start();
    return appender;
  }
  
  public void test() {
    String filename = Constants.OUTPUT_DIR_PREFIX+"temp.log";
    
    FileAppender<Object> appender = new FileAppender<Object>();
    appender.setLayout(new DummyLayout<Object>());
    appender.setAppend(false);
    appender.setFile(filename);
    appender.setName("temp.log");
    appender.setContext(new ContextBase());
    appender.start();
    appender.doAppend(new Object());
    appender.stop();
    
    File file = new File(filename);
    assertTrue(file.exists());
    assertTrue("failed to delete "+file.getAbsolutePath(), file.delete());
  }
  
  public void testCreateParentFolders() {
    int diff =  new Random().nextInt(100);
    String filename = Constants.OUTPUT_DIR_PREFIX+"/fat"+diff+"/testing.txt";    
    File file = new File(filename);
    FileAppender<Object> appender = new FileAppender<Object>();
    appender.setLayout(new DummyLayout<Object>());
    appender.setAppend(false);
    appender.setFile(filename);
    appender.setName("testCreateParentFolders");
    appender.setContext(new ContextBase());
    appender.start();
    appender.doAppend(new Object());
    appender.stop();
    assertFalse(FileUtil.mustCreateParentDirectories(file));
    assertTrue(file.exists());
   
    // cleanup
    assertTrue("failed to delete "+file.getAbsolutePath(), file.delete());
    File parent = file.getParentFile();
    assertTrue("failed to delete "+parent.getAbsolutePath(), parent.delete());
  }

}
