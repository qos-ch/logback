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

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.DatagramSocket;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SyslogOutputStreamTest {

    /**
     * close() must release the underlying DatagramSocket instead of merely
     * dropping the reference. Relying on finalization leaks one UDP socket
     * file descriptor per appender stop (e.g. on every configuration reload)
     * and per ResilientSyslogOutputStream recovery cycle.
     */
    @Test
    public void closeReleasesUnderlyingDatagramSocket() throws Exception {
        SyslogOutputStream sos = new SyslogOutputStream("localhost", SyslogConstants.SYSLOG_PORT);
        DatagramSocket ds = extractDatagramSocket(sos);
        assertFalse(ds.isClosed());

        sos.close();

        assertTrue(ds.isClosed(), "close() should close the underlying DatagramSocket");
    }

    @Test
    public void writeAndFlushAfterCloseAreHarmless() throws Exception {
        SyslogOutputStream sos = new SyslogOutputStream("localhost", SyslogConstants.SYSLOG_PORT);
        sos.close();

        assertDoesNotThrow(() -> {
            sos.write("hello".getBytes());
            sos.flush();
            sos.close(); // close must be idempotent
        });
    }

    private DatagramSocket extractDatagramSocket(SyslogOutputStream sos) throws Exception {
        Field dsField = SyslogOutputStream.class.getDeclaredField("ds");
        dsField.setAccessible(true);
        return (DatagramSocket) dsField.get(sos);
    }
}
