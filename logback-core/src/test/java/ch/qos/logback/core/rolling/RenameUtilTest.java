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
package ch.qos.logback.core.rolling;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.rolling.helper.RenameUtil;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.util.EnvUtil;
import ch.qos.logback.core.util.StatusPrinter;

import ch.qos.logback.core.util.StatusPrinter2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RenameUtilTest {

    Encoder<Object> encoder;
    Context context = new ContextBase();
    StatusChecker statusChecker = new StatusChecker(context);
    StatusPrinter2 statusPrinter2 = new StatusPrinter2();

    long currentTime = System.currentTimeMillis();
    int diff = RandomUtil.getPositiveInt();
    protected String randomOutputDirAsStr = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/";
    protected File randomOutputDir = new File(randomOutputDirAsStr);

    @BeforeEach
    public void setUp() throws Exception {
        encoder = new EchoEncoder<Object>();
        // if this the fist test run after 'build clean up' then the
        // OUTPUT_DIR_PREFIX might be not yet created
        randomOutputDir.mkdirs();
    }

    @Test
    public void renameToNonExistingDirectory() throws IOException, RolloverFailure {
        RenameUtil renameUtil = new RenameUtil();
        renameUtil.setContext(context);

        int diff2 = RandomUtil.getPositiveInt();
        File fromFile = File.createTempFile("from" + diff, "test", randomOutputDir);

        String randomTARGETDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff2;

        renameUtil.rename(fromFile.toString(), new File(randomTARGETDir + "/to.test").toString());
        statusPrinter2.printInCaseOfErrorsOrWarnings(context);
        assertTrue(statusChecker.isErrorFree(0));
    }

    @Test // LOGBACK-1054
    public void renameLockedAbstractFile_LOGBACK_1054() throws IOException, RolloverFailure {

        // this tests only works on windows
        if(!EnvUtil.isWindows()) {
            return;
        }

        RenameUtil renameUtil = new RenameUtil();
        renameUtil.setContext(context);

        String abstractFileName = "abstract_pathname-" + diff;

        String src = CoreTestConstants.OUTPUT_DIR_PREFIX + abstractFileName;
        String target = src + ".target";

        makeFile(src);

        // open file in a way preventing simple rename in order to force call to
        // areOnDifferentVolumes() method. This only works on Windows

        FileInputStream fis = new FileInputStream(src);
        renameUtil.rename(src, target);
        // release the lock
        fis.close();

        statusPrinter2.print(context);
        assertEquals(0, statusChecker.matchCount("Parent of target file ." + target + ". is null"));
    }

    @Test
    @Disabled
    public void MANUAL_renamingOnDifferentVolumesOnLinux() throws IOException, RolloverFailure {
        RenameUtil renameUtil = new RenameUtil();
        renameUtil.setContext(context);

        String src = "/tmp/ramdisk/foo.txt";
        makeFile(src);

        renameUtil.rename(src, "/tmp/foo" + diff + ".txt");
        statusPrinter2.print(context);
    }

    @Test
    @Disabled
    public void MANUAL_renamingOnDifferentVolumesOnWindows() throws IOException, RolloverFailure {
        RenameUtil renameUtil = new RenameUtil();
        renameUtil.setContext(context);

        String src = "c:/tmp/foo.txt";
        makeFile(src);

        renameUtil.rename(src, "d:/tmp/foo" + diff + ".txt");
        statusPrinter2.print(context);
        assertTrue(statusChecker.isErrorFree(0));
    }

    private void makeFile(String src) throws FileNotFoundException, IOException {

        FileOutputStream fos = new FileOutputStream(src);
        fos.write(("hello" + diff).getBytes());
        fos.close();
    }

}
