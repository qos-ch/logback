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

package ch.qos.logback.core.spi;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * A very simple {@link SequenceNumberGenerator} based on an {@link AtomicLong}
 * variable.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.3.0
 */
public class BasicSequenceNumberGenerator extends ContextAwareBase implements SequenceNumberGenerator {

    private final AtomicLong atomicLong = new AtomicLong();

    @Override
    public long nextSequenceNumber() {
        return atomicLong.incrementAndGet();
    }

}
