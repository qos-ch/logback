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
package ch.qos.logback.core.rolling;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.layout.DummyLayout;
import ch.qos.logback.core.layout.EchoLayout;
import ch.qos.logback.core.util.CoreTestConstants;

/**
 * 
 * Do not forget to call start() when configuring programatically.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 */
public class SizeBasedRollingTest extends ScaffoldingForRollingTests {

  @Before
  @Override
  public void setUp() {
    super.setUp();
    {
      File target = new File(CoreTestConstants.OUTPUT_DIR_PREFIX
          + "sizeBased-smoke.log");
      target.mkdirs();
      target.delete();
    }
    {
      File target = new File(CoreTestConstants.OUTPUT_DIR_PREFIX
          + "sbr-test3.log");
      target.mkdirs();
      target.delete();
    }
  }

  @After
  public void tearDown() {
  }

  /**
   * Test whether FixedWindowRollingPolicy throws an exception when the
   * ActiveFileName is not set.
   */
  @Test
  public void activeFileNameNotSet() throws Exception {
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
    fwrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX
        + "sizeBased-test1.%i");
    try {
      fwrp.start();
      fail("The absence of activeFileName option should have caused an exception.");
    } catch (IllegalStateException e) {
      return;
    }
  }

  /**
   * Test basic rolling functionality.
   */
  @Test
  public void smoke() throws Exception {
    Context context = new ContextBase();

    EchoLayout<Object> layout = new EchoLayout<Object>();
    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    rfa.setName("ROLLING");
    rfa.setLayout(layout);
    rfa.setContext(context);
    // make the .log show first
    rfa.setFile(randomOutputDir + "a-sizeBased-smoke.log");

    FixedWindowRollingPolicy swrp = new FixedWindowRollingPolicy();
    swrp.setContext(context);
    SizeBasedTriggeringPolicy<Object> sbtp = new SizeBasedTriggeringPolicy<Object>();
    sbtp.setContext(context);

    sbtp.setMaxFileSize("100");
    swrp.setMinIndex(0);
    swrp.setFileNamePattern(randomOutputDir + "sizeBased-smoke.%i");
    swrp.setParent(rfa);
    swrp.start();

    rfa.setRollingPolicy(swrp);
    rfa.setTriggeringPolicy(sbtp);
    rfa.start();

    int runLength = 45;
    String prefix = "hello";
    for (int i = 0; i < runLength; i++) {
      Thread.sleep(10);
      rfa.doAppend(prefix+i);
    }

    expectedFilenameList.add(randomOutputDir        + "a-sizeBased-smoke.log");
    expectedFilenameList.add(randomOutputDir + "sizeBased-smoke.0");
    expectedFilenameList.add(randomOutputDir + "sizeBased-smoke.1");
    existenceCheck(expectedFilenameList);

    reverseSortedContentCheck(randomOutputDir, runLength, prefix);
  }

  /**
   * Same as testBasic but also with GZ compression.
   */
  @Test
  public void test3() throws Exception {
    Context context = new ContextBase();
    EchoLayout<Object> layout = new EchoLayout<Object>();
    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    rfa.setLayout(layout);
    rfa.setContext(context);
    rfa.setFile(randomOutputDir + "a-sbr-test3.log");

    FixedWindowRollingPolicy fwrp = new FixedWindowRollingPolicy();
    fwrp.setContext(context);
    SizeBasedTriggeringPolicy<Object> sbtp = new SizeBasedTriggeringPolicy<Object>();
    sbtp.setContext(context);

    sbtp.setMaxFileSize("100");
    fwrp.setMinIndex(0);
    // fwrp.setActiveFileName(Constants.TEST_DIR_PREFIX +
    // "output/sbr-test3.log");
    fwrp.setFileNamePattern(randomOutputDir + "sbr-test3.%i.gz");
    fwrp.setParent(rfa);
    fwrp.start();
    rfa.setRollingPolicy(fwrp);
    rfa.setTriggeringPolicy(sbtp);
    rfa.start();

    int runLength = 40;
    String prefix = "hello";
    for (int i = 0; i < runLength; i++) {
      Thread.sleep(10);
      rfa.doAppend("hello"+i);
    }

    expectedFilenameList.add(randomOutputDir        + "a-sbr-test3.log");
    expectedFilenameList.add(randomOutputDir        + "sbr-test3.0.gz");
    expectedFilenameList.add(randomOutputDir        + "sbr-test3.1.gz");

    existenceCheck(expectedFilenameList);
    reverseSortedContentCheck(randomOutputDir, runLength, prefix);
  
  }

}
