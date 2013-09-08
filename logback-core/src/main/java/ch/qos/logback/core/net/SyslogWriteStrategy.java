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
package ch.qos.logback.core.net;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Syslog write and flush strategy
 *
 * @author Nicolas Labrot
 */
public class SyslogWriteStrategy {
  private boolean flushAfterMessage = true;
  private boolean flushAfterHandleThrowableFirstLine = true;
  private boolean flushAfterAppendStackTraceElement = true;

  private StringBuilder builder = new StringBuilder();

  private final int maxMessageSize;

  private final String encoding;

  private final OutputStream outputStream;

  public SyslogWriteStrategy(int maxMessageSize, String encoding, OutputStream outputStream) {
    this.maxMessageSize = maxMessageSize;
    this.encoding = encoding;
    this.outputStream = outputStream;
  }

  /**
   * Append log message
   *
   * @param msg
   * @throws IOException
   */
  public void appendMessage(String msg) throws IOException {
    builder.append(msg);

    if (flushAfterMessage) {
      writeAndFlush();
    }
  }

  /**
   * Append throwable first line
   *
   * @param msg
   * @throws IOException
   */
  public void appendHandleThrowableFirstLine(String msg) throws IOException {
    builder.append(msg);

    if (flushAfterHandleThrowableFirstLine) {
      writeAndFlush();
    }
  }

  /**
   * Append stack trace element
   *
   * @param msg
   * @throws IOException
   */
  public void appendStackTraceElement(String msg) throws IOException {
    builder.append(msg);

    if (flushAfterAppendStackTraceElement) {
      writeAndFlush();
    }

  }

  /**
   * Flush the remaining data
   *
   * @throws IOException
   */
  public void end() throws IOException {
    if (builder.length() > 0) {
      writeAndFlush();
    }
  }


  private void writeAndFlush() throws IOException {
    String msg = builder.toString();

    if (msg.length() > maxMessageSize) {
      msg = msg.substring(0, maxMessageSize);
    }
    outputStream.write(msg.getBytes(encoding));
    outputStream.flush();

    builder = new StringBuilder();
  }


  public boolean isFlushAfterMessage() {
    return flushAfterMessage;
  }

  public void setFlushAfterMessage(boolean flushAfterMessage) {
    this.flushAfterMessage = flushAfterMessage;
  }

  public boolean isFlushAfterHandleThrowableFirstLine() {
    return flushAfterHandleThrowableFirstLine;
  }

  public void setFlushAfterHandleThrowableFirstLine(boolean flushAfterHandleThrowableFirstLine) {
    this.flushAfterHandleThrowableFirstLine = flushAfterHandleThrowableFirstLine;
  }

  public boolean isFlushAfterAppendStackTraceElement() {
    return flushAfterAppendStackTraceElement;
  }

  public void setFlushAfterAppendStackTraceElement(boolean flushAfterAppendStackTraceElement) {
    this.flushAfterAppendStackTraceElement = flushAfterAppendStackTraceElement;
  }

  public void setFlushAll() {
    setFlushAfterAppendStackTraceElement(true);
    setFlushAfterHandleThrowableFirstLine(true);
    setFlushAfterMessage(true);
  }

  public void setFlushNone() {
    setFlushAfterAppendStackTraceElement(false);
    setFlushAfterHandleThrowableFirstLine(false);
    setFlushAfterMessage(false);
  }
}