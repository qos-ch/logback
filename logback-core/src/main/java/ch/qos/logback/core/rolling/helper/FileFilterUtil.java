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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileFilterUtil {

	public static void sortFileArrayByName(final File[] fileArray) {
		Arrays.sort(fileArray, (o1, o2) -> {
			final String o1Name = o1.getName();
			final String o2Name = o2.getName();
			return o1Name.compareTo(o2Name);
		});
	}

	public static void reverseSortFileArrayByName(final File[] fileArray) {
		Arrays.sort(fileArray, (o1, o2) -> {
			final String o1Name = o1.getName();
			final String o2Name = o2.getName();
			return o2Name.compareTo(o1Name);
		});
	}

	public static String afterLastSlash(final String sregex) {
		final int i = sregex.lastIndexOf('/');
		if (i == -1) {
			return sregex;
		}
		return sregex.substring(i + 1);
	}

	static public boolean isEmptyDirectory(final File dir) {
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("[" + dir + "] must be a directory");
		}
		final String[] filesInDir = dir.list();
		if (filesInDir == null || filesInDir.length == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Return the set of files matching the stemRegex as found in 'directory'. A
	 * stemRegex does not contain any slash characters or any folder separators.
	 *
	 * @param file
	 * @param stemRegex
	 * @return
	 */
	public static File[] filesInFolderMatchingStemRegex(final File file, final String stemRegex) {

		if ((file == null) || !file.exists() || !file.isDirectory()) {
			return new File[0];
		}
		return file.listFiles((FilenameFilter) (dir, name) -> name.matches(stemRegex));
	}

	static public int findHighestCounter(final File[] matchingFileArray, final String stemRegex) {
		int max = Integer.MIN_VALUE;
		for (final File aFile : matchingFileArray) {
			final int aCounter = FileFilterUtil.extractCounter(aFile, stemRegex);
			if (max < aCounter) {
				max = aCounter;
			}
		}
		return max;
	}

	static public int extractCounter(final File file, final String stemRegex) {
		final Pattern p = Pattern.compile(stemRegex);
		final String lastFileName = file.getName();

		final Matcher m = p.matcher(lastFileName);
		if (!m.matches()) {
			throw new IllegalStateException("The regex [" + stemRegex + "] should match [" + lastFileName + "]");
		}
		final String counterAsStr = m.group(1);
		return Integer.parseInt(counterAsStr);
	}

	public static String slashify(final String in) {
		return in.replace('\\', '/');
	}

	public static void removeEmptyParentDirectories(final File file, final int recursivityCount) {
		// we should never go more than 3 levels higher
		if (recursivityCount >= 3) {
			return;
		}
		final File parent = file.getParentFile();
		if (parent.isDirectory() && FileFilterUtil.isEmptyDirectory(parent)) {
			parent.delete();
			removeEmptyParentDirectories(parent, recursivityCount + 1);
		}
	}
}
