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
package ch.qos.logback.core.encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.CoreConstants;

/**
 * Write out events as java objects.
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 * @param <E>
 */
public class ObjectStreamEncoder<E> extends EncoderBase<E> {

    static public final int START_PEBBLE = 1853421169;
    static public final int STOP_PEBBLE = 640373619;

    private int MAX_BUFFER_SIZE = 100;

    List<E> bufferList = new ArrayList<E>(MAX_BUFFER_SIZE);

    public void doEncode(E event) throws IOException {
        bufferList.add(event);
        if (bufferList.size() == MAX_BUFFER_SIZE) {
            writeBuffer();
        }
    }

    void writeHeader(ByteArrayOutputStream baos, int bufferSize) {
        ByteArrayUtil.writeInt(baos, START_PEBBLE);
        ByteArrayUtil.writeInt(baos, bufferSize);
        ByteArrayUtil.writeInt(baos, 0);
        ByteArrayUtil.writeInt(baos, START_PEBBLE ^ bufferSize);
    }

    void writeFooter(ByteArrayOutputStream baos, int bufferSize) {
        ByteArrayUtil.writeInt(baos, STOP_PEBBLE);
        ByteArrayUtil.writeInt(baos, STOP_PEBBLE ^ bufferSize);
    }

    void writeBuffer() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);

        int size = bufferList.size();
        writeHeader(baos, size);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        for (E e : bufferList) {
            oos.writeObject(e);
        }
        bufferList.clear();
        oos.flush();

        writeFooter(baos, size);

        byte[] byteArray = baos.toByteArray();
        oos.close();
        writeEndPosition(byteArray);
        outputStream.write(byteArray);

    }

    void writeEndPosition(byte[] byteArray) {
        int offset = 2 * CoreConstants.BYTES_PER_INT;
        ByteArrayUtil.writeInt(byteArray, offset, byteArray.length - offset);
    }

    @Override
    public void init(OutputStream os) throws IOException {
        super.init(os);
        bufferList.clear();
    }

    public void close() throws IOException {
        writeBuffer();
    }
}
