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
package ch.qos.logback.core.encoder;

import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * Encoders are responsible for transform an incoming event into a byte array
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Joern Huxhorn
 * @author Maarten Bosteels
 * 
 * @param <E> event type
 * @since 0.9.19
 */
public interface Encoder<E> extends ContextAware, LifeCycle {

    /**
     * Get header bytes. This method is typically called upon opening of an output
     * stream.
     * 
     * @return header bytes. Null values are allowed.
     */
    byte[] headerBytes();

    /**
     * Encode an event as bytes.
     * 
     * @param event the event to encode
     * @return the encoded bytes. Null values are allowed.
     */
    byte[] encode(E event);

    /**
     * Get footer bytes. This method is typically called prior to the closing of the
     * stream where events are written.
     * 
     * @return footer bytes. Null values are allowed.
     */
    byte[] footerBytes();

    /**
     * Returns true if the encoder is stateful, false otherwise.
     *
     * <p>A stateful encoder keeps state between invocation of {@link #encode(E event)}
     * method.</p>
     *
     * <p>A stateful encoder may produce different output for the same input event
     * depending on its internal state. A stateless encoder will always produce
     * the same output for the same input event.</p>
     *
     * @return true if the encoder is stateful, false otherwise.
     * @since 1.6.4
     */
    default boolean isStateful() {
        return false;
    }
}
