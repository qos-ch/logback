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
package ch.qos.logback.core.util;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResilienceUtil {

	static public void verify(final String logfile, final String regexp, final long totalSteps, final double successRatioLowerBound) throws NumberFormatException, IOException {
		final FileReader fr = new FileReader(logfile);
		final BufferedReader br = new BufferedReader(fr);
		final Pattern p = Pattern.compile(regexp);
		String line;

		int totalLines = 0;
		int oldNum = -1;
		int gaps = 0;
		while ((line = br.readLine()) != null) {
			final Matcher m = p.matcher(line);
			if (m.matches()) {
				totalLines++;
				final String g = m.group(1);
				final int num = Integer.parseInt(g);
				if (oldNum != -1 && num != oldNum + 1) {
					gaps++;
				}
				oldNum = num;
			}
		}
		fr.close();
		br.close();

		final int lowerLimit = (int) (totalSteps * successRatioLowerBound);
		assertTrue("totalLines=" + totalLines + " less than " + lowerLimit, totalLines > lowerLimit);

		// we want at least one gap indicating recuperation
		assertTrue("gaps=" + gaps + " less than 1", gaps >= 1);

	}
}
