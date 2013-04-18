/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.rolling.helper.RenameUtil;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class RenameUtilTest {

  Encoder<Object> encoder;
  Context context = new ContextBase();
  StatusChecker statusChecker = new StatusChecker(context);

  long currentTime = System.currentTimeMillis();
  int diff = RandomUtil.getPositiveInt();
  protected String randomOutputDirAsStr = CoreTestConstants.OUTPUT_DIR_PREFIX + diff
          + "/";
  protected File randomOutputDir = new File(randomOutputDirAsStr);

  @Before
  public void setUp() throws Exception {
    encoder = new EchoEncoder<Object>();
    // if this this the fist test run after 'build clean up' then the
    // OUTPUT_DIR_PREFIX might be not yet created
    randomOutputDir.mkdirs();
  }

  @Test
  public void renameToNonExistingDirectory() throws IOException, RolloverFailure {
    RenameUtil renameUtil = new RenameUtil();
    renameUtil.setContext(context);

    int diff2 = RandomUtil.getPositiveInt();
    File fromFile = File.createTempFile("from" + diff, "test",
            randomOutputDir);

    String randomTARGETDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff2;

    renameUtil.rename(fromFile.toString(), new File(randomTARGETDir + "/to.test").toString());
    StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    assertTrue(statusChecker.isErrorFree(0));
  }


  @Test
  @Ignore
  public void MANUAL_renamingOnDifferentVolumesOnLinux() throws IOException, RolloverFailure {
    RenameUtil renameUtil = new RenameUtil();
    renameUtil.setContext(context);

    String src = "/tmp/ramdisk/foo.txt";
    FileOutputStream fis = new FileOutputStream(src);
    fis.write(("hello" + diff).getBytes());

    renameUtil.rename(src, "/tmp/foo" + diff + ".txt");
    StatusPrinter.print(context);
  }


  @Test
  @Ignore
  public void MANUAL_renamingOnDifferentVolumesOnWindows() throws IOException, RolloverFailure {
    RenameUtil renameUtil = new RenameUtil();
    renameUtil.setContext(context);

    String src = "c:/tmp/foo.txt";
    FileOutputStream fis = new FileOutputStream(src);
    fis.write(("hello" + diff).getBytes());
    fis.close();

    renameUtil.rename(src, "d:/tmp/foo" + diff + ".txt");
    StatusPrinter.print(context);
    assertTrue(statusChecker.isErrorFree(0));
  }

  @Test
  public void renameByCopying() {

  }

}
