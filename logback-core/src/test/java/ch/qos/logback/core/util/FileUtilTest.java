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


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileUtilTest {

  List<File> cleanupList = new ArrayList<File>();
  
  @Before
  public void setUp() throws Exception {
    
  }

  @After
  public void tearDown() throws Exception {
    for(File f: cleanupList) {
      f.delete();
    }
  }

  
  @Test
  public void smoke() {
    int diff =  new Random().nextInt(100);
    File file = new File(Constants.OUTPUT_DIR_PREFIX+"/fu"+diff+"/testing.txt");
    // these will be deleted later
    cleanupList.add(file);
    cleanupList.add(file.getParentFile());

    assertTrue(FileUtil.mustCreateParentDirectories(file));
    assertTrue(FileUtil.createMissingParentDirectories(file));
    assertFalse(FileUtil.mustCreateParentDirectories(file));
  }
  
  @Test
  public void smokeII() {
    int diff =  new Random().nextInt(100);
    File file = new File(Constants.OUTPUT_DIR_PREFIX+"/fu"+diff+"/bla/testing.txt");
    // these will be deleted later
    cleanupList.add(file);
    cleanupList.add(file.getParentFile());
    cleanupList.add(file.getParentFile().getParentFile());
    
    assertTrue(FileUtil.mustCreateParentDirectories(file));
    assertTrue(FileUtil.createMissingParentDirectories(file));
    assertFalse(FileUtil.mustCreateParentDirectories(file));
  }
}
