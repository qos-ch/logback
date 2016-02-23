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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public class Compare {
    static final int B1_NULL = -1;
    static final int B2_NULL = -2;

    public static boolean compare(String file1, String file2) throws FileNotFoundException, IOException {
        if (file1.endsWith(".gz")) {
            return gzFileCompare(file1, file2);
        } else if (file1.endsWith(".zip")) {
            return zipFileCompare(file1, file2);
        } else {
            return regularFileCompare(file1, file2);
        }
    }

    static BufferedReader gzFileToBufferedReader(String file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        GZIPInputStream gzis = new GZIPInputStream(fis);
        return new BufferedReader(new InputStreamReader(gzis));
    }

    static BufferedReader zipFileToBufferedReader(String file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ZipInputStream zis = new ZipInputStream(fis);
        zis.getNextEntry();
        return new BufferedReader(new InputStreamReader(zis));
    }

    public static boolean gzFileCompare(String file1, String file2) throws IOException {
        BufferedReader in1 = gzFileToBufferedReader(file1);
        BufferedReader in2 = gzFileToBufferedReader(file2);
        return bufferCompare(in1, in2, file1, file2);
    }

    public static boolean zipFileCompare(String file1, String file2) throws IOException {
        BufferedReader in1 = zipFileToBufferedReader(file1);
        BufferedReader in2 = zipFileToBufferedReader(file2);
        return bufferCompare(in1, in2, file1, file2);
    }

    public static boolean regularFileCompare(String file1, String file2) throws FileNotFoundException, IOException {
        BufferedReader in1 = new BufferedReader(new FileReader(file1));
        BufferedReader in2 = new BufferedReader(new FileReader(file2));
        return bufferCompare(in1, in2, file1, file2);
    }

    public static boolean bufferCompare(BufferedReader in1, BufferedReader in2, String file1, String file2) throws FileNotFoundException, IOException {

        String s1;
        int lineCounter = 0;

        while ((s1 = in1.readLine()) != null) {
            lineCounter++;

            String s2 = in2.readLine();

            if (!s1.equals(s2)) {
                System.out.println("Files [" + file1 + "] and [" + file2 + "] differ on line " + lineCounter);
                System.out.println("One reads:  [" + s1 + "].");
                System.out.println("Other reads:[" + s2 + "].");
                outputFile(file1);
                outputFile(file2);

                return false;
            }
        }

        // the second file is longer
        if (in2.read() != -1) {
            System.out.println("File [" + file2 + "] longer than file [" + file1 + "].");
            outputFile(file1);
            outputFile(file2);

            return false;
        }

        return true;
    }

    /**
     * 
     * Prints file on the console.
     * 
     */
    private static void outputFile(String file) throws FileNotFoundException, IOException {
        BufferedReader in1 = null;

        try {
            in1 = new BufferedReader(new FileReader(file));
            String s1;
            int lineCounter = 0;
            System.out.println("--------------------------------");
            System.out.println("Contents of " + file + ":");

            while ((s1 = in1.readLine()) != null) {
                lineCounter++;
                System.out.print(lineCounter);

                if (lineCounter < 10) {
                    System.out.print("   : ");
                } else if (lineCounter < 100) {
                    System.out.print("  : ");
                } else if (lineCounter < 1000) {
                    System.out.print(" : ");
                } else {
                    System.out.print(": ");
                }

                System.out.println(s1);
            }
        } finally {
            close(in1);
        }
    }

    public static boolean gzCompare(String file1, String file2) throws FileNotFoundException, IOException {
        BufferedReader in1 = null;
        BufferedReader in2 = null;
        try {
            in1 = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file1))));
            in2 = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file2))));

            String s1;
            int lineCounter = 0;

            while ((s1 = in1.readLine()) != null) {
                lineCounter++;

                String s2 = in2.readLine();

                if (!s1.equals(s2)) {
                    System.out.println("Files [" + file1 + "] and [" + file2 + "] differ on line " + lineCounter);
                    System.out.println("One reads:  [" + s1 + "].");
                    System.out.println("Other reads:[" + s2 + "].");
                    outputFile(file1);
                    outputFile(file2);

                    return false;
                }
            }

            // the second file is longer
            if (in2.read() != -1) {
                System.out.println("File [" + file2 + "] longer than file [" + file1 + "].");
                outputFile(file1);
                outputFile(file2);

                return false;
            }

            return true;
        } finally {
            close(in1);
            close(in2);
        }
    }

    static void close(Reader r) {
        if (r != null)
            try {
                r.close();
            } catch (IOException e) {
            }
    }
}
