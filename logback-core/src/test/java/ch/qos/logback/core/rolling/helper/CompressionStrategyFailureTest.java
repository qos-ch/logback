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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static ch.qos.logback.core.testUtil.CoreTestConstants.OUTPUT_DIR_PREFIX;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * When compression fails, the original (uncompressed) file must NOT be
 * deleted, otherwise the rolled log data is lost with only a corrupt
 * archive left behind. See LOGBACK-992.
 */
public class CompressionStrategyFailureTest {

    Context context = new ContextBase();
    File outputDir;
    File originalFile;
    File blockingFile;

    @BeforeEach
    public void setUp() throws IOException {
        outputDir = new File(OUTPUT_DIR_PREFIX + "compressFailure-" + System.nanoTime());
        assertTrue(outputDir.mkdirs());
        originalFile = new File(outputDir, "app.log");
        try (FileWriter fw = new FileWriter(originalFile)) {
            fw.write("hello world\n");
        }
        // plain file later used as the parent "directory" of the compression
        // target, guaranteeing that opening the target stream fails
        blockingFile = new File(outputDir, "blocking");
        try (FileWriter fw = new FileWriter(blockingFile)) {
            fw.write("x");
        }
    }

    private String failingTarget(String suffix) {
        return new File(blockingFile, "app.log" + suffix).getPath();
    }

    @Test
    public void originalFileSurvivesFailedGZCompression() {
        GZCompressionStrategy gz = new GZCompressionStrategy();
        gz.setContext(context);
        gz.compress(originalFile.getPath(), failingTarget(".gz"), null);

        StatusChecker checker = new StatusChecker(context);
        checker.assertContainsMatch(Status.ERROR, "Error occurred while compressing");
        assertTrue(originalFile.exists(), "original file should survive a failed GZ compression");
        assertFalse(new File(failingTarget(".gz")).exists());
    }

    @Test
    public void originalFileSurvivesFailedZipCompression() {
        ZipCompressionStrategy zip = new ZipCompressionStrategy();
        zip.setContext(context);
        zip.compress(originalFile.getPath(), failingTarget(".zip"), "app.log");

        StatusChecker checker = new StatusChecker(context);
        checker.assertContainsMatch(Status.ERROR, "Error occurred while compressing");
        assertTrue(originalFile.exists(), "original file should survive a failed ZIP compression");
        assertFalse(new File(failingTarget(".zip")).exists());
    }
}
