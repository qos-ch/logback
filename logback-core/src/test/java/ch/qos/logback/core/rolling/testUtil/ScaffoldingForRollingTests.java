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

import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import ch.qos.logback.core.testUtil.FileToBufferUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Scaffolding for various rolling tests. Some assumptions are made: - rollover
 * periodicity is 1 second (without precluding size based roll-over)
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class ScaffoldingForRollingTests extends ParentScaffoldingForRollingTests {

    static public final String DATE_PATTERN_WITH_SECONDS = "yyyy-MM-dd_HH_mm_ss";
    static public final String DATE_PATTERN_BY_DAY = "yyyy-MM-dd";
    static public final SimpleDateFormat SDF = new SimpleDateFormat(DATE_PATTERN_WITH_SECONDS);
    public static final int MILLIS_IN_ONE_SECOND = 1000;


    protected long nextRolloverThreshold; // initialized in setUp()

    public void setUp() {
        super.setUp();
        recomputeRolloverThreshold(currentTime);
    }

    public static void existenceCheck(String filename) {
        assertTrue(new File(filename).exists(), "File " + filename + " does not exist");
    }

    public static File[] getFilesInDirectory(String outputDirStr) {
        File outputDir = new File(outputDirStr);
        return outputDir.listFiles();
    }

    public static void fileContentCheck(File[] fileArray, int runLength, String prefix) throws IOException {
        fileContentCheck(fileArray, runLength, prefix, 0);
    }

    public static void fileContentCheck(File[] fileArray, int runLength, String prefix, int runStart)
            throws IOException {
        List<String> stringList = new ArrayList<String>();
        for (File file : fileArray) {
            FileToBufferUtil.readIntoList(file, stringList);
        }

        List<String> witnessList = new ArrayList<String>();

        for (int i = runStart; i < runLength; i++) {
            witnessList.add(prefix + i);
        }
        assertEquals(witnessList, stringList);
    }

    public static void sortedContentCheck(String outputDirStr, int runLength, String prefix) throws IOException {
        sortedContentCheck(outputDirStr, runLength, prefix, 0);
    }

    public static void sortedContentCheck(String outputDirStr, int runLength, String prefix, int runStart)
            throws IOException {
        File[] fileArray = getFilesInDirectory(outputDirStr);
        FileFilterUtil.sortFileArrayByName(fileArray);
        fileContentCheck(fileArray, runLength, prefix, runStart);
    }

    public static int existenceCount(List<String> filenameList) {
        int existenceCounter = 0;
        for (String filename : filenameList) {
            if (new File(filename).exists()) {
                existenceCounter++;
            }
        }
        return existenceCounter;
    }

    protected String nullFileName(String testId) {
        return null;
    }

    protected String impossibleFileName(String testId) {
        throw new RuntimeException("implement");
    }

    // assuming rollover every second
    protected void recomputeRolloverThreshold(long ct) {
        long delta = ct % MILLIS_IN_ONE_SECOND;
        nextRolloverThreshold = (ct - delta) + MILLIS_IN_ONE_SECOND;
    }

    protected boolean passThresholdTime(long nextRolloverThreshold) {
        return currentTime >= nextRolloverThreshold;
    }

    protected Date getDateOfCurrentPeriodsStart() {
        long delta = currentTime % MILLIS_IN_ONE_SECOND;
        return new Date(currentTime - delta);
    }

    protected Date getDateOfPreviousPeriodsStart() {
        long delta = currentTime % MILLIS_IN_ONE_SECOND;
        return new Date(currentTime - delta - MILLIS_IN_ONE_SECOND);
    }

    protected long getMillisOfCurrentPeriodsStart() {
        long delta = currentTime % MILLIS_IN_ONE_SECOND;
        return (currentTime - delta);
    }

    protected void addExpectedFileNamedIfItsTime_ByDate(String fileNamePatternStr) {
        if (passThresholdTime(nextRolloverThreshold)) {
            addExpectedFileName_ByDate(fileNamePatternStr, getMillisOfCurrentPeriodsStart());
            recomputeRolloverThreshold(currentTime);
        }
    }

    protected void addExpectedFileName_ByDate(String outputDir, String testId, Date date, boolean gzExtension) {

        String fn = outputDir + testId + "-" + SDF.format(date);
        if (gzExtension) {
            fn += ".gz";
        }
        expectedFilenameList.add(fn);
    }

    protected void addExpectedFileName_ByFileIndexCounter(String randomOutputDir, String testId, long millis,
            int fileIndexCounter, String compressionSuffix) {
        String fn = randomOutputDir + testId + "-" + SDF.format(millis) + "-" + fileIndexCounter + ".txt"
                + compressionSuffix;
        expectedFilenameList.add(fn);
    }

    protected void addExpectedFileNamedIfItsTime_ByDate(String outputDir, String testId, boolean gzExtension) {
        if (passThresholdTime(nextRolloverThreshold)) {
            addExpectedFileName_ByDate(outputDir, testId, getDateOfCurrentPeriodsStart(), gzExtension);
            recomputeRolloverThreshold(currentTime);
        }
    }

    protected void massageExpectedFilesToCorresponToCurrentTarget(String testId,
            UnaryOperator<String> filenameFunction) {
        int lastIndex = expectedFilenameList.size() - 1;
        String last = expectedFilenameList.remove(lastIndex);

        String filename = filenameFunction.apply(testId);
        if (filename != null) {
            expectedFilenameList.add(filename);
        } else if (last.endsWith(".gz")) {
            int lastLen = last.length();
            String stem = last.substring(0, lastLen - 3);
            expectedFilenameList.add(stem);
        }
    }

    String addGZIfNotLast(int i) {
        int lastIndex = expectedFilenameList.size() - 1;
        if (i != lastIndex) {
            return ".gz";
        } else {
            return "";
        }
    }

    protected void checkZipEntryMatchesZipFilename(List<String> expectedFilenameList) throws IOException {
        for (String filepath : expectedFilenameList) {
            String stripped = stripStemFromZipFilename(filepath);
            checkZipEntryName(filepath, stripped);
        }
    }

    String stripStemFromZipFilename(String filepath) {
        File filepathAsFile = new File(filepath);
        String stem = filepathAsFile.getName();
        int stemLen = stem.length();
        return stem.substring(0, stemLen - ".zip".length());

    }


}
