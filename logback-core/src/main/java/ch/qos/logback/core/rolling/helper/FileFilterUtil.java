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
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileFilterUtil {

    public static void sortFileArrayByName(File[] fileArray) {
        Arrays.sort(fileArray, new Comparator<File>() {
            public int compare(File o1, File o2) {
                String o1Name = o1.getName();
                String o2Name = o2.getName();
                return (o1Name.compareTo(o2Name));
            }
        });
    }

    public static void reverseSortFileArrayByName(File[] fileArray) {
        Arrays.sort(fileArray, new Comparator<File>() {
            public int compare(File o1, File o2) {
                String o1Name = o1.getName();
                String o2Name = o2.getName();
                return (o2Name.compareTo(o1Name));
            }
        });
    }

    public static String afterLastSlash(String sregex) {
        int i = sregex.lastIndexOf('/');
        if (i == -1) {
            return sregex;
        } else {
            return sregex.substring(i + 1);
        }
    }

    static public boolean isEmptyDirectory(File dir) {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("[" + dir + "] must be a directory");
        }
        String[] filesInDir = dir.list();
        if (filesInDir == null || filesInDir.length == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return the set of files matching the stemRegex as found in 'directory'. A
     * stemRegex does not contain any slash characters or any folder separators.
     * 
     * @param file
     * @param stemRegex
     * @return
     */
    public static File[] filesInFolderMatchingStemRegex(File file, final String stemRegex) {

        if (file == null) {
            return new File[0];
        }
        if (!file.exists() || !file.isDirectory()) {
            return new File[0];
        }
        return file.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.matches(stemRegex);
            }
        });
    }

    static public int findHighestCounter(File[] matchingFileArray, final String stemRegex) {
        int max = Integer.MIN_VALUE;
        for (File aFile : matchingFileArray) {
            int aCounter = FileFilterUtil.extractCounter(aFile, stemRegex);
            if (max < aCounter)
                max = aCounter;
        }
        return max;
    }

    static public int extractCounter(File file, final String stemRegex) {
        Pattern p = Pattern.compile(stemRegex);
        String lastFileName = file.getName();

        Matcher m = p.matcher(lastFileName);
        if (!m.matches()) {
            throw new IllegalStateException("The regex [" + stemRegex + "] should match [" + lastFileName + "]");
        }
        String counterAsStr = m.group(1);
        return new Integer(counterAsStr).intValue();
    }

    public static String slashify(String in) {
        return in.replace('\\', '/');
    }

    public static void removeEmptyParentDirectories(File file, int recursivityCount) {
        // we should never go more than 3 levels higher
        if (recursivityCount >= 3) {
            return;
        }
        File parent = file.getParentFile();
        if (parent.isDirectory() && FileFilterUtil.isEmptyDirectory(parent)) {
            parent.delete();
            removeEmptyParentDirectories(parent, recursivityCount + 1);
        }
    }
}
