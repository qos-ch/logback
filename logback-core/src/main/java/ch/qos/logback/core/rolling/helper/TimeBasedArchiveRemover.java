/**
 * Logback: the reliable, generic, fast and flexible logging framework. Copyright (C) 1999-2015, QOS.ch. All rights
 * reserved.
 *
 * This program and the accompanying materials are dual-licensed under either the terms of the Eclipse Public License
 * v1.0 as published by the Eclipse Foundation
 *
 * or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1 as published by the Free Software Foundation.
 */
package ch.qos.logback.core.rolling.helper;

import static ch.qos.logback.core.CoreConstants.UNBOUNDED_TOTAL_SIZE_CAP;

import java.io.File;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.LiteralConverter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.FileSize;

public class TimeBasedArchiveRemover extends ContextAwareBase implements ArchiveRemover {

    static protected final long UNINITIALIZED = -1;
    // aim for 32 days, except in case of hourly rollover, see
    // MAX_VALUE_FOR_INACTIVITY_PERIODS
    static protected final long INACTIVITY_TOLERANCE_IN_MILLIS = 32L * (long) CoreConstants.MILLIS_IN_ONE_DAY;
    static final int MAX_VALUE_FOR_INACTIVITY_PERIODS = 14 * 24; // 14 days in case of hourly rollover

    final FileNamePattern fileNamePattern;
    final RollingCalendar rc;
    private int maxHistory = CoreConstants.UNBOUNDED_HISTORY;
    private long totalSizeCap = CoreConstants.UNBOUNDED_TOTAL_SIZE_CAP;
    private boolean cleanLogsByLastModifiedDate = false;
    final boolean parentClean;
    long lastHeartBeat = UNINITIALIZED;

    public TimeBasedArchiveRemover(FileNamePattern fileNamePattern, RollingCalendar rc) {
        this.fileNamePattern = fileNamePattern;
        this.rc = rc;
        this.parentClean = computeParentCleaningFlag(fileNamePattern);
    }

    int callCount = 0;

    public Future<?> cleanAsynchronously(Instant now) {
        ArchiveRemoverRunnable runnable = new ArchiveRemoverRunnable(now);
        ExecutorService alternateExecutorService = context.getAlternateExecutorService();
        Future<?> future = alternateExecutorService.submit(runnable);
        return future;
    }

    /**
     * Called from the cleaning thread.
     *
     * @param now
     */
    @Override
    public void clean(Instant now) {

        long nowInMillis = now.toEpochMilli();
        // for a live appender periodsElapsed is expected to be 1
        int periodsElapsed = computeElapsedPeriodsSinceLastClean(nowInMillis);
        lastHeartBeat = nowInMillis;
        if (periodsElapsed > 1) {
            addInfo("Multiple periods, i.e. " + periodsElapsed
                    + " periods, seem to have elapsed. This is expected at application start.");
        }

        if (cleanLogsByLastModifiedDate) {
            // Delete old logs based on date the file was last modified
            cleanLogsByDateModified(now);
        } else {
            // Delete old logs based on expected file name
            for (int i = 0; i < periodsElapsed; i++) {
                int offset = getPeriodOffsetForDeletionTarget() - i;
                Instant instantOfPeriodToClean = rc.getEndOfNextNthPeriod(now, offset);
                cleanPeriod(instantOfPeriodToClean);
            }
        }
    }

    /**
     * Iterates through log files and deletes files outside the rollover window
     * Expects the file name to occur before the date specifier
     * Does not work well with file patterns that have auxiliary date specifiers
     *
     * @param now
     */
    private void cleanLogsByDateModified(Instant now) {
        File filePattern = new File(fileNamePattern.getPattern());
        String fileNameBeforeDateSpecifier = filePattern.getName().split("\\%d\\{.+\\}")[0];
        Instant cleanupCutoff = rc.getEndOfNextNthPeriod(now, getPeriodOffsetForDeletionTarget());
        
        File parentDir;
        parentDir = getParentDir(cleanupCutoff);
        if (parentDir == null) {
            addError("Cannot get parent directory");
            return;
        }
        
        File[] matchedFiles;
        matchedFiles = parentDir.listFiles((dir, name) -> name.contains(fileNameBeforeDateSpecifier));
        if (matchedFiles == null) {
            addError("Failed to find relevant log files");
            return;
        }
        
        for (File file : matchedFiles) {
            Instant lastModifiedDate = Instant.ofEpochMilli(file.lastModified());
            if (cleanupCutoff.isAfter(lastModifiedDate)) {
                checkAndDeleteFile(file);
            }
        }
        if (parentClean && matchedFiles.length > 0) {
            removeFolderIfEmpty(parentDir);
        }
    }

    protected File[] getFilesInPeriod(Instant instantOfPeriodToClean) {
        String filenameToDelete = fileNamePattern.convert(instantOfPeriodToClean);
        File file2Delete = new File(filenameToDelete);

        if (fileExistsAndIsFile(file2Delete)) {
            return new File[] { file2Delete };
        } else {
            return new File[0];
        }
    }

    private boolean fileExistsAndIsFile(File file2Delete) {
        return file2Delete.exists() && file2Delete.isFile();
    }

