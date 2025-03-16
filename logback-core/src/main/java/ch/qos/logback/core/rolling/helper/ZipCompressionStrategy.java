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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Compresses files using JDK's Zip compression algorithm.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.5.18
 */
public class ZipCompressionStrategy extends CompressionStrategyBase {
    static final int BUFFER_SIZE = 8192;

    @Override
    public void compress(String originalFileName, String compressedFileName, String innerEntryName) {

        File file2zip = new File(originalFileName);

        if (!file2zip.exists()) {
            addStatus(new WarnStatus("The file to compress named [" + originalFileName + "] does not exist.", this));

            return;
        }

        if (innerEntryName == null) {
            addStatus(new WarnStatus("The innerEntryName parameter cannot be null", this));
            return;
        }

        if (!compressedFileName.endsWith(".zip")) {
            compressedFileName = compressedFileName + ".zip";
        }

        File zippedFile = new File(compressedFileName);

        if (zippedFile.exists()) {
            addStatus(new WarnStatus("The target compressed file named [" + compressedFileName + "] exist already.", this));

            return;
        }

        addInfo("ZIP compressing [" + file2zip + "] as [" + zippedFile + "]");
        createMissingTargetDirsIfNecessary(zippedFile);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(originalFileName));
                        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(compressedFileName))) {

            ZipEntry zipEntry = computeZipEntry(innerEntryName);
            zos.putNextEntry(zipEntry);

            byte[] inbuf = new byte[BUFFER_SIZE];
            int n;

            while ((n = bis.read(inbuf)) != -1) {
                zos.write(inbuf, 0, n);
            }

            addInfo("Done ZIP compressing [" + file2zip + "] as [" + zippedFile + "]");
        } catch (Exception e) {
            addStatus(new ErrorStatus("Error occurred while compressing [" + originalFileName + "] into [" + compressedFileName + "].", this, e));
        }
        if (!file2zip.delete()) {
            addStatus(new WarnStatus("Could not delete [" + originalFileName + "].", this));
        }
    }

    // http://jira.qos.ch/browse/LBCORE-98
    // The name of the compressed file as nested within the zip archive
    //
    // Case 1: RawFile = null, Pattern = foo-%d.zip
    // nestedFilename = foo-${current-date}
    //
    // Case 2: RawFile = hello.txt, Pattern = = foo-%d.zip
    // nestedFilename = foo-${current-date}
    //
    // in both cases, the strategy consisting of removing the compression
    // suffix of zip file works reasonably well. The alternative strategy
    // whereby the nested file name was based on the value of the raw file name
    // (applicable to case 2 only) has the disadvantage of the nested files
    // all having the same name, which could make it harder for the user
    // to unzip the file without collisions
    //ZipEntry computeZipEntry(File zippedFile) {
    //    return computeZipEntry(zippedFile.getName());
    //}

    ZipEntry computeZipEntry(String filename) {
        String nameOfFileNestedWithinArchive = Compressor.computeFileNameStrWithoutCompSuffix(filename, CompressionMode.ZIP);
        return new ZipEntry(nameOfFileNestedWithinArchive);
    }
}
