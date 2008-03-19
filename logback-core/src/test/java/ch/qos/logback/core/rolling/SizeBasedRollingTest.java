/*
 * Copyright 1999,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.qos.logback.core.rolling;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.layout.DummyLayout;
import ch.qos.logback.core.util.Compare;
import ch.qos.logback.core.util.Constants;

/**
 * 
 * Do not forget to call start() when configuring programatically.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 */
public class SizeBasedRollingTest extends TestCase {

  public SizeBasedRollingTest(String name) {
    super(name);
  }

  public void setUp() {
    {
      File target = new File(Constants.TEST_DIR_PREFIX
          + "output/sizeBased-test2.log");
      target.mkdirs();
      target.delete();
    }
    {
      File target = new File(Constants.TEST_DIR_PREFIX + "output/sbr-test3.log");
      target.mkdirs();
      target.delete();
    }
  }

  public void tearDown() {
  }

  /**
   * Test whether FixedWindowRollingPolicy throws an exception when the
   * ActiveFileName is not set.
   */
  public void test1() throws Exception {
    // We purposefully use the \n as the line separator.
    // This makes the regression test system independent.
    Context context = new ContextBase();
    Layout<Object> layout = new DummyLayout<Object>();
    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    rfa.setLayout(layout);
    rfa.setContext(new ContextBase());

    FixedWindowRollingPolicy fwrp = new FixedWindowRollingPolicy();
    fwrp.setContext(context);
    fwrp.setParent(rfa);
    SizeBasedTriggeringPolicy sbtp = new SizeBasedTriggeringPolicy();
    sbtp.setContext(context);

    sbtp.setMaxFileSize("100");
    sbtp.start();
    fwrp.setFileNamePattern(Constants.TEST_DIR_PREFIX
        + "output/sizeBased-test1.%i");
    try {
      fwrp.start();
      fail("The absence of activeFileName option should have caused an exception.");
    } catch (IllegalStateException e) {
      return;
    }

    // StatusPrinter.print(context.getStatusManager());
  }

  /**
   * Test basic rolling functionality.
   */
  public void test2() throws Exception {
    Context context = new ContextBase();

    DummyLayout<Object> layout = new DummyLayout<Object>();
    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    rfa.setName("ROLLING");
    rfa.setLayout(layout);
    rfa.setContext(context);
    rfa.setFile(Constants.TEST_DIR_PREFIX
        + "output/sizeBased-test2.log");
    
    FixedWindowRollingPolicy swrp = new FixedWindowRollingPolicy();
    swrp.setContext(context);
    SizeBasedTriggeringPolicy<Object> sbtp = new SizeBasedTriggeringPolicy<Object>();
    sbtp.setContext(context);

    sbtp.setMaxFileSize("100");
    swrp.setMinIndex(0);
//    swrp.setActiveFileName(Constants.TEST_DIR_PREFIX
//        + "output/sizeBased-test2.log");

    swrp.setFileNamePattern(Constants.TEST_DIR_PREFIX
        + "output/sizeBased-test2.%i");
    swrp.setParent(rfa);
    swrp.start();

    rfa.setRollingPolicy(swrp);
    rfa.setTriggeringPolicy(sbtp);
    rfa.start();

    // Write exactly 10 bytes with each log
    // for (int i = 0; i < 25; i++) {
    // Thread.sleep(100);
    // if (i < 10) {
    // rfa.doAppend("Hello---" + i);
    // //logger.debug("Hello---" + i);
    // } else if (i < 100) {
    // rfa.doAppend("Hello---" + i);
    // //logger.debug("Hello--" + i);
    // }
    // }

    for (int i = 0; i < 45; i++) {
      Thread.sleep(10);
      rfa.doAppend("hello");
    }

    assertTrue(new File(Constants.TEST_DIR_PREFIX
        + "output/sizeBased-test2.log").exists());
    assertTrue(new File(Constants.TEST_DIR_PREFIX + "output/sizeBased-test2.0")
        .exists());
    assertTrue(new File(Constants.TEST_DIR_PREFIX + "output/sizeBased-test2.1")
        .exists());

    // The File.length() method is not accurate under Windows

    if (!isWindows()) {

      assertTrue(Compare.compare(Constants.TEST_DIR_PREFIX
          + "output/sizeBased-test2.log", Constants.TEST_DIR_PREFIX
          + "witness/rolling/sbr-test2.l"));
      assertTrue(Compare.compare(Constants.TEST_DIR_PREFIX
          + "output/sizeBased-test2.0", Constants.TEST_DIR_PREFIX
          + "witness/rolling/sbr-test2.0"));
      assertTrue(Compare.compare(Constants.TEST_DIR_PREFIX
          + "output/sizeBased-test2.1", Constants.TEST_DIR_PREFIX
          + "witness/rolling/sbr-test2.1"));
    }

    // StatusPrinter.print(context.getStatusManager());
  }

