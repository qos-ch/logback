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

import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.WarnStatus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

public class GZCompressionStrategy extends CompressionStrategyBase {

    static final int BUFFER_SIZE = 8192;

    @Override
    public void compress(String originalFileName, String compressedFileName, String innerEntryName) {

        File file2gz = new File(originalFileName);

        if (!file2gz.exists()) {
            addStatus(new WarnStatus("The file to compress named [" + originalFileName + "] does not exist.", this));

            return;
        }

        if (!compressedFileName.endsWith(".gz")) {
            compressedFileName = compressedFileName + ".gz";
        }

        File gzedFile = new File(compressedFileName);

        if (gzedFile.exists()) {
            addWarn("The target compressed file named [" + compressedFileName + "] exist already. Aborting file compression.");
            return;
        }

        addInfo("GZ compressing [" + file2gz + "] as [" + gzedFile + "]");
        createMissingTargetDirsIfNecessary(gzedFile);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(originalFileName));
                        GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(compressedFileName))) {

            byte[] inbuf = new byte[BUFFER_SIZE];
            int n;

            while ((n = bis.read(inbuf)) != -1) {
                gzos.write(inbuf, 0, n);
            }

            addInfo("Done GZ compressing [" + file2gz + "] as [" + gzedFile + "]");
        } catch (Exception e) {
            addStatus(new ErrorStatus("Error occurred while compressing [" + originalFileName + "] into [" + compressedFileName + "].", this, e));
        }

        if (!file2gz.delete()) {
            addStatus(new WarnStatus("Could not delete [" + originalFileName + "].", this));
        }

    }

    @Override
    public String computeFileNameStrWithoutCompSuffix(String fileNamePatternStr) {
        int len = fileNamePatternStr.length();
        if (fileNamePatternStr.endsWith(".gz"))
            return fileNamePatternStr.substring(0, len - 3);
        else
            return fileNamePatternStr;
    }
}
