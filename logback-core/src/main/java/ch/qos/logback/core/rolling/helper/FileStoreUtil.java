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
package ch.qos.logback.core.rolling.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A utility class using functionality available since JDK 1.7.
 *
 * @author ceki
 * @since 1.0.10
 */
public class FileStoreUtil {

  static boolean areOnSameFileStore(File a, File b) throws IOException {
    Path pathA = a.toPath();
    Path pathB = b.toPath();

    FileStore fileStoreA = Files.getFileStore(pathA);
    FileStore fileStoreB = Files.getFileStore(pathB);

    return fileStoreA.equals(fileStoreB);
  }
}
