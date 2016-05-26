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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * SyslogOutputStream is a wrapper around the {@link DatagramSocket} class so that it
 * behaves like an {@link OutputStream}.
 */
public class SyslogOutputStream extends OutputStream {

    /**
     * The maximum length after which we discard the existing string buffer and
     * start anew.
     */
    private static final int MAX_LEN = 1024;

    private InetAddress address;
    private DatagramSocket ds;
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final private int port;

    public SyslogOutputStream(String syslogHost, int port) throws UnknownHostException, SocketException {
        this.address = InetAddress.getByName(syslogHost);
        this.port = port;
        this.ds = new DatagramSocket();
    }

    public void write(byte[] byteArray, int offset, int len) throws IOException {
        baos.write(byteArray, offset, len);
    }

    public void flush() throws IOException {
        byte[] bytes = baos.toByteArray();
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);

        // clean up for next round
        if (baos.size() > MAX_LEN) {
            baos = new ByteArrayOutputStream();
        } else {
            baos.reset();
        }

        // after a failure, it can happen that bytes.length is zero
        // in that case, there is no point in sending out an empty message/
        if (bytes.length == 0) {
            return;
        }
        if (this.ds != null) {
            ds.send(packet);
        }

    }

    public void close() {
        address = null;
        ds = null;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void write(int b) throws IOException {
        baos.write(b);
    }

    int getSendBufferSize() throws SocketException {
        return ds.getSendBufferSize();
    }
}