  /**
   * Same as testBasic but also with GZ compression.
   */
  public void test3() throws Exception {
    Context context = new ContextBase();
    DummyLayout<Object> layout = new DummyLayout<Object>();
    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    rfa.setLayout(layout);
    rfa.setContext(context);
    rfa.setFile(Constants.TEST_DIR_PREFIX + "output/sbr-test3.log");

    FixedWindowRollingPolicy fwrp = new FixedWindowRollingPolicy();
    fwrp.setContext(context);
    SizeBasedTriggeringPolicy<Object> sbtp = new SizeBasedTriggeringPolicy<Object>();
    sbtp.setContext(context);

    sbtp.setMaxFileSize("100");
    fwrp.setMinIndex(0);
    //fwrp.setActiveFileName(Constants.TEST_DIR_PREFIX + "output/sbr-test3.log");
    fwrp.setFileNamePattern(Constants.TEST_DIR_PREFIX
        + "output/sbr-test3.%i.gz");
    fwrp.setParent(rfa);
    fwrp.start();
    rfa.setRollingPolicy(fwrp);
    rfa.setTriggeringPolicy(sbtp);
    rfa.start();

    // Write exactly 10 bytes with each log
    // for (int i = 0; i < 25; i++) {
    // Thread.sleep(100);
    // if (i < 10) {
    // rfa.doAppend("Hello---" + i);
    // //logger.debug("Hello---" + i);
    // } else if (i < 100) {
    // rfa.doAppend("Hello---" + i);
    // //logger.debug("Hello--" + i);
    // }
    // }

    for (int i = 0; i < 45; i++) {
      Thread.sleep(10);
      rfa.doAppend("hello");
    }

    assertTrue(new File(Constants.TEST_DIR_PREFIX + "output/sbr-test3.log")
        .exists());
    assertTrue(new File(Constants.TEST_DIR_PREFIX + "output/sbr-test3.0.gz")
        .exists());
    assertTrue(new File(Constants.TEST_DIR_PREFIX + "output/sbr-test3.1.gz")
        .exists());

    if (!isWindows()) {

      assertTrue(Compare.compare(
          "Constants.TEST_DIR_PREFIXoutput/sbr-test3.log",
          Constants.TEST_DIR_PREFIX + "witness/rolling/sbr-test3.l"));
      assertTrue(Compare.gzCompare(
          "Constants.TEST_DIR_PREFIXoutput/sbr-test3.0.gz",
          Constants.TEST_DIR_PREFIX + "witness/rolling/sbr-test3.0.gz"));
      assertTrue(Compare.gzCompare(
          "Constants.TEST_DIR_PREFIXoutput/sbr-test3.1.gz",
          Constants.TEST_DIR_PREFIX + "witness/rolling/sbr-test3.1.gz"));
    }

    // StatusPrinter.print(context.getStatusManager());
  }

  boolean isWindows() {
    return System.getProperty("os.name").indexOf("Windows") != -1;
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(SizeBasedRollingTest.class);
    return suite;
  }
}
