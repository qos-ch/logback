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
package ch.qos.logback.core.recovery;

import java.io.*;
import java.nio.channels.FileChannel;

public class ResilientFileOutputStream extends ResilientOutputStreamBase {

    private File file;
    private FileOutputStream fos;
    private CountingOutputStream countingOutputStream;
    private long originalFileLength;


    public ResilientFileOutputStream(File file, boolean append, long bufferSize) throws FileNotFoundException {
        this.file = file;
        this.originalFileLength = append ? getFileLength(file) : 0;
        fos = new FileOutputStream(file, append);
        countingOutputStream = new CountingOutputStream(new BufferedOutputStream(fos, (int) bufferSize));
        this.os = countingOutputStream;
        this.presumedClean = true;
    }

    public FileChannel getChannel() {
        if (os == null) {
            return null;
        }
        return fos.getChannel();
    }

    public File getFile() {
        return file;
    }

    public long getCount() {
        return originalFileLength + (countingOutputStream == null ? 0 : countingOutputStream.getCount());
    }

    @Override
    String getDescription() {
        return "file [" + file + "]";
    }

    @Override
    OutputStream openNewOutputStream() throws IOException {
        originalFileLength = getFileLength(file);
        // see LOGBACK-765
        fos = new FileOutputStream(file, true);
        countingOutputStream = new CountingOutputStream(new BufferedOutputStream(fos));
        return countingOutputStream;
    }

    @Override
    public String toString() {
        return "c.q.l.c.recovery.ResilientFileOutputStream@" + System.identityHashCode(this);
    }

    private static long getFileLength(File file) {
        try {
            return file.length();
        } catch (Exception ignored) {
            // file doesn't exist or we don't have permissions
            return 0L;
        }
    }
}
