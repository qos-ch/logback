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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.StatusChecker;
import ch.qos.logback.core.util.Compare;

/**
 * @author Ceki Gulcu
 */
public class CompressTest {

    Context context = new ContextBase();

    @Before
    public void setUp() throws IOException {
        // Copy source files
        // Delete output files
        {
            File source = new File(CoreTestConstants.TEST_SRC_PREFIX + "input/compress1.copy");
            File dest = new File(CoreTestConstants.TEST_SRC_PREFIX + "input/compress1.txt");

            copy(source, dest);
            File target = new File(CoreTestConstants.OUTPUT_DIR_PREFIX + "compress1.txt.gz");
            target.mkdirs();
            target.delete();
        }
        {
            File source = new File(CoreTestConstants.TEST_SRC_PREFIX + "input/compress2.copy");
            File dest = new File(CoreTestConstants.TEST_SRC_PREFIX + "input/compress2.txt");
            copy(source, dest);
            File target = new File(CoreTestConstants.OUTPUT_DIR_PREFIX + "compress2.txt.gz");
            target.mkdirs();
            target.delete();
        }
        {
            File source = new File(CoreTestConstants.TEST_SRC_PREFIX + "input/compress3.copy");
            File dest = new File(CoreTestConstants.TEST_SRC_PREFIX + "input/compress3.txt");
            copy(source, dest);
            File target = new File(CoreTestConstants.OUTPUT_DIR_PREFIX + "compress3.txt.zip");
            target.mkdirs();
            target.delete();
        }
    }

    @Test
    public void test1() throws Exception {
        Compressor compressor = new Compressor(CompressionMode.GZ);
        compressor.setContext(context);
        compressor.compress(CoreTestConstants.TEST_SRC_PREFIX + "input/compress1.txt", CoreTestConstants.OUTPUT_DIR_PREFIX + "compress1.txt.gz", null);

        StatusChecker checker = new StatusChecker(context);
        assertTrue(checker.isErrorFree(0));
        assertTrue(Compare.gzCompare(CoreTestConstants.OUTPUT_DIR_PREFIX + "compress1.txt.gz", CoreTestConstants.TEST_SRC_PREFIX + "witness/compress1.txt.gz"));
    }

    @Test
    public void test2() throws Exception {
        Compressor compressor = new Compressor(CompressionMode.GZ);
        compressor.setContext(context);
        compressor.compress(CoreTestConstants.TEST_SRC_PREFIX + "input/compress2.txt", CoreTestConstants.OUTPUT_DIR_PREFIX + "compress2.txt", null);

        StatusChecker checker = new StatusChecker(context);
        assertTrue(checker.isErrorFree(0));

        assertTrue(Compare.gzCompare(CoreTestConstants.OUTPUT_DIR_PREFIX + "compress2.txt.gz", CoreTestConstants.TEST_SRC_PREFIX + "witness/compress2.txt.gz"));
    }

    @Test
    public void test3() throws Exception {
        Compressor compressor = new Compressor(CompressionMode.ZIP);
        compressor.setContext(context);
        compressor.compress(CoreTestConstants.TEST_SRC_PREFIX + "input/compress3.txt", CoreTestConstants.OUTPUT_DIR_PREFIX + "compress3.txt", "compress3.txt");
        StatusChecker checker = new StatusChecker(context);
        assertTrue(checker.isErrorFree(0));

        // we don't know how to compare .zip files
        // assertTrue(Compare.compare(CoreTestConstants.OUTPUT_DIR_PREFIX
        // + "compress3.txt.zip", CoreTestConstants.TEST_SRC_PREFIX
        // + "witness/compress3.txt.zip"));
    }

    private void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

}
