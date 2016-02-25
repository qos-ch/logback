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
package ch.qos.logback.classic.multiJVM;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Checker {

    static long LEN;
    static String FILENAME;

    static void usage(String msg) {
        System.err.println(msg);
        System.err.println("Usage: java " + Checker.class.getName() + " runLength filename stamp0 stamp1 ..stampN\n"
                        + "   runLength (integer) the number of logs to generate perthread\n" + "    filename (string) the filename where to write\n"
                        + "   stamp0 JVM instance stamp0\n" + "   stamp1 JVM instance stamp1\n");
        System.exit(1);
    }

    public static void main(String[] argv) throws Exception {
        if (argv.length < 3) {
            usage("Wrong number of arguments.");
        }

        LEN = Integer.parseInt(argv[0]);
        FILENAME = argv[1];

        for (int i = 2; i < argv.length; i++) {
            check(argv[i], FILENAME, true);
        }
    }

    static void check(String stamp, String filename, boolean safetyMode) throws Exception {

        FileReader fr = new FileReader(FILENAME);
        BufferedReader br = new BufferedReader(fr);

        String regExp = "^" + stamp + " DEBUG - " + LoggingThread.msgLong + " (\\d+)$";
        Pattern p = Pattern.compile(regExp);

        String line;
        int expected = 0;
        while ((line = br.readLine()) != null) {
            Matcher m = p.matcher(line);
            if (m.matches()) {
                String g = m.group(1);
                int num = Integer.parseInt(g);
                if (num != expected) {
                    System.err.println("ERROR: out of sequence line: ");
                    System.err.println(line);
                    return;
                }
                expected++;
            }
        }

        if (expected != LEN) {
            System.err.println("ERROR: For JVM stamp " + stamp + " found " + expected + " was expecting " + LEN);
        } else {
            System.out.println("For JVM stamp " + stamp + " found " + LEN + " lines in correct sequence");
        }
        fr.close();
        br.close();
    }
}