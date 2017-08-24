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
package ch.qos.logback.core.testUtil;

import ch.qos.logback.core.read.ListAppender;

public class DelayingListAppender<E> extends ListAppender<E> {

    public int delay = 1;
    public boolean interrupted = false;

    public void setDelay(int ms) {
        delay = ms;
    }

    @Override
    public void append(E e) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            // consume InterruptedException
            interrupted = true;
        }
        super.append(e);
    }
}

