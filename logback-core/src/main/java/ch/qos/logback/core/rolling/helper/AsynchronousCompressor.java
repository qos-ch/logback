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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsynchronousCompressor {
  Compressor compressor;

  public AsynchronousCompressor(Compressor compressor) {
    this.compressor = compressor;
  }

  public Future<?> compressAsynchronously(String nameOfFile2Compress,
      String nameOfCompressedFile, String innerEntryName) {
    ExecutorService executor = Executors.newScheduledThreadPool(1);
    Future<?> future = executor.submit(new CompressionRunnable(compressor,
        nameOfFile2Compress, nameOfCompressedFile, innerEntryName));
    executor.shutdown();
    return future;
  }

}
