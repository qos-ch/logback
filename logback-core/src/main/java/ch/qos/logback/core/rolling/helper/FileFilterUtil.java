/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
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

  /**
   * Return the set of files matching the stemRegex as found in 'directory'. A
   * stemRegex does not contain any slash characters or any folder seperators.
   * 
   * @param directory
   * @param stemRegex
   * @return
   */
  public static File[] filesInFolderMatchingStemRegex(File directory,
      final String stemRegex) {
    if (directory == null || directory.isDirectory()) {
      throw new IllegalArgumentException("[" + directory
          + " cannot be null or a non-directory");
    }
    File[] matchingFileArray = directory.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.matches(stemRegex);
      }
    });
    return matchingFileArray;
  }

  static public int extractCounter(File file, final String stemRegex) {
     Pattern p = Pattern.compile(stemRegex);
    String lastFileName = file.getName();
  
    Matcher m = p.matcher(lastFileName);
    if (!m.matches()) {
      throw new IllegalStateException("The regex [" + stemRegex
          + "] should match [" + lastFileName + "]");
    }
    String counterAsStr = m.group(1);
    int counter = new Integer(counterAsStr).intValue();
    return counter;
  }

  static String slashify(String in) {
    return in.replace('\\', '/');
  }
}
