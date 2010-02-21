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
package ch.qos.logback.core.recovery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class ResilientFileOutputStream extends OutputStream {

  RecoveryCoordinator recoveryCoordinator;
  boolean bufferedIO;
  int bufferSize;

  FileOutputStream fos;
  File file;
  boolean presumedClean = true;

  public ResilientFileOutputStream(File file, boolean append)
      throws FileNotFoundException {
    this.file = file;
    fos = new FileOutputStream(file, append);
  }

  public FileChannel getChannel() {
    if (fos == null) {
      return null;
    }
    return fos.getChannel();
  }

  public void write(byte b[], int off, int len) throws IOException {
    // existence of recoveryCoordinator indicates failed state
    if (recoveryCoordinator != null && !presumedClean) {
      if (!recoveryCoordinator.isTooSoon()) {
        performRecoveryAttempt();
      }
      // we return regardless of the success of the recovery attempt
      return;
    }

    try {
      fos.write(b, off, len);
      postSuccessfulWrite();
    } catch (IOException e) {
      presumedClean = false;
      recoveryCoordinator = new RecoveryCoordinator();
    }
  }

  private void postSuccessfulWrite() {
    recoveryCoordinator = null;
  }

  @Override
  public void close() throws IOException {
    if (fos != null) {
      fos.close();
    }
  }

  @Override
  public void write(int b) throws IOException {
    // existence of recoveryCoordinator indicates failed state
    if (recoveryCoordinator != null) {
      if (!recoveryCoordinator.isTooSoon()) {
        performRecoveryAttempt();
      }
      // we return regardless of the success of the recovery attempt
      return;
    }
    try {
      fos.write(b);
      postSuccessfulWrite();
    } catch (IOException e) {
      recoveryCoordinator = new RecoveryCoordinator();
    }
  }

  void performRecoveryAttempt() throws FileNotFoundException {
    try {
      close();
    } catch (IOException e) {
    }

    // subsequent writes must always be in append mode
    fos = new FileOutputStream(file, true);
    presumedClean = true;
  }

}
