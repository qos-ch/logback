/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;

/**
 * FileAppender appends log events to a file.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class FileAppender extends WriterAppender {

  /**
   * Append to or truncate the file? The default value for this variable is
   * <code>true</code>, meaning that by default a <code>FileAppender</code>
   * will append to an existing file and not truncate it.
   */
  protected boolean append = true;

  /**
   * The name of the active log file.
   */
  protected String fileName = null;

  /**
   * Do we do bufferedIO?
   */
  protected boolean bufferedIO = false;

  /**
   * The size of the IO buffer. Default is 8K.
   */
  protected int bufferSize = 8 * 1024;

  /**
   * As in most cases, the default constructor does nothing.
   */
  public FileAppender() {
  }

  /**
   * The <b>File</b> property takes a string value which should be the name of
   * the file to append to.
   */
  public void setFile(String file) {
    // Trim spaces from both ends. The users probably does not want
    // trailing spaces in file names.
    String val = file.trim();
    fileName = val;
  }

  /**
   * Returns the value of the <b>Append</b> option.
   */
  public boolean getAppend() {
    return append;
  }

  /** Returns the value of the <b>File</b> option. */
  public String getFile() {
    return fileName;
  }

  /**
   * If the value of <b>File</b> is not <code>null</code>, then
   * {@link #setFile} is called with the values of <b>File</b> and <b>Append</b>
   * properties.
   */
  public void start() {
    int errors = 0;
    if (fileName != null) {
      // In case both bufferedIO and immediateFlush are set, the former
      // takes priority because 'immediateFlush' is set to true by default.
      // If the user explicitly set bufferedIO, then we should follow her
      // directives.
      if (bufferedIO) {
        immediateFlush = false;
        addStatus(new InfoStatus(
            "Setting immediateFlush to false on account of bufferedIO option",
            this));
      }
      try {
        setFile();
      } catch (java.io.IOException e) {
        errors++;

        addStatus(new ErrorStatus("setFile(" + fileName + "," + append
            + ") call failed.", this, e));
      }
    } else {
      errors++;
      addStatus(new ErrorStatus("File option not set for appender [" + name
          + "].", this));
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
   * @param filename
   *          The path to the log file.
   * @param append
   *          If true will append to fileName. Otherwise will truncate fileName.
   * @param bufferedIO
   * @param bufferSize
   * 
   * @throws IOException
   * 
   */
  public synchronized void setFile() throws IOException {
    closeWriter();

    this.writer = createWriter(new FileOutputStream(fileName, append));

    if (bufferedIO) {
      this.writer = new BufferedWriter(this.writer, bufferSize);
    }
    writeHeader();
  }

  public boolean isBufferedIO() {
    return bufferedIO;
  }

  public void setBufferedIO(boolean bufferedIO) {
    this.bufferedIO = bufferedIO;
  }

  public int getBufferSize() {
    return bufferSize;
  }

  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  public void setAppend(boolean append) {
    this.append = append;
  }
}
