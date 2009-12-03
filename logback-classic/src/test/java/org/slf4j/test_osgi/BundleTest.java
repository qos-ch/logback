package org.slf4j.test_osgi;

import java.io.File;

import junit.framework.TestCase;

public class BundleTest extends TestCase {

  FrameworkErrorListener fel = new FrameworkErrorListener();
  CheckingBundleListener mbl = new CheckingBundleListener();
  
  FelixHost felixHost = new FelixHost(fel, mbl);
  
  protected void setUp() throws Exception {
    super.setUp();
    felixHost.doLaunch();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    felixHost.stop();
  }

  public void testSmoke() {
    System.out.println("==========="+new File(".").getAbsolutePath());
    mbl.dumpAll();
    // check that the bundle was installed
    assertTrue(mbl.exists("iBundle"));
    if(fel.errorList.size() != 0) {
      fel.dumpAll(); 
    }
    // check that no errors occured
    assertEquals(0, fel.errorList.size());
  }
}
