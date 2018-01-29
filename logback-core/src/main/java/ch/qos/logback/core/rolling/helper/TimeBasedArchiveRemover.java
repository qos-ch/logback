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

import static ch.qos.logback.core.CoreConstants.UNBOUNDED_TOTAL_SIZE_CAP;

import java.io.File;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.LiteralConverter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.FileSize;

public class TimeBasedArchiveRemover extends ContextAwareBase implements ArchiveRemover {

    static protected final long UNINITIALIZED = -1;
    // aim for 32 days, except in case of hourly rollover
    static protected final long INACTIVITY_TOLERANCE_IN_MILLIS = 32L * (long) CoreConstants.MILLIS_IN_ONE_DAY;
    static final int MAX_VALUE_FOR_INACTIVITY_PERIODS = 14 * 24; // 14 days in case of hourly rollover

    final FileNamePattern fileNamePattern;
    final RollingCalendar rc;
    private int maxHistory = CoreConstants.UNBOUND_HISTORY;
    private long totalSizeCap = CoreConstants.UNBOUNDED_TOTAL_SIZE_CAP;
    final boolean parentClean;
    long lastHeartBeat = UNINITIALIZED;

    public TimeBasedArchiveRemover(FileNamePattern fileNamePattern, RollingCalendar rc) {
        this.fileNamePattern = fileNamePattern;
        this.rc = rc;
        this.parentClean = computeParentCleaningFlag(fileNamePattern);
    }

    int callCount = 0;
    public void clean(Date now) {
 
        long nowInMillis = now.getTime();
        // for a live appender periodsElapsed is expected to be 1
        int periodsElapsed = computeElapsedPeriodsSinceLastClean(nowInMillis);
        lastHeartBeat = nowInMillis;
        if (periodsElapsed > 1) {
            addInfo("Multiple periods, i.e. " + periodsElapsed + " periods, seem to have elapsed. This is expected at application start.");
        }
        for (int i = 0; i < periodsElapsed; i++) {
            int offset = getPeriodOffsetForDeletionTarget() - i;
            Date dateOfPeriodToClean = rc.getEndOfNextNthPeriod(now, offset);
            cleanPeriod(dateOfPeriodToClean);
        }
    }

    protected File[] getFilesInPeriod(Date dateOfPeriodToClean) {
        String filenameToDelete = fileNamePattern.convert(dateOfPeriodToClean);
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

    public void cleanPeriod(Date dateOfPeriodToClean) {
        File[] matchingFileArray = getFilesInPeriod(dateOfPeriodToClean);

        for (File f : matchingFileArray) {
            addInfo("deleting " + f);
            f.delete();
        }

        if (parentClean && matchingFileArray.length > 0) {
            File parentDir = getParentDir(matchingFileArray[0]);
            removeFolderIfEmpty(parentDir);
        }
    }

    void capTotalSize(Date now) {
        long totalSize = 0;
        long totalRemoved = 0;
        for (int offset = 0; offset < maxHistory; offset++) {
            Date date = rc.getEndOfNextNthPeriod(now, -offset);
            File[] matchingFileArray = getFilesInPeriod(date);
            descendingSort(matchingFileArray, date);
            for (File f : matchingFileArray) {
                long size = f.length();
                if (totalSize + size > totalSizeCap) {
                    addInfo("Deleting [" + f + "]" + " of size " + new FileSize(size));
                    totalRemoved += size;
                    f.delete();
                }
                totalSize += size;
            }
        }
        addInfo("Removed  " + new FileSize(totalRemoved) + " of files");
    }

    
    protected void descendingSort(File[] matchingFileArray, Date date) {
        // nothing to do in super class
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
     * @param fileNamePattern
     * @return
     */
    boolean computeParentCleaningFlag(FileNamePattern fileNamePattern) {
        DateTokenConverter<Object> dtc = fileNamePattern.getPrimaryDateTokenConverter();
        // if the date pattern has a /, then we need parent cleaning
        if (dtc.getDatePattern().indexOf('/') != -1) {
            return true;
        }
        // if the literal string subsequent to the dtc contains a /, we also
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
     * Will remove the directory passed as parameter if empty. After that, if the
     * parent is also becomes empty, remove the parent dir as well but at most 3
     * times.
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
            dir.delete();
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

    public String toString() {
        return "c.q.l.core.rolling.helper.TimeBasedArchiveRemover";
    }

    public Future<?> cleanAsynchronously(Date now) {
        ArhiveRemoverRunnable runnable = new ArhiveRemoverRunnable(now);
        ExecutorService executorService = context.getScheduledExecutorService();
        Future<?> future = executorService.submit(runnable);
        return future;
    }

    public class ArhiveRemoverRunnable implements Runnable {
        Date now;

        ArhiveRemoverRunnable(Date now) {
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
