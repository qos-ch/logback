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
package ch.qos.logback.core.rolling.testUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.FileToBufferUtil;
import ch.qos.logback.core.testUtil.RandomUtil;

/**
 * Scaffolding for various rolling tests. Some assumptions are made: - rollover
 * periodicity is 1 second (without precluding size based roll-over)
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class ScaffoldingForRollingTests {

    static public final String DATE_PATTERN_WITH_SECONDS = "yyyy-MM-dd_HH_mm_ss";
    static public final String DATE_PATTERN_BY_DAY = "yyyy-MM-dd";
    static public final SimpleDateFormat SDF = new SimpleDateFormat(DATE_PATTERN_WITH_SECONDS);

    int diff = RandomUtil.getPositiveInt();
    protected String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/";
    protected EchoEncoder<Object> encoder = new EchoEncoder<>();
    protected Context context = new ContextBase();
    protected List<String> expectedFilenameList = new ArrayList<>();
    protected long nextRolloverThreshold; // initialized in setUp()
    protected long currentTime; // initialized in setUp()
    protected List<Future<?>> futureList = new ArrayList<>();

    Calendar calendar = Calendar.getInstance();

    public void setUp() {
        context.setName("test");
        calendar.set(Calendar.MILLISECOND, 333);
        currentTime = calendar.getTimeInMillis();
        recomputeRolloverThreshold(currentTime);
    }

    public static void existenceCheck(final String filename) {
        assertTrue("File " + filename + " does not exist", new File(filename).exists());
    }

    public static File[] getFilesInDirectory(final String outputDirStr) {
        final File outputDir = new File(outputDirStr);
        return outputDir.listFiles();
    }

    public static void fileContentCheck(final File[] fileArray, final int runLength, final String prefix) throws IOException {
        fileContentCheck(fileArray, runLength, prefix, 0);
    }

    public static void fileContentCheck(final File[] fileArray, final int runLength, final String prefix, final int runStart) throws IOException {
        final List<String> stringList = new ArrayList<>();
        for (final File file : fileArray) {
            FileToBufferUtil.readIntoList(file, stringList);
        }

        final List<String> witnessList = new ArrayList<>();

        for (int i = runStart; i < runLength; i++) {
            witnessList.add(prefix + i);
        }
        assertEquals(witnessList, stringList);
    }

    public static void sortedContentCheck(final String outputDirStr, final int runLength, final String prefix) throws IOException {
        sortedContentCheck(outputDirStr, runLength, prefix, 0);
    }

    public static void sortedContentCheck(final String outputDirStr, final int runLength, final String prefix, final int runStart) throws IOException {
        final File[] fileArray = getFilesInDirectory(outputDirStr);
        FileFilterUtil.sortFileArrayByName(fileArray);
        fileContentCheck(fileArray, runLength, prefix, runStart);
    }

    public static void reverseSortedContentCheck(final String outputDirStr, final int runLength, final String prefix) throws IOException {
        final File[] fileArray = getFilesInDirectory(outputDirStr);
        FileFilterUtil.reverseSortFileArrayByName(fileArray);
        fileContentCheck(fileArray, runLength, prefix);
    }

    public static void existenceCheck(final List<String> filenameList) {
        for (final String filename : filenameList) {
            assertTrue("File " + filename + " does not exist", new File(filename).exists());
        }
    }

    public static int existenceCount(final List<String> filenameList) {
        int existenceCounter = 0;
        for (final String filename : filenameList) {
            if (new File(filename).exists()) {
                existenceCounter++;
            }
        }
        return existenceCounter;
    }

    protected String nullFileName(final String testId) {
        return null;
    }

    protected String impossibleFileName(final String testId) {
        throw new RuntimeException("implement");
    }

    protected String testId2FileName(final String testId) {
        return randomOutputDir + testId + ".log";
    }

    // assuming rollover every second
    protected void recomputeRolloverThreshold(final long ct) {
        final long delta = ct % 1000;
        nextRolloverThreshold = ct - delta + 1000;
    }

    protected boolean passThresholdTime(final long nextRolloverThreshold) {
        return currentTime >= nextRolloverThreshold;
    }

    protected void incCurrentTime(final long increment) {
        currentTime += increment;
    }

    protected Date getDateOfCurrentPeriodsStart() {
        final long delta = currentTime % 1000;
        return new Date(currentTime - delta);
    }

    protected Date getDateOfPreviousPeriodsStart() {
        final long delta = currentTime % 1000;
        return new Date(currentTime - delta - 1000);
    }

    protected long getMillisOfCurrentPeriodsStart() {
        final long delta = currentTime % 1000;
        return currentTime - delta;
    }

    protected void addExpectedFileName_ByDate(final String patternStr, final long millis) {
        final FileNamePattern fileNamePattern = new FileNamePattern(patternStr, context);
        final String fn = fileNamePattern.convert(new Date(millis));
        expectedFilenameList.add(fn);
    }

    protected void addExpectedFileNamedIfItsTime_ByDate(final String fileNamePatternStr) {
        if (passThresholdTime(nextRolloverThreshold)) {
            addExpectedFileName_ByDate(fileNamePatternStr, getMillisOfCurrentPeriodsStart());
            recomputeRolloverThreshold(currentTime);
        }
    }

    protected void addExpectedFileName_ByDate(final String outputDir, final String testId, final Date date, final boolean gzExtension) {

        final StringBuilder fn = new StringBuilder().append(outputDir).append(testId).append("-").append(SDF.format(date));
        if (gzExtension) {
            fn.append(".gz");
        }
        expectedFilenameList.add(fn.toString());
    }

    protected void addExpectedFileName_ByFileIndexCounter(final String randomOutputDir, final String testId, final long millis, final int fileIndexCounter, final String compressionSuffix) {
        final String fn = randomOutputDir + testId + "-" + SDF.format(millis) + "-" + fileIndexCounter + ".txt" + compressionSuffix;
        expectedFilenameList.add(fn);
    }

    protected List<String> filterElementsInListBySuffix(final String suffix) {
        final List<String> zipFiles = new ArrayList<>();
        for (final String filename : expectedFilenameList) {
            if (filename.endsWith(suffix)) {
                zipFiles.add(filename);
            }
        }
        return zipFiles;
    }

    protected void addExpectedFileNamedIfItsTime_ByDate(final String outputDir, final String testId, final boolean gzExtension) {
        if (passThresholdTime(nextRolloverThreshold)) {
            addExpectedFileName_ByDate(outputDir, testId, getDateOfCurrentPeriodsStart(), gzExtension);
            recomputeRolloverThreshold(currentTime);
        }
    }

    protected void massageExpectedFilesToCorresponToCurrentTarget(final String testId, final UnaryOperator<String> filenameFunction) {
        final int lastIndex = expectedFilenameList.size() - 1;
        final String last = expectedFilenameList.remove(lastIndex);

        final String filename = filenameFunction.apply(testId);
        if (filename != null) {
            expectedFilenameList.add(filename);
        } else if (last.endsWith(".gz")) {
            final int lastLen = last.length();
            final String stem = last.substring(0, lastLen - 3);
            expectedFilenameList.add(stem);
        }
    }

    String addGZIfNotLast(final int i) {
        final int lastIndex = expectedFilenameList.size() - 1;
        if (i != lastIndex) {
            return ".gz";
        }
        return "";
    }

    protected void zipEntryNameCheck(final List<String> expectedFilenameList, final String pattern) throws IOException {
        for (final String filepath : expectedFilenameList) {
            checkZipEntryName(filepath, pattern);
        }
    }

    protected void checkZipEntryMatchesZipFilename(final List<String> expectedFilenameList) throws IOException {
        for (final String filepath : expectedFilenameList) {
            final String stripped = stripStemFromZipFilename(filepath);
            checkZipEntryName(filepath, stripped);
        }
    }

    String stripStemFromZipFilename(final String filepath) {
        final File filepathAsFile = new File(filepath);
        final String stem = filepathAsFile.getName();
        final int stemLen = stem.length();
        return stem.substring(0, stemLen - ".zip".length());

    }

    void checkZipEntryName(final String filepath, final String pattern) throws IOException {
        final ZipFile zf = new ZipFile(filepath);

        try (zf) {
            final Enumeration<? extends ZipEntry> entries = zf.entries();
            assert entries.hasMoreElements();
            final ZipEntry firstZipEntry = entries.nextElement();
            assert !entries.hasMoreElements();
            assertTrue(firstZipEntry.getName().matches(pattern));
        }
    }

    protected void add(final Future<?> future) {
        if (future == null) {
            return;
        }
        if (!futureList.contains(future)) {
            futureList.add(future);
        }
    }

    protected void waitForJobsToComplete() {
        for (final Future<?> future : futureList) {
            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (final Exception e) {
                new RuntimeException("unexpected exception while testing", e);
            }
        }
        futureList.clear();
    }
}
