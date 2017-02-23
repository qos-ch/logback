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
package ch.qos.logback.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This stream writes its output to the target PrintStream supplied to its
 * constructor. At the same time, all the available bytes are collected and
 * returned by the toString() method.
 * 
 * @author Ceki Gulcu
 */
public class TeeOutputStream extends OutputStream {

    final PrintStream targetPS;
    public final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public TeeOutputStream(PrintStream targetPS) {
        // allow for null arguments
        this.targetPS = targetPS;
    }

    public void write(int b) throws IOException {
        baos.write(b);
        if (targetPS != null) {
            targetPS.write(b);
        }
    }

    public String toString() {
        return baos.toString();
    }

    public byte[] toByteArray() {
        return baos.toByteArray();
    }
}
