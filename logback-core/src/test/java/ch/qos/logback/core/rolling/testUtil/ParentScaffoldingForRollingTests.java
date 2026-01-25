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

package ch.qos.logback.core.rolling.testUtil;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParentScaffoldingForRollingTests {

    protected EchoEncoder<Object> encoder = new EchoEncoder<Object>();
    protected int diff = RandomUtil.getPositiveInt();
    protected String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/";
    protected List<String> expectedFilenameList = new ArrayList<String>();

    Calendar calendar = Calendar.getInstance();
    protected Context context = new ContextBase();

    protected long currentTime; // initialized in setUp()
    protected List<Future<?>> futureList = new ArrayList<Future<?>>();

    public static void existenceCheck(List<String> filenameList) {
        for (String filename : filenameList) {
            assertTrue(new File(filename).exists(), "File " + filename + " does not exist");
        }
    }

    public static void reverseSortedContentCheck(String outputDirStr, int runLength, String prefix) throws IOException {
        File[] fileArray = ScaffoldingForRollingTests.getFilesInDirectory(outputDirStr);
        FileFilterUtil.reverseSortFileArrayByName(fileArray);
        ScaffoldingForRollingTests.fileContentCheck(fileArray, runLength, prefix);
    }

    static protected void checkZipEntryName(String filepath, String pattern) throws IOException {
        ZipFile zf = new ZipFile(filepath);

        try {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            assert ((entries.hasMoreElements()));
            ZipEntry firstZipEntry = entries.nextElement();
            assert ((!entries.hasMoreElements()));
            assertTrue(firstZipEntry.getName().matches(pattern));
        } finally {
            if (zf != null)
                zf.close();
        }
    }

    static protected void zipEntryNameCheck(List<String> expectedFilenameList, String pattern) throws IOException {
        for (String filepath : expectedFilenameList) {
            checkZipEntryName(filepath, pattern);
        }
    }

    public void setUp() {
        context.setName("test");
        calendar.set(Calendar.MILLISECOND, 333);
        currentTime = 1760822446333L; //calendar.getTimeInMillis();

    }

    protected void add(Future<?> future) {
        if (future == null)
            return;
        if (!futureList.contains(future)) {
            futureList.add(future);
        }
    }

    protected void waitForJobsToComplete() {
        for (Future<?> future : futureList) {
            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                new RuntimeException("unexpected exception while testing", e);
            }
        }
        futureList.clear();
    }

    protected List<String> filterElementsInListBySuffix(String suffix) {
        List<String> zipFiles = new ArrayList<String>();
        for (String filename : expectedFilenameList) {
            if (filename.endsWith(suffix))
                zipFiles.add(filename);
        }
        return zipFiles;
    }

    protected void addExpectedFileName_ByDate(String patternStr, long millis) {
        FileNamePattern fileNamePattern = new FileNamePattern(patternStr, context);
        String fn = fileNamePattern.convert(new Date(millis));
        expectedFilenameList.add(fn);
    }

    protected String testId2FileName(String testId) {
        return randomOutputDir + testId + ".log";
    }

    protected void incCurrentTime(long increment) {
        currentTime += increment;
    }
}
