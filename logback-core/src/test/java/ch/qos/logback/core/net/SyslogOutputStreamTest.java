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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SyslogOutputStream}.
 */
public class SyslogOutputStreamTest {

    @Test
    public void connectsToTheSyslogHost() throws Exception {
        try (SyslogOutputStream stream = new SyslogOutputStream("localhost", 1514)) {
            // See #939: the underlying DatagramSocket must be connected to the syslog host so that it
            // does not appear as an unconnected listener on the wildcard address.
            assertTrue(stream.isConnected());
        }
    }

    @Test
    public void fallsBackToUnconnectedSocketWhenConnectFails() throws Exception {
        // connecting to port 0 is rejected by the JDK
        try (SyslogOutputStream stream = new SyslogOutputStream("localhost", 0)) {
            assertFalse(stream.isConnected());
            assertNotNull(stream.getConnectException());
            assertTrue(stream.isValidState());
        }
    }

    @Test
    public void flushToUnreachablePortDoesNotThrow() throws Exception {
        int port;
        try (DatagramSocket probe = new DatagramSocket(new InetSocketAddress("localhost", 0))) {
            port = probe.getLocalPort();
        }
        // nobody listens on 'port', ICMP port unreachable messages will be received
        assumeTrue(isPortUnreachableReported(port),
                "ICMP port unreachable is not reported on connected datagram sockets on this platform");
        try (SyslogOutputStream stream = new SyslogOutputStream("localhost", port)) {
            assertNull(stream.getConnectException());
            byte[] payload = "<14>hello syslog".getBytes(StandardCharsets.UTF_8);
            for (int i = 0; i < 5; i++) {
                stream.write(payload, 0, payload.length);
                stream.flush();
            }
        }
    }

    // Without the retry in SyslogOutputStream.flush(), the second send to an unreachable port
    // throws a PortUnreachableException on platforms which report ICMP errors synchronously
    // on loopback, e.g. Linux. On other platforms, flushToUnreachablePortDoesNotThrow is moot.
    private boolean isPortUnreachableReported(int port) throws Exception {
        try (DatagramSocket ds = new DatagramSocket()) {
            ds.connect(new InetSocketAddress("localhost", port));
            DatagramPacket packet = new DatagramPacket(new byte[] { 1 }, 1);
            try {
                ds.send(packet);
                ds.send(packet);
                return false;
            } catch (PortUnreachableException e) {
                return true;
            }
        }
    }

    @Test
    public void flushDeliversDatagramToTheSyslogHost() throws Exception {
        try (DatagramSocket receiver = new DatagramSocket(new InetSocketAddress("localhost", 0))) {
            receiver.setSoTimeout(5000);
            int port = receiver.getLocalPort();

            try (SyslogOutputStream stream = new SyslogOutputStream("localhost", port)) {
                byte[] payload = "<14>hello syslog".getBytes(StandardCharsets.UTF_8);
                stream.write(payload, 0, payload.length);
                stream.flush();

                byte[] buffer = new byte[payload.length + 128];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                receiver.receive(packet);
                byte[] received = Arrays.copyOf(packet.getData(), packet.getLength());

                assertEquals(new String(payload, StandardCharsets.UTF_8), new String(received, StandardCharsets.UTF_8));
            }
        }
    }

}
