/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.util;

import junit.framework.TestCase;

public class FileSizeTest extends TestCase {

  static long KB_CO = 1024;
  static long MB_CO = 1024*1024;
  static long GB_CO = 1024*MB_CO;
  
  public FileSizeTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void test() {
    {
      FileSize fs = FileSize.valueOf("8");
      assertEquals(8, fs.getSize());
    }
    
    {
      FileSize fs = FileSize.valueOf("8 kbs");
      assertEquals(8*KB_CO, fs.getSize());
    }
  
    {
      FileSize fs = FileSize.valueOf("8 kb");
      assertEquals(8*KB_CO, fs.getSize());
    }
    
    {
      FileSize fs = FileSize.valueOf("12 mb");
      assertEquals(12*MB_CO, fs.getSize());
    }

    {
      FileSize fs = FileSize.valueOf("5 GBs");
      assertEquals(5*GB_CO, fs.getSize());
    }

  }
}
