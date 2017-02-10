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

import ch.qos.logback.core.helpers.AsyncOutputStream;
import ch.qos.logback.core.helpers.DirectNIOByteBufferedOutputStream;
import ch.qos.logback.core.helpers.MemMappedBufferedOutputStream;
import ch.qos.logback.core.helpers.NIOByteBufferedOutputStream;

public class ResilientFileOutputStream extends ResilientOutputStreamBase {

    private File file;
    private FileOutputStream fos;
    long bufferSize;
    
    public ResilientFileOutputStream(File file, boolean append, long bufferSize) throws IOException {
        this.file = file;
        this.bufferSize = bufferSize;
        this.os = openNewOutputStream(append);
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
    OutputStream openNewOutputStream(boolean append) throws IOException {
        // see LOGBACK-765
//        fos = new FileOutputStream(file, append);
//        return new BufferedOutputStream(fos, (int) bufferSize);
//        
//        RandomAccessFile raf = new  RandomAccessFile(file, "rw");
//        MemMappedBufferedOutputStream mmbos = new MemMappedBufferedOutputStream(raf.getChannel(), 0);
//        return mmbos;
        
//        fos = new FileOutputStream(file, append);
//        return new NIOByteBufferedOutputStream(fos);

        //fos = new FileOutputStream(file, append);
        RandomAccessFile raf = new  RandomAccessFile(file, "rw");
                        
        return new DirectNIOByteBufferedOutputStream(raf.getChannel());

//        AsyncOutputStream aos = new AsyncOutputStream(file, (int) bufferSize); 
//        return aos;
    }

    @Override
    public String toString() {
        return "c.q.l.c.recovery.ResilientFileOutputStream@" + System.identityHashCode(this);
    }

}
