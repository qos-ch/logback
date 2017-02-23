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

import ch.qos.logback.core.rolling.RolloverFailure;

import java.io.File;
import java.lang.reflect.Method;

/**
 * A utility class using functionality available since JDK 1.7.
 *
 * @author ceki
 * @since 1.0.10
 */
public class FileStoreUtil {

    static final String PATH_CLASS_STR = "java.nio.file.Path";
    static final String FILES_CLASS_STR = "java.nio.file.Files";

    /**
     * This method assumes that both files a and b exists.
     *
     * @param a
     * @param b
     * @return
     * @throws Exception
     */
    static public boolean areOnSameFileStore(File a, File b) throws RolloverFailure {
        if (!a.exists()) {
            throw new IllegalArgumentException("File [" + a + "] does not exist.");
        }
        if (!b.exists()) {
            throw new IllegalArgumentException("File [" + b + "] does not exist.");
        }

        // Implements the following by reflection
        // Path pathA = a.toPath();
        // Path pathB = b.toPath();
        //
        // FileStore fileStoreA = Files.getFileStore(pathA);
        // FileStore fileStoreB = Files.getFileStore(pathB);
        //
        // return fileStoreA.equals(fileStoreB);

        try {
            Class<?> pathClass = Class.forName(PATH_CLASS_STR);
            Class<?> filesClass = Class.forName(FILES_CLASS_STR);

            Method toPath = File.class.getMethod("toPath");
            Method getFileStoreMethod = filesClass.getMethod("getFileStore", pathClass);

            Object pathA = toPath.invoke(a);
            Object pathB = toPath.invoke(b);

            Object fileStoreA = getFileStoreMethod.invoke(null, pathA);
            Object fileStoreB = getFileStoreMethod.invoke(null, pathB);
            return fileStoreA.equals(fileStoreB);
        } catch (Exception e) {
            throw new RolloverFailure("Failed to check file store equality for [" + a + "] and [" + b + "]", e);
        }
    }
}
