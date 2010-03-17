/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
 * 
 * This program and the accompanying materials are dual-licensed under either
 * the terms of the Eclipse Public License v1.0 as published by the Eclipse
 * Foundation
 * 
 * or (per the licensee's choosing)
 * 
 * under the terms of the GNU Lesser General Public License version 2.1 as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.helpers;

import java.io.IOException;
import java.io.OutputStream;

import ch.qos.logback.core.joran.spi.ConsoleTarget;

/**
 * An {@link OutputStream} which always outputs to the current value of
 * System.out/System.err.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Tom SH Liu
 */
public class ConsoleOutputStreamWrapper extends OutputStream {

  ConsoleTarget consoleTarget;

  public ConsoleOutputStreamWrapper(ConsoleTarget consoleTarget) {
    this.consoleTarget = consoleTarget;
  }

  private OutputStream getOutputStream() {
    switch (consoleTarget) {
    case SystemOut:
      return System.out;
    case SystemErr:
      return System.err;
    }
    throw new IllegalStateException("Unpexpected consoleTarget value ["
        + consoleTarget + "]");
  }

  @Override
  public void write(int b) throws IOException {
    getOutputStream().write(b);
  }

  @Override
  public void write(byte b[]) throws IOException {
    this.write(b, 0, b.length);
  }

  @Override
  public void write(byte b[], int off, int len) throws IOException {
    getOutputStream().write(b, off, len);
  }

  @Override
  public void flush() throws IOException {
    getOutputStream().flush();
  }

  @Override
  public void close() throws IOException {
    // the console is not ours to close
  }
}
