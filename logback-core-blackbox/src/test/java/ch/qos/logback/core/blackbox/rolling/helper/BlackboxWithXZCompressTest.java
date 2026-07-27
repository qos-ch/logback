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

package ch.qos.logback.core.blackbox.rolling.helper;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.blackbox.joran.CoreBlackboxStatusChecker;
import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.Compressor;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusPrinter2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static ch.qos.logback.core.blackbox.BlackboxCoreTestConstants.TEST_SRC_PREFIX;
import static ch.qos.logback.core.testUtil.CoreTestConstants.OUTPUT_DIR_PREFIX;

//import ch.qos.logback.core.status.testUtil.StatusChecker;

public class BlackboxWithXZCompressTest  {
    Context context = new ContextBase();
    StatusPrinter2 statusPrinter2 = new StatusPrinter2();

    final String original1 = TEST_SRC_PREFIX + "blackboxInput/compress1.original";
    final String copy1 = TEST_SRC_PREFIX + "blackboxInput/compress1.txt";
    final String compressed1 = OUTPUT_DIR_PREFIX + "compress1.txt.gz";

    final String original2 = TEST_SRC_PREFIX + "blackboxInput/compress2.original";
    final String copy2 = TEST_SRC_PREFIX + "blackboxInput/compress2.txt";
    final String compressed2 = OUTPUT_DIR_PREFIX + "compress2.txt.gz";

    final String original3 = TEST_SRC_PREFIX + "blackboxInput/compress3.original";
    final String copy3 = TEST_SRC_PREFIX + "blackboxInput/compress3.txt";
    final String compressed3 = OUTPUT_DIR_PREFIX + "compress3.txt.zip";

    final String original4 = TEST_SRC_PREFIX + "blackboxInput/compress4.original";
    final String copy4 = TEST_SRC_PREFIX + "blackboxInput/compress4.txt";
    final String compressed4 = OUTPUT_DIR_PREFIX + "compress4.txt.xz";

    int diff = RandomUtil.getPositiveInt();
    String outputDir = OUTPUT_DIR_PREFIX + "xzCompress_" + diff + "/";


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


        CoreBlackboxStatusChecker checker = new CoreBlackboxStatusChecker(context);
        Assertions.assertTrue(checker.isErrorFree(0));
        //Assertions.assertTrue(Compare.gzCompare(compressed1, TEST_SRC_PREFIX + "witness/compress1.txt.gz"));
    }

    @Test
    public void gzTest2() throws Exception {
        copySourceFilesAndDeleteCompressedOutputFiles(original2, copy2, compressed2);
        Compressor compressor = new Compressor(CompressionMode.GZ);
        compressor.setContext(context);
        compressor.compress(copy2, compressed2, null);

        CoreBlackboxStatusChecker checker = new CoreBlackboxStatusChecker(context);
        Assertions.assertTrue(checker.isErrorFree(0));

        //Assertions.assertTrue(Compare.gzCompare(compressed2, TEST_SRC_PREFIX + "witness/compress2.txt.gz"));
    }

    @Test
    public void zipTest() throws Exception {
        copySourceFilesAndDeleteCompressedOutputFiles(original3, copy3, compressed3);
        Compressor compressor = new Compressor(CompressionMode.ZIP);
        compressor.setContext(context);
        compressor.compress(copy3,  compressed3, "compress3.txt");
        CoreBlackboxStatusChecker checker = new CoreBlackboxStatusChecker(context);
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
        statusPrinter2.print(context);
        CoreBlackboxStatusChecker checker = new CoreBlackboxStatusChecker(context);
        Assertions.assertTrue(checker.isErrorFree(0));
    }


    /**
     * On compression failure the source file must be left intact.
     * <p>
     * Failure strategy: make the compressed file's parent path a regular file so
     * {@link java.io.FileOutputStream} cannot open the target (parent is not a
     * directory). That is portable, deterministic, and exercises the exception
     * path rather than the early "target already exists" abort.
     */
    @Test
    public void compressionFailureLeavesOriginalFileIntact() throws Exception {
        final String originalPath = outputDir + "compress-fail-original.txt";
        final String parentAsFilePath = outputDir + "compress-fail-parent";
        final String compressedPath = parentAsFilePath + "/compress-fail.txt";

        File originalFile = new File(originalPath);
        originalFile.getParentFile().mkdirs();
        copy(new File(original1), originalFile);

        File parentAsFile = new File(parentAsFilePath);
        Assertions.assertTrue(parentAsFile.createNewFile(),
                "parent path must be a regular file to force compression I/O failure");

        File compressedFile = new File(compressedPath);

        Compressor compressor = new Compressor(CompressionMode.XZ);
        compressor.setContext(context);
        compressor.compress(originalPath, compressedPath, null);

        CoreBlackboxStatusChecker checker = new CoreBlackboxStatusChecker(context);
        checker.assertContainsMatch(Status.ERROR, "Error occurred while compressing");
        checker.assertContainsMatch(Status.WARN,
                "Compression of \\[" + originalPath + "\\] failed. Original file left intact.");

        Assertions.assertTrue(originalFile.exists(),
                "original file must remain after failed XZ compression");
        Assertions.assertFalse(compressedFile.exists(),
                "compressed file must not be left behind after failed XZ compression");
    }
}
