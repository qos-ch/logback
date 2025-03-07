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
import java.util.concurrent.Future;

import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * The <code>Compression</code> class implements ZIP and GZ file
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
     * @param originalFileName
     * @param compressedFileName
     * @param innerEntryName       The name of the file within the zip file. Use for
     *                             ZIP compression.
     */
    public void compress(String originalFileName, String compressedFileName, String innerEntryName) {
        CompressionStrategy compressionStrategy = makeCompressionStrategy(compressionMode);
        if (compressionStrategy == null) {
            addWarn("Could not ");
        }
        compressionStrategy.setContext(getContext());
        compressionStrategy.compress(originalFileName, compressedFileName, innerEntryName);

    }

    CompressionStrategy makeCompressionStrategy(CompressionMode compressionMode) {
        switch (compressionMode) {
        case GZ:
            return new GZCompressionStrategy();
        case ZIP:
            return new ZipCompressionStrategy();
        case NONE:
            throw new UnsupportedOperationException("compress method called in NONE compression mode");
        default:
            return null;
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
    //    ZipEntry computeZipEntry(File zippedFile) {
    //        return computeZipEntry(zippedFile.getName());
    //    }
    //
    //    ZipEntry computeZipEntry(String filename) {
    //        String nameOfFileNestedWithinArchive = computeFileNameStrWithoutCompSuffix(filename, compressionMode);
    //        return new ZipEntry(nameOfFileNestedWithinArchive);
    //    }

    static public String computeFileNameStrWithoutCompSuffix(String fileNamePatternStr, CompressionMode compressionMode) {
        int len = fileNamePatternStr.length();
        switch (compressionMode) {
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

    //    void createMissingTargetDirsIfNecessary(File file) {
    //        boolean result = FileUtil.createMissingParentDirectories(file);
    //        if (!result) {
    //            addError("Failed to create parent directories for [" + file.getAbsolutePath() + "]");
    //        }
    //    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    public Future<?> asyncCompress(String nameOfFile2Compress, String nameOfCompressedFile, String innerEntryName) throws RolloverFailure {
        CompressionRunnable runnable = new CompressionRunnable(nameOfFile2Compress, nameOfCompressedFile, innerEntryName);
        ExecutorService executorService = context.getExecutorService();
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
