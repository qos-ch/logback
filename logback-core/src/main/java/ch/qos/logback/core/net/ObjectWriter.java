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

/**
 * Writes objects to an output.
 *
 * @author Sebastian Gr&ouml;bler
 */
public interface ObjectWriter {

    /**
     * Writes an object to an output.
     *
     * @param object the {@link Object} to write
     * @throws IOException in case input/output fails, details are defined by the implementation
     */
    void write(Object object) throws IOException;

}
