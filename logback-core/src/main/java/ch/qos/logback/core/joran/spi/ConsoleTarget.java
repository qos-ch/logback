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
package ch.qos.logback.core.joran.spi;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The set of console output targets.

 * @author Ruediger Dohna
 * @author Ceki G&uuml;lc&uuml;
 * @author Tom SH Liu
 * @author David Roussel
 * 
 * @sse LOGBACK-136
 */
public enum ConsoleTarget {

    SystemOut("System.out", new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            System.out.write(b);
        }

        @Override
        public void write(byte b[]) throws IOException {
            System.out.write(b);
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            System.out.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            System.out.flush();
        }
    }),

    SystemErr("System.err", new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            System.err.write(b);
        }

        @Override
        public void write(byte b[]) throws IOException {
            System.err.write(b);
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            System.err.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            System.err.flush();
        }
    });

    public static ConsoleTarget findByName(String name) {
        for (ConsoleTarget target : ConsoleTarget.values()) {
            if (target.name.equalsIgnoreCase(name)) {
                return target;
            }
        }
        return null;
    }

    private final String name;
    private final OutputStream stream;

    private ConsoleTarget(String name, OutputStream stream) {
        this.name = name;
        this.stream = stream;
    }

    public String getName() {
        return name;
    }

    public OutputStream getStream() {
        return stream;
    }

    @Override
    public String toString() {
        return name;
    }
}
