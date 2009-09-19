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
    File file = new File(CoreTestConstants.OUTPUT_DIR_PREFIX+"/fu"+diff+"/testing.txt");
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
    File file = new File(CoreTestConstants.OUTPUT_DIR_PREFIX+"/fu"+diff+"/bla/testing.txt");
    // these will be deleted later
    cleanupList.add(file);
    cleanupList.add(file.getParentFile());
    cleanupList.add(file.getParentFile().getParentFile());
    
    assertTrue(FileUtil.mustCreateParentDirectories(file));
    assertTrue(FileUtil.createMissingParentDirectories(file));
    assertFalse(FileUtil.mustCreateParentDirectories(file));
  }
}
