/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.net;

import ch.qos.logback.core.util.CloseUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * SyslogOutputStream is a wrapper around the {@link DatagramSocket} class so
 * that it behaves like an {@link OutputStream}.
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
    private SocketException connectException;

    public SyslogOutputStream(String syslogHost, int port) throws UnknownHostException, SocketException {
        this.address = InetAddress.getByName(syslogHost);
        this.port = port;
        this.ds = new DatagramSocket();
        // This stream only sends datagrams to the syslog host. Connecting the socket removes it from
        // the list of unconnected (wildcard) UDP listeners reported by tools like `ss -ulnp`.
        // See https://github.com/qos-ch/logback/issues/939
        try {
            this.ds.connect(new InetSocketAddress(address, port));
        } catch (SocketException e) {
            // connecting may fail, e.g. if the network is not yet available. In that case
            // fall back to an unconnected socket, i.e. the behavior prior to issue 939.
            this.connectException = e;
        }
    }

    boolean isValidState() {
        if(ds == null || baos == null || address == null) {
            return false;
        }
        return true;
    }

    boolean isConnected() {
        return ds != null && ds.isConnected();
    }

    /**
     * Returns the exception thrown when connecting the underlying socket to the
     * syslog host, or null if the connection succeeded.
     */
    SocketException getConnectException() {
        return connectException;
    }

    public void write(byte[] byteArray, int offset, int len) throws IOException {
        if(!isValidState()) return;
        baos.write(byteArray, offset, len);
    }

    public void flush() throws IOException {
        if(!isValidState()) return;
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
            try {
                ds.send(packet);
            } catch (PortUnreachableException e) {
                // On a connected socket, an ICMP port unreachable message triggered by a
                // previously sent datagram is reported on the next send, in which case the
                // current packet is not sent. Retry once so that the current packet is not lost,
                // as was the case with unconnected sockets. Should the retry fail as well, the
                // exception propagates to the caller.
                ds.send(packet);
            }
        }

    }

    @Override
    public void close() {
        CloseUtil.closeQuietly(ds);
        CloseUtil.closeQuietly(baos);
        address = null;
        ds = null;
        baos = null;
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
