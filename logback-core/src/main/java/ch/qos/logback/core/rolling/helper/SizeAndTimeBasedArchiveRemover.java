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

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SizeAndTimeBasedArchiveRemover extends TimeBasedArchiveRemover {

    protected static final int NO_INDEX = -1;

    public SizeAndTimeBasedArchiveRemover(FileNamePattern fileNamePattern, RollingCalendar rc) {
        super(fileNamePattern, rc);
    }

    protected File[] getFilesInPeriod(Date dateOfPeriodToClean) {
        File archive0 = new File(fileNamePattern.convertMultipleArguments(dateOfPeriodToClean, 0));
        File parentDir = getParentDir(archive0);
        String stemRegex = createStemRegex(dateOfPeriodToClean);
        File[] matchingFileArray = FileFilterUtil.filesInFolderMatchingStemRegex(parentDir, stemRegex);
        return matchingFileArray;
    }

    private String createStemRegex(final Date dateOfPeriodToClean) {
        String regex = fileNamePattern.toRegexForFixedDate(dateOfPeriodToClean);
        return FileFilterUtil.afterLastSlash(regex);
    }

    @Override
    protected void descendingSort(File[] matchingFileArray, Date date) {

        String regexForIndexExtreaction = createStemRegex(date);
        final Pattern pattern = Pattern.compile(regexForIndexExtreaction);

        Arrays.sort(matchingFileArray, new Comparator<File>() {
            @Override
            public int compare(final File f1, final File f2) {

                int index1 = extractIndex(pattern, f1);
                int index2 = extractIndex(pattern, f2);

                if (index1 == index2)
                    return 0;
                // descending sort, i.e. newest files first
                if (index2 < index1)
                    return -1;
                else
                    return 1;
            }

            private int extractIndex(Pattern pattern, File f1) {
                Matcher matcher = pattern.matcher(f1.getName());
                if (matcher.find()) {
                    String indexAsStr = matcher.group(1);
                    
                    if (indexAsStr == null || indexAsStr.isEmpty())
                        return NO_INDEX; // unreachable code?
                    else
                        return Integer.parseInt(indexAsStr);
                } else
                    return NO_INDEX;
            }
        });
    }

}
