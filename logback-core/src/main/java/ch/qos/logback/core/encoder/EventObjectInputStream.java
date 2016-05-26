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

import static ch.qos.logback.core.CoreConstants.BYTES_PER_INT;
import static ch.qos.logback.core.encoder.ObjectStreamEncoder.START_PEBBLE;
import static ch.qos.logback.core.encoder.ObjectStreamEncoder.STOP_PEBBLE;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Restitute the contents of an input stream as java objects.
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 * @param <E>
 */
public class EventObjectInputStream<E> extends InputStream {

    NonClosableInputStream ncis;
    List<E> buffer = new ArrayList<E>();

    int index = 0;

    EventObjectInputStream(InputStream is) throws IOException {
        this.ncis = new NonClosableInputStream(is);
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException("Only the readEvent method is supported.");
    }

    /**
     * Returns the number of bytes available
     */
    public int available() throws IOException {
        return ncis.available();
    }

    public E readEvent() throws IOException {

        E event = getFromBuffer();
        if (event != null) {
            return event;
        }

        internalReset();
        int count = readHeader();
        if (count == -1) {
            return null;
        }
        readPayload(count);
        readFooter(count);
        return getFromBuffer();
    }

    private void internalReset() {
        index = 0;
        buffer.clear();
    }

    E getFromBuffer() {
        if (index >= buffer.size()) {
            return null;
        }
        return buffer.get(this.index++);
    }

    int readHeader() throws IOException {
        byte[] headerBA = new byte[4 * BYTES_PER_INT];
        // System.out.println("available="+ncis.available());
        int bytesRead = ncis.read(headerBA);
        if (bytesRead == -1) {
            return -1;
        }
        // System.out.println("**bytesRead="+bytesRead);

        // System.out.println(ByteArrayUtil.toHexString(headerBA));

        int offset = 0;
        int startPebble = ByteArrayUtil.readInt(headerBA, offset);
        if (startPebble != START_PEBBLE) {
            throw new IllegalStateException("Does not look like data created by ObjectStreamEncoder");
        }
        offset += BYTES_PER_INT;
        int count = ByteArrayUtil.readInt(headerBA, offset);
        offset += BYTES_PER_INT;
        int endPointer = ByteArrayUtil.readInt(headerBA, offset);
        offset += BYTES_PER_INT;
        int checksum = ByteArrayUtil.readInt(headerBA, offset);
        if (checksum != (START_PEBBLE ^ count)) {
            throw new IllegalStateException("Invalid checksum");
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    E readEvents(ObjectInputStream ois) throws IOException {
        E e = null;
        try {
            e = (E) ois.readObject();
            buffer.add(e);
        } catch (ClassNotFoundException e1) {
            // FIXME Auto-generated catch block
            e1.printStackTrace();
        }
        return e;
    }

    void readFooter(int count) throws IOException {
        byte[] headerBA = new byte[2 * BYTES_PER_INT];
        ncis.read(headerBA);

        int offset = 0;
        int stopPebble = ByteArrayUtil.readInt(headerBA, offset);
        if (stopPebble != STOP_PEBBLE) {
            throw new IllegalStateException("Looks like a corrupt stream");
        }
        offset += BYTES_PER_INT;
        int checksum = ByteArrayUtil.readInt(headerBA, offset);
        if (checksum != (STOP_PEBBLE ^ count)) {
            throw new IllegalStateException("Invalid checksum");
        }
    }

    void readPayload(int count) throws IOException {
        List<E> eventList = new ArrayList<E>(count);
        ObjectInputStream ois = new ObjectInputStream(ncis);
        for (int i = 0; i < count; i++) {
            E e = (E) readEvents(ois);
            eventList.add(e);
        }
        ois.close();
    }

    public void close() throws IOException {
        ncis.realClose();
    }

}
