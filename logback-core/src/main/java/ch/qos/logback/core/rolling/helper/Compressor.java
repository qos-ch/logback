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
import ch.qos.logback.core.util.DynamicClassLoadingException;
import ch.qos.logback.core.util.IncompatibleClassException;

import static ch.qos.logback.core.util.OptionHelper.instantiateByClassName;

/**
 * The <code>Compression</code> class implements ZIP and GZ file
 * compression/decompression methods.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class Compressor extends ContextAwareBase {

    public static final String COULD_NOT_OBTAIN_COMPRESSION_STRATEGY_MESSAGE = "Could not obtain compression strategy";
    static String XZ_COMPRESSION_STRATEGY_CLASS_NAME = "ch.qos.logback.core.rolling.helper.XZCompressionStrategy";

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
            addWarn(COULD_NOT_OBTAIN_COMPRESSION_STRATEGY_MESSAGE);
            return;
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
        case XZ:
            return dynamicInstantiation(XZ_COMPRESSION_STRATEGY_CLASS_NAME);
        case NONE:
            throw new UnsupportedOperationException("compress method called in NONE compression mode");
        default:
            return null;
        }
    }

    private CompressionStrategy dynamicInstantiation(String className) {
        try {
            return (CompressionStrategy) instantiateByClassName(className, CompressionStrategy.class, getContext());
        } catch (IncompatibleClassException | DynamicClassLoadingException e) {
            addError("Could not instantiate " + className, e);
            return null;
        }
    }

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
        case XZ:
            if (fileNamePatternStr.endsWith(".xz"))
                return fileNamePatternStr.substring(0, len - 3);
            else
                return fileNamePatternStr;
        case NONE:
            return fileNamePatternStr;
        }
        throw new IllegalStateException("Execution should not reach this point");
    }

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
