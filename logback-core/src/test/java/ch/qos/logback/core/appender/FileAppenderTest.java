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
package ch.qos.logback.core.appender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.layout.DummyLayout;
import ch.qos.logback.core.layout.NopLayout;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.FileUtil;

public class FileAppenderTest extends AbstractAppenderTest<Object> {

  int diff = new Random().nextInt(100);
  Context context = new ContextBase();

  protected Appender<Object> getAppender() {
    return new FileAppender<Object>();
  }

  protected Appender<Object> getConfiguredAppender() {
    FileAppender<Object> appender = new FileAppender<Object>();
    appender.setLayout(new NopLayout<Object>());
    appender.setFile(CoreTestConstants.OUTPUT_DIR_PREFIX+"temp.log");
    appender.setName("test");
    appender.setContext(context);
    appender.start();
    return appender;
  }

  @Test
  public void smoke() {
    String filename = CoreTestConstants.OUTPUT_DIR_PREFIX + "temp.log";

    FileAppender<Object> appender = new FileAppender<Object>();
    appender.setLayout(new DummyLayout<Object>());
    appender.setAppend(false);
    appender.setFile(filename);
    appender.setName("smoke");
    appender.setContext(context);
    appender.start();
    appender.doAppend(new Object());
    appender.stop();

    File file = new File(filename);
    assertTrue(file.exists());
    assertTrue("failed to delete " + file.getAbsolutePath(), file.delete());
  }

  @Test
  public void testCreateParentFolders() {
    String filename = CoreTestConstants.OUTPUT_DIR_PREFIX + "/fat" + diff
        + "/testing.txt";
    File file = new File(filename);
    FileAppender<Object> appender = new FileAppender<Object>();
    appender.setLayout(new DummyLayout<Object>());
    appender.setAppend(false);
    appender.setFile(filename);
    appender.setName("testCreateParentFolders");
    appender.setContext(context);
    appender.start();
    appender.doAppend(new Object());
    appender.stop();
    assertFalse(FileUtil.mustCreateParentDirectories(file));
    assertTrue(file.exists());

    // cleanup
    assertTrue("failed to delete " + file.getAbsolutePath(), file.delete());
    File parent = file.getParentFile();
    assertTrue("failed to delete " + parent.getAbsolutePath(), parent.delete());
  }

  @Test
  public void testPrudentModeLogicalImplications() {
    String filename = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "testing.txt";
    File file = new File(filename);
    FileAppender<Object> appender = new FileAppender<Object>();
    appender.setLayout(new DummyLayout<Object>());
    appender.setFile(filename);
    appender.setName("testPrudentMode");
    appender.setContext(context);

    appender.setAppend(false);
    appender.setImmediateFlush(false);
    appender.setBufferedIO(true);
    appender.setPrudent(true);
    appender.start();

    assertTrue(appender.getImmediateFlush());
    assertTrue(appender.isAppend());
    assertFalse(appender.isBufferedIO());

    StatusManager sm = context.getStatusManager();
    assertEquals(Status.WARN, sm.getLevel());
    List<Status> statusList = sm.getCopyOfStatusList();
    assertTrue("Expecting status list size to be larger than 3, but was "
        + statusList.size(), statusList.size() > 3);
    String msg1 = statusList.get(1).getMessage();

    assertTrue("Got message [" + msg1 + "]", msg1
        .startsWith("Setting \"Append\" property"));
    
    appender.doAppend(new Object());
    appender.stop();
    assertTrue(file.exists());
    assertTrue("failed to delete " + file.getAbsolutePath(), file.delete());
  }
}
