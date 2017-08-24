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
package ch.qos.logback.core.read;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.helpers.CyclicBuffer;

/**
 * CyclicBufferAppender stores events in a cyclic buffer of user-specified size. As the 
 * name suggests, if the size of the buffer is N, only the latest N events are available.
 * 
 * 
 * @author Ceki Gulcu
 */
public class CyclicBufferAppender<E> extends AppenderBase<E> {

    CyclicBuffer<E> cb;
    int maxSize = 512;

    public void start() {
        cb = new CyclicBuffer<E>(maxSize);
        super.start();
    }

    public void stop() {
        cb = null;
        super.stop();
    }

    @Override
    protected void append(E eventObject) {
        if (!isStarted()) {
            return;
        }
        cb.add(eventObject);
    }

    public int getLength() {
        if (isStarted()) {
            return cb.length();
        } else {
            return 0;
        }
    }

    public E get(int i) {
        if (isStarted()) {
            return cb.get(i);
        } else {
            return null;
        }
    }

    public void reset() {
        cb.clear();
    }

    /**
     * Set the size of the cyclic buffer.
     */
    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

}
