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
package ch.qos.logback.core.net;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Automatically flushes the underlying {@link java.io.ObjectOutputStream} immediately after calling
 * it's {@link java.io.ObjectOutputStream#writeObject(Object)} method.
 *
 * @author Sebastian Gr&ouml;bler
 */
public class AutoFlushingObjectWriter implements ObjectWriter {

    private final ObjectOutputStream objectOutputStream;
    private final int resetFrequency;
    private int writeCounter = 0;

    /**
     * Creates a new instance for the given {@link java.io.ObjectOutputStream}.
     *
     * @param objectOutputStream the stream to write to
     * @param resetFrequency the frequency with which the given stream will be
     *                       automatically reset to prevent a memory leak
     */
    public AutoFlushingObjectWriter(ObjectOutputStream objectOutputStream, int resetFrequency) {
        this.objectOutputStream = objectOutputStream;
        this.resetFrequency = resetFrequency;
    }

    @Override
    public void write(Object object) throws IOException {
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
        preventMemoryLeak();
    }

    /**
     * Failing to reset the object output stream every now and then creates a serious memory leak which
     * is why the underlying stream will be reset according to the {@code resetFrequency}.
     */
    private void preventMemoryLeak() throws IOException {
        if (++writeCounter >= resetFrequency) {
            objectOutputStream.reset();
            writeCounter = 0;
        }
    }
}