    public void cleanPeriod(Instant instantOfPeriodToClean) {
        File[] matchingFileArray = getFilesInPeriod(instantOfPeriodToClean);

        for (File f : matchingFileArray) {
            checkAndDeleteFile(f);
        }

        if (parentClean && matchingFileArray.length > 0) {
            File parentDir = getParentDir(matchingFileArray[0]);
            removeFolderIfEmpty(parentDir);
        }
    }

    private boolean checkAndDeleteFile(File f) {
        addInfo("deleting " + f);
        if (f == null) {
            addWarn("Cannot delete empty file");
            return false;
        } else if (!f.exists()) {
            addWarn("Cannot delete non existent file");
            return false;
        }
       
        boolean result = f.delete();
        if (!result) {
            addWarn("Failed to delete file " + f.toString());
        }
        return result;
    }

    void capTotalSize(Instant now) {
        long totalSize = 0;
        long totalRemoved = 0;
        for (int offset = 0; offset < maxHistory; offset++) {
            Instant instant = rc.getEndOfNextNthPeriod(now, -offset);
            File[] matchingFileArray = getFilesInPeriod(instant);
            descendingSort(matchingFileArray, instant);
            for (File f : matchingFileArray) {
                long size = f.length();
                if (totalSize + size > totalSizeCap) {
                    addInfo("Deleting [" + f + "]" + " of size " + new FileSize(size));
                    // assume that deletion attempt will succeed.
                    totalRemoved += size;

                    checkAndDeleteFile(f);
                }
                totalSize += size;
            }
        }
        addInfo("Removed  " + new FileSize(totalRemoved) + " of files");
    }

    protected void descendingSort(File[] matchingFileArray, Instant instant) {
        // nothing to do in super class
    }

    File getParentDir(Instant cleanupCutoff) {
        return getParentDir(new File(fileNamePattern.convert(cleanupCutoff)));
    }

    File getParentDir(File file) {
        File absolute = file.getAbsoluteFile();
        File parentDir = absolute.getParentFile();
        return parentDir;
    }

    int computeElapsedPeriodsSinceLastClean(long nowInMillis) {
        long periodsElapsed = 0;
        if (lastHeartBeat == UNINITIALIZED) {
            addInfo("first clean up after appender initialization");
            periodsElapsed = rc.periodBarriersCrossed(nowInMillis, nowInMillis + INACTIVITY_TOLERANCE_IN_MILLIS);
            periodsElapsed = Math.min(periodsElapsed, MAX_VALUE_FOR_INACTIVITY_PERIODS);
        } else {
            periodsElapsed = rc.periodBarriersCrossed(lastHeartBeat, nowInMillis);
            // periodsElapsed of zero is possible for size and time based policies
        }
        return (int) periodsElapsed;
    }

    /**
     * Computes whether the fileNamePattern may create sub-folders.
     *
     * @param fileNamePattern
     * @return
     */
    boolean computeParentCleaningFlag(FileNamePattern fileNamePattern) {
        DateTokenConverter<Object> dtc = fileNamePattern.getPrimaryDateTokenConverter();
        // if the date pattern has a /, then we need parent cleaning
        if (dtc.getDatePattern().indexOf('/') != -1) {
            return true;
        }
        // if the literal string after the dtc contains a /, we also
        // need parent cleaning

        Converter<Object> p = fileNamePattern.headTokenConverter;

        // find the date converter
        while (p != null) {
            if (p instanceof DateTokenConverter) {
                break;
            }
            p = p.getNext();
        }

        while (p != null) {
            if (p instanceof LiteralConverter) {
                String s = p.convert(null);
                if (s.indexOf('/') != -1) {
                    return true;
                }
            }
            p = p.getNext();
        }

        // no '/', so we don't need parent cleaning
        return false;
    }

    void removeFolderIfEmpty(File dir) {
        removeFolderIfEmpty(dir, 0);
    }

    /**
     * Will remove the directory passed as parameter if empty. After that, if the parent is also becomes empty, remove
     * the parent dir as well but at most 3 times.
     *
     * @param dir
     * @param depth
     */
    private void removeFolderIfEmpty(File dir, int depth) {
        // we should never go more than 3 levels higher
        if (depth >= 3) {
            return;
        }
        if (dir.isDirectory() && FileFilterUtil.isEmptyDirectory(dir)) {
            addInfo("deleting folder [" + dir + "]");
            checkAndDeleteFile(dir);
            removeFolderIfEmpty(dir.getParentFile(), depth + 1);
        }
    }

    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    protected int getPeriodOffsetForDeletionTarget() {
        return -maxHistory - 1;
    }

    public void setTotalSizeCap(long totalSizeCap) {
        this.totalSizeCap = totalSizeCap;
    }

    public void setCleanLogsByLastModifiedDate(boolean cleanLogsByLastModifiedDate) {
        this.cleanLogsByLastModifiedDate = cleanLogsByLastModifiedDate;
    }

    public String toString() {
        return "c.q.l.core.rolling.helper.TimeBasedArchiveRemover";
    }



    public class ArchiveRemoverRunnable implements Runnable {
        Instant now;

        ArchiveRemoverRunnable(Instant now) {
            this.now = now;
        }

        @Override
        public void run() {
            clean(now);
            if (totalSizeCap != UNBOUNDED_TOTAL_SIZE_CAP && totalSizeCap > 0) {
                capTotalSize(now);
            }
        }
    }

}
