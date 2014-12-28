/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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


public class CompressionRunnable implements Runnable {

  final Compressor compressor;
  final String nameOfFile2Compress;
  final String nameOfCompressedFile;
  final String innerEntryName;

  public CompressionRunnable(Compressor compressor, String nameOfFile2Compress,
                             String nameOfCompressedFile, String innerEntryName) {
    this.compressor = compressor;
    this.nameOfFile2Compress = nameOfFile2Compress;
    this.nameOfCompressedFile = nameOfCompressedFile;
    this.innerEntryName = innerEntryName;
  }

  public void run() {
    compressor.compress(nameOfFile2Compress, nameOfCompressedFile, innerEntryName);
  }
}
