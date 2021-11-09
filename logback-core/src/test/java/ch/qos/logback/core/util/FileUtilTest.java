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
package ch.qos.logback.core.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.testUtil.CoreTestConstants;

public class FileUtilTest {

    Context context = new ContextBase();
    FileUtil fileUtil = new FileUtil(context);
    List<File> cleanupList = new ArrayList<>();
    // test-output folder is not always clean
    int diff = new Random().nextInt(10000);

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
        for (final File f : cleanupList) {
            f.delete();
        }
    }

    @Test
    public void checkParentCreationInquiryAndSubsequentCreation() {
        final File file = new File(CoreTestConstants.OUTPUT_DIR_PREFIX + "/fu" + diff + "/testing.txt");
        // these will be deleted later
        cleanupList.add(file);
        cleanupList.add(file.getParentFile());

        assertFalse(file.getParentFile().exists());
        assertTrue(FileUtil.createMissingParentDirectories(file));
        assertTrue(file.getParentFile().exists());
    }

    @Test
    public void checkDeeperParentCreationInquiryAndSubsequentCreation() {

        final File file = new File(CoreTestConstants.OUTPUT_DIR_PREFIX + "/fu" + diff + "/bla/testing.txt");
        // these will be deleted later
        cleanupList.add(file);
        cleanupList.add(file.getParentFile());
        cleanupList.add(file.getParentFile().getParentFile());

        assertFalse(file.getParentFile().exists());
        assertTrue(FileUtil.createMissingParentDirectories(file));
        assertTrue(file.getParentFile().exists());
    }

    @Test
    public void basicCopyingWorks() throws IOException {
        final String dir = CoreTestConstants.OUTPUT_DIR_PREFIX + "/fu" + diff;

        final File dirFile = new File(dir);
        dirFile.mkdir();

        final String src = CoreTestConstants.TEST_INPUT_PREFIX + "compress1.copy";
        final String target = CoreTestConstants.OUTPUT_DIR_PREFIX + "/fu" + diff + "/copyingWorks.txt";

        fileUtil.copy(src, target);
        Compare.compare(src, target);
    }

    @Test
    public void createParentDirIgnoresExistingDir() {
        final String target = CoreTestConstants.OUTPUT_DIR_PREFIX + "/fu" + diff + "/testing.txt";
        final File file = new File(target);
        cleanupList.add(file);
        file.mkdirs();
        assertTrue(file.getParentFile().exists());
        assertTrue(FileUtil.createMissingParentDirectories(file));
    }

    @Test
    public void createParentDirAcceptsNoParentSpecified() {
        final File file = new File("testing.txt");
        assertTrue(FileUtil.createMissingParentDirectories(file));
    }
}
