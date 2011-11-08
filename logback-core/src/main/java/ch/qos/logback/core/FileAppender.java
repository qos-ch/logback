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
package ch.qos.logback.core;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import ch.qos.logback.core.recovery.ResilientFileOutputStream;
import ch.qos.logback.core.util.FileUtil;

/**
 * FileAppender appends log events to a file.
 * 
 * For more information about this appender, please refer to the online manual
 * at http://logback.qos.ch/manual/appenders.html#FileAppender
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class FileAppender<E> extends OutputStreamAppender<E> {

  /**
   * Append to or truncate the file? The default value for this variable is
   * <code>true</code>, meaning that by default a <code>FileAppender</code> will
   * append to an existing file and not truncate it.
   */
  protected boolean append = true;

  /**
   * The name of the active log file.
   */
  protected String fileName = null;

  private boolean prudent = false;

  /**
   * The <b>File</b> property takes a string value which should be the name of
   * the file to append to.
   */
  public void setFile(String file) {
    if (file == null) {
      fileName = file;
    } else {
      // Trim spaces from both ends. The users probably does not want
      // trailing spaces in file names.
      fileName = file.trim();
    }
  }

  /**
   * Returns the value of the <b>Append</b> property.
   */
  public boolean isAppend() {
    return append;
  }

  /**
   * This method is used by derived classes to obtain the raw file property.
   * Regular users should not be calling this method.
   * 
   * @return the value of the file property
   */
  final public String rawFileProperty() {
    return fileName;
  }

  /**
   * Returns the value of the <b>File</b> property.
   * 
   * <p>
   * This method may be overridden by derived classes.
   * 
   */
  public String getFile() {
    return fileName;
  }

  /**
   * If the value of <b>File</b> is not <code>null</code>, then
   * {@link #openFile} is called with the values of <b>File</b> and
   * <b>Append</b> properties.
   */
  public void start() {
    int errors = 0;
    if (getFile() != null) {
      addInfo("File property is set to [" + fileName + "]");

      if (prudent) {
        if (!isAppend()) {
          setAppend(true);
          addWarn("Setting \"Append\" property to true on account of \"Prudent\" mode");
        }
      }

      try {
        openFile(getFile());
      } catch (java.io.IOException e) {
        errors++;
        addError("openFile(" + fileName + "," + append + ") call failed.", e);
      }
    } else {
      errors++;
      addError("\"File\" property not set for appender named [" + name + "].");
    }
    if (errors == 0) {
      super.start();
    }
  }

  /**
   * <p>
   * Sets and <i>opens</i> the file where the log output will go. The specified
   * file must be writable.
   * 
   * <p>
   * If there was already an opened file, then the previous file is closed
   * first.
   * 
   * <p>
   * <b>Do not use this method directly. To configure a FileAppender or one of
   * its subclasses, set its properties one by one and then call start().</b>
   * 
   * @param file_name
   *          The path to the log file.
   */
  public void openFile(String file_name) throws IOException {
    synchronized (lock) {
      File file = new File(file_name);
      if (FileUtil.isParentDirectoryCreationRequired(file)) {
        boolean result = FileUtil.createMissingParentDirectories(file);
        if (!result) {
          addError("Failed to create parent directories for ["
              + file.getAbsolutePath() + "]");
        }
      }

      ResilientFileOutputStream resilientFos = new ResilientFileOutputStream(
          file, append);
      resilientFos.setContext(context);
      setOutputStream(resilientFos);
    }
  }

  /**
   * @see #setPrudent(boolean)
   * 
   * @return true if in prudent mode
   */
  public boolean isPrudent() {
    return prudent;
  }

  /**
   * When prudent is set to true, file appenders from multiple JVMs can safely
   * write to the same file.
   * 
   * @param prudent
   */
  public void setPrudent(boolean prudent) {
    this.prudent = prudent;
  }

  public void setAppend(boolean append) {
    this.append = append;
  }

  private void safeWrite(E event) throws IOException {
    ResilientFileOutputStream resilientFOS = (ResilientFileOutputStream) getOutputStream();
    FileChannel fileChannel = resilientFOS.getChannel();
    if (fileChannel == null) {
      return;
    }
    FileLock fileLock = null;
    try {
      fileLock = fileChannel.lock();
      long position = fileChannel.position();
      long size = fileChannel.size();
      if (size != position) {
        fileChannel.position(size);
      }
      super.writeOut(event);
    } finally {
      if (fileLock != null) {
        fileLock.release();
      }
    }
  }

  @Override
  protected void writeOut(E event) throws IOException {
    if (prudent) {
      safeWrite(event);
    } else {
      super.writeOut(event);
    }
  }
}
