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
package ch.qos.logback.core.rolling.helper;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.spi.ContextAwareBase;


/**
 * Utility class to help solving problems encountered while renaming files.
 * @author Ceki Gulcu  
 */
public class RenameUtil extends ContextAwareBase {

  
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
    if(from.equals(to)) {
      addWarn("From and to file are the same ["+from+"]. Skipping.");
      return;
    }
    File fromFile = new File(from);

    if (fromFile.exists()) {
      File toFile = new File(to);
      addInfo("Renaming file ["+fromFile+"] to ["+toFile+"]");

      boolean result = fromFile.renameTo(toFile);

      if (!result) {
        addWarn("Failed to rename file ["+fromFile+"] to ["+toFile+"].");
        addWarn("Attempting to rename by copying.");
        renameByCopying(from, to);
      }
    } else {
      throw new RolloverFailure("File [" + from + "] does not exist.");
    }
  }

  static final int BUF_SIZE = 32*1024;
  
  public void renameByCopying(String from, String to)
      throws RolloverFailure {
    try {
      FileInputStream fis = new FileInputStream(from);
      FileOutputStream fos = new FileOutputStream(to);
      byte[] inbuf = new byte[BUF_SIZE];
      int n;

      while ((n = fis.read(inbuf)) != -1) {
        fos.write(inbuf, 0, n);
      }

      fis.close();
      fos.close();

      File fromFile = new File(from);

      if (!fromFile.delete()) {
        addWarn("Could not delete "+ from);
      }
    } catch (IOException ioe) {
      addError("Failed to rename file by copying", ioe);
      throw new RolloverFailure("Failed to rename file by copying");
    }
  }
  
  @Override
  public String toString() {
    return "c.q.l.co.rolling.helper.RenameUtil";
  }
}