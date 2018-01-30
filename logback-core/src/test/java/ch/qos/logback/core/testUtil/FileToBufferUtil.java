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
package ch.qos.logback.core.testUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileToBufferUtil {

    static public void readIntoList(File file, List<String> stringList) throws IOException {

        if (file.getName().endsWith(".gz")) {
            gzFileReadIntoList(file, stringList);
        } else if (file.getName().endsWith(".zip")) {
            zipFileReadIntoList(file, stringList);
        } else {
            regularReadIntoList(file, stringList);
        }
    }

    private static void zipFileReadIntoList(File file, List<String> stringList) throws IOException {
        System.out.println("Reading zip file [" + file + "]");
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry entry = entries.nextElement();
            readInputStream(zipFile.getInputStream(entry), stringList);
        }
    }

    static void readInputStream(InputStream is, List<String> stringList) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = in.readLine()) != null) {
            stringList.add(line);
        }
        in.close();
    }

    static public void regularReadIntoList(File file, List<String> stringList) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));
        String line;
        while ((line = in.readLine()) != null) {
            stringList.add(line);
        }
        in.close();
    }

    static public void gzFileReadIntoList(File file, List<String> stringList) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        GZIPInputStream gzis = new GZIPInputStream(fis);
        readInputStream(gzis, stringList);
    }

}
