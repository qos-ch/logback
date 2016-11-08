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

import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * Encoders are responsible for transform an incoming event into a byte array
 * *and* writing out the byte array onto the appropriate {@link OutputStream}.
 * Thus, encoders have total control of what and when gets written to the
 * {@link OutputStream} maintained by the owning appender.
 * 
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Joern Huxhorn
 * @author Maarten Bosteels
 * 
 * @param <E>
 *          event type
 * @since 0.9.19
 */
public interface Encoder<E> extends ContextAware, LifeCycle {

    /**
     * This method is called when the owning appender starts or whenever output
     * needs to be directed to a new OutputStream, for instance as a result of a
     * rollover. Implementing encoders should at the very least remember the
     * OutputStream passed as argument and use it in future operations.
     * 
     * @param os
     * @throws IOException
     */
    void init(OutputStream os) throws IOException;

    /**
     * Encode and write an event to the appropriate {@link OutputStream}.
     * Implementations are free to differ writing out of the encoded event and
     * instead write in batches.
     * 
     * @param event
     * @throws IOException
     */
    void doEncode(E event) throws IOException;

    /**
     * This method is called prior to the closing of the underling
     * {@link OutputStream}. Implementations MUST not close the underlying
     * {@link OutputStream} which is the responsibility of the owning appender.
     * 
     * @throws IOException
     */
    void close() throws IOException;
}
