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
package ch.qos.logback.access.testUtil;

import java.util.concurrent.LinkedBlockingQueue;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.AppenderBase;

public class NotifyingListAppender extends AppenderBase<IAccessEvent> {

    public final LinkedBlockingQueue<IAccessEvent> list = new LinkedBlockingQueue<IAccessEvent>();

    protected void append(IAccessEvent e) {
        list.add(e);
    }
}
