/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.rolling.helper;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.Compare;
import ch.qos.logback.core.util.StatusPrinter2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.*;

import static ch.qos.logback.core.rolling.helper.Compressor.COULD_NOT_OBTAIN_COMPRESSION_STRATEGY_MESSAGE;
import static ch.qos.logback.core.rolling.helper.Compressor.XZ_COMPRESSION_STRATEGY_CLASS_NAME;
import static ch.qos.logback.core.testUtil.CoreTestConstants.OUTPUT_DIR_PREFIX;
import static ch.qos.logback.core.testUtil.CoreTestConstants.TEST_SRC_PREFIX;

/**
 * @author Ceki Gulcu
 */
public class JDKOnlyCompressTest {
    Context context = new ContextBase();
    StatusPrinter2 statusPrinter2 = new StatusPrinter2();

    final String original1 = TEST_SRC_PREFIX + "input/compress1.original";
    final String copy1 = TEST_SRC_PREFIX + "input/compress1.txt";
    final String compressed1 = OUTPUT_DIR_PREFIX + "compress1.txt.gz";

    final String original2 = TEST_SRC_PREFIX + "input/compress2.original";
    final String copy2 = TEST_SRC_PREFIX + "input/compress2.txt";
    final String compressed2 = OUTPUT_DIR_PREFIX + "compress2.txt.gz";

    final String original3 = TEST_SRC_PREFIX + "input/compress3.original";
    final String copy3 = TEST_SRC_PREFIX + "input/compress3.txt";
    final String compressed3 = OUTPUT_DIR_PREFIX + "compress3.txt.zip";

    final String original4 = TEST_SRC_PREFIX + "input/compress4.original";
    final String copy4 = TEST_SRC_PREFIX + "input/compress4.txt";
    final String compressed4 = OUTPUT_DIR_PREFIX + "compress4.txt.xz";

    int diff = RandomUtil.getPositiveInt();
    String outputDir = OUTPUT_DIR_PREFIX + "compress_" + diff + "/";

    @BeforeEach
    public void setUp() throws IOException {

    }

    protected void copySourceFilesAndDeleteCompressedOutputFiles(String originalPathStr, String copyPathStr, String compressedStr) throws IOException {
        // Copy source files
        // Delete output files

        File originalFile = new File(originalPathStr);
        File copyFile = new File(copyPathStr);
        copy(originalFile, copyFile);
        File compressedFile = new File(compressedStr);
        compressedFile.mkdirs();
        compressedFile.delete();
    }

    protected void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dst);) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    @Test
    public void gzTest1() throws Exception {
        copySourceFilesAndDeleteCompressedOutputFiles(original1, copy1, compressed1);
        Compressor compressor = new Compressor(CompressionMode.GZ);
        compressor.setContext(context);
        compressor.compress(copy1, compressed1, null);


        StatusChecker checker = new StatusChecker(context);
        Assertions.assertTrue(checker.isErrorFree(0));
        Assertions.assertTrue(Compare.gzCompare(compressed1, TEST_SRC_PREFIX + "witness/compress1.txt.gz"));
    }

    @Test
    public void gzTest2() throws Exception {
        copySourceFilesAndDeleteCompressedOutputFiles(original2, copy2, compressed2);
        Compressor compressor = new Compressor(CompressionMode.GZ);
        compressor.setContext(context);
        compressor.compress(copy2, compressed2, null);

        StatusChecker checker = new StatusChecker(context);
        Assertions.assertTrue(checker.isErrorFree(0));

        Assertions.assertTrue(Compare.gzCompare(compressed2, TEST_SRC_PREFIX + "witness/compress2.txt.gz"));
    }

    @Test
    public void zipTest() throws Exception {
        copySourceFilesAndDeleteCompressedOutputFiles(original3, copy3, compressed3);
        Compressor compressor = new Compressor(CompressionMode.ZIP);
        compressor.setContext(context);
        compressor.compress(copy3,  compressed3, "compress3.txt");
        StatusChecker checker = new StatusChecker(context);
        Assertions.assertTrue(checker.isErrorFree(0));

        // we don't know how to compare .zip files
        // Assertions.assertTrue(Compare.compare(CoreTestConstants.OUTPUT_DIR_PREFIX
        // + "compress3.txt.zip", CoreTestConstants.TEST_SRC_PREFIX
        // + "witness/compress3.txt.zip"));
    }

    @Test
    public void xzTest() throws Exception {
        copySourceFilesAndDeleteCompressedOutputFiles(original4, copy4, compressed4);
        Compressor compressor = new Compressor(CompressionMode.XZ);
        compressor.setContext(context);
        compressor.compress(copy4, compressed4, null);
        StatusChecker checker = new StatusChecker(context);
        //statusPrinter2.print(context);
        checker.assertContainsMatch(Status.ERROR, "Could not instantiate "+XZ_COMPRESSION_STRATEGY_CLASS_NAME);
        checker.assertContainsMatch(Status.WARN, COULD_NOT_OBTAIN_COMPRESSION_STRATEGY_MESSAGE);
    }

    /**
     * On compression failure the source file must be left intact.
     * <p>
     * Failure strategy: make the compressed file's parent path a regular file so
     * {@link java.io.FileOutputStream} cannot open the target (parent is not a
     * directory). That is portable, deterministic, and exercises the exception
     * path rather than the early "target already exists" abort.
     */
    @ParameterizedTest
    @EnumSource(value = CompressionMode.class, names = { "GZ", "ZIP" })
    public void compressionFailureLeavesOriginalFileIntact(CompressionMode compressionMode) throws Exception {
        final String suffix = compressionMode == CompressionMode.GZ ? CompressionMode.GZ_SUFFIX : CompressionMode.ZIP_SUFFIX;
        final String originalPath = outputDir + "compress-fail-original.txt";
        final String parentAsFilePath = outputDir + "compress-fail-parent";
        final String compressedPath = parentAsFilePath + "/compress-fail.txt" + suffix;
        final String innerEntryName = compressionMode == CompressionMode.ZIP ? "compress-fail-original.txt" : null;

        File originalFile = new File(originalPath);
        originalFile.getParentFile().mkdirs();
        copy(new File(original1), originalFile);

        File parentAsFile = new File(parentAsFilePath);
        Assertions.assertTrue(parentAsFile.createNewFile(),
                "parent path must be a regular file to force compression I/O failure");

        File compressedFile = new File(compressedPath);

        Compressor compressor = new Compressor(compressionMode);
        compressor.setContext(context);
        compressor.compress(originalPath, compressedPath, innerEntryName);

        StatusChecker checker = new StatusChecker(context);
        checker.assertContainsMatch(Status.ERROR, "Error occurred while compressing");
        checker.assertContainsMatch(Status.WARN,
                "Compression of \\[" + originalPath + "\\] failed. Original file left intact.");

        Assertions.assertTrue(originalFile.exists(),
                "original file must remain after failed " + compressionMode + " compression");
        Assertions.assertTrue(Compare.compare(originalPath, original1),
                "original file content must be unchanged after failed " + compressionMode + " compression");
        Assertions.assertFalse(compressedFile.exists(),
                "compressed file must not be left behind after failed " + compressionMode + " compression");
    }

}
