/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.rolling.helper;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.FileUtil;


/**
 * Utility class to help solving problems encountered while renaming files.
 *
 * @author Ceki Gulcu
 */
public class RenameUtil extends ContextAwareBase {

  static String RENAMING_ERROR_URL = CoreConstants.CODES_URL+"#renamingError";

  /**
   * A robust file renaming method which in case of failure falls back to
   * renaming by copying. In case, the file to be renamed is open by another
   * process, renaming by copying will succeed whereas regular renaming will
   * fail. However, renaming by copying is much slower.
   *
   * @param from
   * @param to
   * @throws RolloverFailure
   */
  public void rename(String from, String to) throws RolloverFailure {
    if (from.equals(to)) {
      addWarn("From and to file are the same [" + from + "]. Skipping.");
      return;
    }
    File fromFile = new File(from);

    if (fromFile.exists()) {
      File toFile = new File(to);
      createMissingTargetDirsIfNecessary(toFile);

      addInfo("Renaming file [" + fromFile + "] to [" + toFile + "]");

      boolean result = fromFile.renameTo(toFile);

      if (!result) {
        addInfo("Failed to rename file [" + fromFile.getAbsolutePath() + "] to [" + 
          toFile.getAbsolutePath() + "]. Attempting rename by copying.");
        try {
          renameByCopying(from, to);
        } catch(RolloverFailure e) {
          addWarn("Please consider leaving the [file] option of "+ RollingFileAppender.class.getSimpleName()+" empty.");
          addWarn("See also "+ RENAMING_ERROR_URL);
        }
      }
    } else {
      throw new RolloverFailure("File [" + from + "] does not exist.");
    }
  }

  static final int BUF_SIZE = 32 * 1024;

  public void renameByCopying(String from, String to)
          throws RolloverFailure {
    BufferedInputStream bis = null;
    BufferedOutputStream bos = null;
    try {
      bis = new BufferedInputStream(new FileInputStream(from));
      bos = new BufferedOutputStream(new FileOutputStream(to));
      byte[] inbuf = new byte[BUF_SIZE];
      int n;

      while ((n = bis.read(inbuf)) != -1) {
        bos.write(inbuf, 0, n);
      }

      bis.close();
      bis = null;
      bos.close();
      bos = null;

      File fromFile = new File(from);

      if (!fromFile.delete()) {
        addWarn("Could not delete " + from);
      }
    } catch (IOException ioe) {
      addWarn("Failed to rename file by copying", ioe);
      throw new RolloverFailure("Failed to rename file by copying");
    } finally {
      if(bis != null) {
        try {
          bis.close();
        } catch (IOException e) {
          // ignore
        }
      }
      if(bos != null) {
        try {
          bos.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  void createMissingTargetDirsIfNecessary(File toFile) throws RolloverFailure {
    if (FileUtil.isParentDirectoryCreationRequired(toFile)) {
      boolean result = FileUtil.createMissingParentDirectories(toFile);
      if (!result) {
        throw new RolloverFailure("Failed to create parent directories for ["
                + toFile.getAbsolutePath() + "]");
      }
    }
  }

  @Override
  public String toString() {
    return "c.q.l.co.rolling.helper.RenameUtil";
  }
}
