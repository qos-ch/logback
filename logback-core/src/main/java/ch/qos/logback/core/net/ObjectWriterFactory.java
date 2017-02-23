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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import ch.qos.logback.core.CoreConstants;

/**
 * Factory for {@link ch.qos.logback.core.net.ObjectWriter} instances.
 *
 * @author Sebastian Gr&ouml;bler
 */
public class ObjectWriterFactory {

    /**
     * Creates a new {@link ch.qos.logback.core.net.AutoFlushingObjectWriter} instance.
     *
     * @param outputStream the underlying {@link java.io.OutputStream} to write to
     * @return a new {@link ch.qos.logback.core.net.AutoFlushingObjectWriter} instance
     * @throws IOException if an I/O error occurs while writing stream header
     */
    public AutoFlushingObjectWriter newAutoFlushingObjectWriter(OutputStream outputStream) throws IOException {
        return new AutoFlushingObjectWriter(new ObjectOutputStream(outputStream), CoreConstants.OOS_RESET_FREQUENCY);
    }
}
