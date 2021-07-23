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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.FileUtil;

/**
 * The <code>Compression</code> class implements ZIP, GZ and BZIP2 file
 * compression/decompression methods.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class Compressor extends ContextAwareBase {

    final CompressionMode compressionMode;

    static final int BUFFER_SIZE = 8192;

    public Compressor(CompressionMode compressionMode) {
        this.compressionMode = compressionMode;
    }

    /**
     * @param nameOfFile2Compress
     * @param nameOfCompressedFile
     * @param innerEntryName
     *            The name of the file within the zip file. Use for ZIP compression.
     */
    public void compress(String nameOfFile2Compress, String nameOfCompressedFile, String innerEntryName) {
        switch (compressionMode) {
        case BZIP2:
            bzip2Compress(nameOfFile2Compress, nameOfCompressedFile);
        case GZ:
            gzCompress(nameOfFile2Compress, nameOfCompressedFile);
            break;
        case ZIP:
            zipCompress(nameOfFile2Compress, nameOfCompressedFile, innerEntryName);
            break;
        case NONE:
            throw new UnsupportedOperationException("compress method called in NONE compression mode");
        }
    }

    private void zipCompress(String nameOfFile2zip, String nameOfZippedFile, String innerEntryName) {
        File file2zip = new File(nameOfFile2zip);

        if (!file2zip.exists()) {
            addStatus(new WarnStatus("The file to compress named [" + nameOfFile2zip + "] does not exist.", this));

            return;
        }

        if (innerEntryName == null) {
            addStatus(new WarnStatus("The innerEntryName parameter cannot be null", this));
            return;
        }

        if (!nameOfZippedFile.endsWith(".zip")) {
            nameOfZippedFile = nameOfZippedFile + ".zip";
        }

        File zippedFile = new File(nameOfZippedFile);

        if (zippedFile.exists()) {
            addStatus(new WarnStatus("The target compressed file named [" + nameOfZippedFile + "] exist already.", this));

            return;
        }

        addInfo("ZIP compressing [" + file2zip + "] as [" + zippedFile + "]");
        createMissingTargetDirsIfNecessary(zippedFile);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(nameOfFile2zip));
                        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(nameOfZippedFile))) {

            ZipEntry zipEntry = computeZipEntry(innerEntryName);
            zos.putNextEntry(zipEntry);

            byte[] inbuf = new byte[BUFFER_SIZE];
            int n;

            while ((n = bis.read(inbuf)) != -1) {
                zos.write(inbuf, 0, n);
            }

            addInfo("Done ZIP compressing [" + file2zip + "] as [" + zippedFile + "]");
        } catch (Exception e) {
            addStatus(new ErrorStatus("Error occurred while compressing [" + nameOfFile2zip + "] into [" + nameOfZippedFile + "].", this, e));
        }
        if (!file2zip.delete()) {
            addStatus(new WarnStatus("Could not delete [" + nameOfFile2zip + "].", this));
        }
    }

    // http://jira.qos.ch/browse/LBCORE-98
    // The name of the compressed file as nested within the zip archive
    //
    // Case 1: RawFile = null, Patern = foo-%d.zip
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
    ZipEntry computeZipEntry(File zippedFile) {
        return computeZipEntry(zippedFile.getName());
    }

    ZipEntry computeZipEntry(String filename) {
        String nameOfFileNestedWithinArchive = computeFileNameStrWithoutCompSuffix(filename, compressionMode);
        return new ZipEntry(nameOfFileNestedWithinArchive);
    }

    private void gzCompress(String nameOfFile2gz, String nameOfgzedFile) {
        File file2gz = new File(nameOfFile2gz);

        if (!file2gz.exists()) {
            addStatus(new WarnStatus("The file to compress named [" + nameOfFile2gz + "] does not exist.", this));

            return;
        }

        if (!nameOfgzedFile.endsWith(".gz")) {
            nameOfgzedFile = nameOfgzedFile + ".gz";
        }

        File gzedFile = new File(nameOfgzedFile);

        if (gzedFile.exists()) {
            addWarn("The target compressed file named [" + nameOfgzedFile + "] exist already. Aborting file compression.");
            return;
        }

        addInfo("GZ compressing [" + file2gz + "] as [" + gzedFile + "]");
        createMissingTargetDirsIfNecessary(gzedFile);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(nameOfFile2gz));
                        GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(nameOfgzedFile))) {

            byte[] inbuf = new byte[BUFFER_SIZE];
            int n;

            while ((n = bis.read(inbuf)) != -1) {
                gzos.write(inbuf, 0, n);
            }

            addInfo("Done ZIP compressing [" + file2gz + "] as [" + gzedFile + "]");
        } catch (Exception e) {
            addStatus(new ErrorStatus("Error occurred while compressing [" + nameOfFile2gz + "] into [" + nameOfgzedFile + "].", this, e));
        }

        if (!file2gz.delete()) {
            addStatus(new WarnStatus("Could not delete [" + nameOfFile2gz + "].", this));
        }

    }

    private void bzip2Compress(String nameOfFile2bzip, String nameOfbzippedFile) {
        File file2bzip = new File(nameOfFile2bzip);

        if (!file2bzip.exists()) {
            addStatus(new WarnStatus("The file to compress named [" + nameOfFile2bzip + "] does not exist.", this));

            return;
        }

        if (!nameOfbzippedFile.endsWith(".bz2")) {
            nameOfbzippedFile = nameOfbzippedFile + ".bz2";
        }

        File bzippedFile = new File(nameOfbzippedFile);

        if (bzippedFile.exists()) {
            addWarn("The target compressed file named [" + nameOfbzippedFile + "] exist already. Aborting file compression.");
            return;
        }

        addInfo("BZ2 compressing [" + file2bzip + "] as [" + bzippedFile + "]");
        createMissingTargetDirsIfNecessary(bzippedFile);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(nameOfFile2bzip));
                BZip2CompressorOutputStream bzos = new BZip2CompressorOutputStream(new FileOutputStream(nameOfbzippedFile))) {

            byte[] inbuf = new byte[BUFFER_SIZE];
            int n;

            while ((n = bis.read(inbuf)) != -1) {
                bzos.write(inbuf, 0, n);
            }

            addInfo("Done ZIP compressing [" + file2bzip + "] as [" + bzippedFile + "]");
        } catch (Exception e) {
            addStatus(new ErrorStatus("Error occurred while compressing [" + nameOfFile2bzip + "] into [" + nameOfbzippedFile + "].", this, e));
        }

        if (!file2bzip.delete()) {
            addStatus(new WarnStatus("Could not delete [" + nameOfFile2bzip + "].", this));
        }

    }

    static public String computeFileNameStrWithoutCompSuffix(String fileNamePatternStr, CompressionMode compressionMode) {
        int len = fileNamePatternStr.length();
        switch (compressionMode) {
        case BZIP2:
            if (fileNamePatternStr.endsWith(".bz2"))
                return fileNamePatternStr.substring(0, len - 4);
            else
                return fileNamePatternStr;
        case GZ:
            if (fileNamePatternStr.endsWith(".gz"))
                return fileNamePatternStr.substring(0, len - 3);
            else
                return fileNamePatternStr;
        case ZIP:
            if (fileNamePatternStr.endsWith(".zip"))
                return fileNamePatternStr.substring(0, len - 4);
            else
                return fileNamePatternStr;
        case NONE:
            return fileNamePatternStr;
        }
        throw new IllegalStateException("Execution should not reach this point");
    }

    void createMissingTargetDirsIfNecessary(File file) {
        boolean result = FileUtil.createMissingParentDirectories(file);
        if (!result) {
            addError("Failed to create parent directories for [" + file.getAbsolutePath() + "]");
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    public Future<?> asyncCompress(String nameOfFile2Compress, String nameOfCompressedFile, String innerEntryName) throws RolloverFailure {
        CompressionRunnable runnable = new CompressionRunnable(nameOfFile2Compress, nameOfCompressedFile, innerEntryName);
        ExecutorService executorService = context.getScheduledExecutorService();
        Future<?> future = executorService.submit(runnable);
        return future;
    }

    class CompressionRunnable implements Runnable {
        final String nameOfFile2Compress;
        final String nameOfCompressedFile;
        final String innerEntryName;

        public CompressionRunnable(String nameOfFile2Compress, String nameOfCompressedFile, String innerEntryName) {
            this.nameOfFile2Compress = nameOfFile2Compress;
            this.nameOfCompressedFile = nameOfCompressedFile;
            this.innerEntryName = innerEntryName;
        }

        public void run() {

            Compressor.this.compress(nameOfFile2Compress, nameOfCompressedFile, innerEntryName);
        }
    }

}
