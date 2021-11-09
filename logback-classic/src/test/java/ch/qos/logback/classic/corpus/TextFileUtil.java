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
package ch.qos.logback.classic.corpus;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextFileUtil {

	public static List<String> toWords(final URL url) throws IOException {
		final InputStream is = url.openStream();
		final InputStreamReader reader = new InputStreamReader(is);
		final BufferedReader br = new BufferedReader(reader);
		return toWords(br);
	}

	public static List<String> toWords(final String filename) throws IOException {
		final FileReader fr = new FileReader(filename);
		final BufferedReader br = new BufferedReader(fr);
		return toWords(br);
	}

	public static List<String> toWords(final BufferedReader br) throws IOException {

		// (\\d+)$
		// String regExp = "^(\\d+) "+ msg + " ([\\dabcdef-]+)$";
		// Pattern p = Pattern.compile(regExp);
		String line;

		final List<String> wordList = new ArrayList<>();

		while ((line = br.readLine()) != null) {
			// line = line.replaceAll("\\p{Punct}+", " ");
			final String[] words = line.split("\\s");
			Collections.addAll(wordList, words);
		}
		br.close();

		return wordList;
	}
}
