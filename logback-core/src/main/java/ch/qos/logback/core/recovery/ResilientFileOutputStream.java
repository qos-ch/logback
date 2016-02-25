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

    public ResilientFileOutputStream(File file, boolean append) throws FileNotFoundException {
        this.file = file;
        fos = new FileOutputStream(file, append);
        this.os = new BufferedOutputStream(fos);
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

    @Override
    String getDescription() {
        return "file [" + file + "]";
    }

    @Override
    OutputStream openNewOutputStream() throws IOException {
        // see LOGBACK-765
        fos = new FileOutputStream(file, true);
        return new BufferedOutputStream(fos);
    }

    @Override
    public String toString() {
        return "c.q.l.c.recovery.ResilientFileOutputStream@" + System.identityHashCode(this);
    }

}
