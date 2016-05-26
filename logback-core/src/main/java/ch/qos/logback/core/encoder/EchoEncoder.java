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

import java.io.IOException;
import java.io.OutputStream;

import ch.qos.logback.core.CoreConstants;

public class EchoEncoder<E> extends EncoderBase<E> {

    String fileHeader;
    String fileFooter;

    public void doEncode(E event) throws IOException {
        String val = event + CoreConstants.LINE_SEPARATOR;
        outputStream.write(val.getBytes());
        // necessary if ResilientFileOutputStream is buffered
        outputStream.flush();
    }

    public void close() throws IOException {
        if (fileFooter == null) {
            return;
        }
        outputStream.write(fileFooter.getBytes());
    }

    public void init(OutputStream os) throws IOException {
        super.init(os);
        if (fileHeader == null) {
            return;
        }
        outputStream.write(fileHeader.getBytes());
    }
}
