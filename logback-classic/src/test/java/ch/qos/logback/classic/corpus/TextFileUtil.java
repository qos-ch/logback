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
import java.util.List;

public class TextFileUtil {

    public static List<String> toWords(URL url) throws IOException {
        InputStream is = url.openStream();
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(reader);
        return toWords(br);
    }

    public static List<String> toWords(String filename) throws IOException {
        FileReader fr = new FileReader(filename);
        BufferedReader br = new BufferedReader(fr);
        return toWords(br);
    }

    public static List<String> toWords(BufferedReader br) throws IOException {

        // (\\d+)$
        // String regExp = "^(\\d+) "+ msg + " ([\\dabcdef-]+)$";
        // Pattern p = Pattern.compile(regExp);
        String line;

        List<String> wordList = new ArrayList<String>();

        while ((line = br.readLine()) != null) {
            // line = line.replaceAll("\\p{Punct}+", " ");
            String[] words = line.split("\\s");
            for (String word : words) {
                wordList.add(word);
            }
        }
        br.close();

        return wordList;
    }
}
