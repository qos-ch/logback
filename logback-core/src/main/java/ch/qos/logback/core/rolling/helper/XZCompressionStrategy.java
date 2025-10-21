/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2025, QOS.ch. All rights reserved.
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

/**
 * Compresses files using {@link org.tukaani.xz xz} library.
 *
 * <p>Note that </p>
 *
 * @author Marian Kazimir
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.5.18
 */
public class XZCompressionStrategy extends CompressionStrategyBase {

    @Override
    public void compress(String nameOfFile2xz, String nameOfxzedFile, String innerEntryName) {
        File file2xz = new File(nameOfFile2xz);

        if (!file2xz.exists()) {
            addWarn("The file to compress named [" + nameOfFile2xz + "] does not exist.");

            return;
        }

        if (!nameOfxzedFile.endsWith(".xz")) {
            nameOfxzedFile = nameOfxzedFile + ".xz";
        }

        File xzedFile = new File(nameOfxzedFile);

        if (xzedFile.exists()) {
            addWarn("The target compressed file named [" + nameOfxzedFile + "] exist already. Aborting file compression.");
            return;
        }

        addInfo("XZ compressing [" + file2xz + "] as [" + xzedFile + "]");
        createMissingTargetDirsIfNecessary(xzedFile);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(nameOfFile2xz));
             XZOutputStream xzos = new XZOutputStream(new FileOutputStream(nameOfxzedFile), new LZMA2Options())) {

            byte[] inbuf = new byte[BUFFER_SIZE];
            int n;

            while ((n = bis.read(inbuf)) != -1) {
                xzos.write(inbuf, 0, n);
            }
        } catch (Exception e) {
            addError("Error occurred while compressing [" + nameOfFile2xz + "] into [" + nameOfxzedFile + "].", e);
        }

        if (!file2xz.delete()) {
            addWarn("Could not delete [" + nameOfFile2xz + "].");
        }
    }
}
