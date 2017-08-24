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
package ch.qos.logback.classic.issue.logback416;

import java.util.concurrent.atomic.AtomicInteger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class InstanceCountingAppender extends AppenderBase<ILoggingEvent> {

    static public AtomicInteger INSTANCE_COUNT = new AtomicInteger(0);

    public InstanceCountingAppender() {
        INSTANCE_COUNT.getAndIncrement();
    }

    protected void append(ILoggingEvent e) {
    }

}
