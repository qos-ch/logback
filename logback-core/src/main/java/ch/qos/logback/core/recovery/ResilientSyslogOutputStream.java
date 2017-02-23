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

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;

import ch.qos.logback.core.net.SyslogOutputStream;

public class ResilientSyslogOutputStream extends ResilientOutputStreamBase {

    String syslogHost;
    int port;

    public ResilientSyslogOutputStream(String syslogHost, int port) throws UnknownHostException, SocketException {
        this.syslogHost = syslogHost;
        this.port = port;
        super.os = new SyslogOutputStream(syslogHost, port);
        this.presumedClean = true;
    }

    @Override
    String getDescription() {
        return "syslog [" + syslogHost + ":" + port + "]";
    }

    @Override
    OutputStream openNewOutputStream() throws IOException {
        return new SyslogOutputStream(syslogHost, port);
    }

    @Override
    public String toString() {
        return "c.q.l.c.recovery.ResilientSyslogOutputStream@" + System.identityHashCode(this);
    }

}
