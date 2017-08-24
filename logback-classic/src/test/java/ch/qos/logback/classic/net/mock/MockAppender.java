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
package ch.qos.logback.classic.net.mock;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * A mock {@link AppenderBase} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
public class MockAppender extends AppenderBase<ILoggingEvent> {

    private final Lock lock = new ReentrantLock();
    private final Condition appendCondition = lock.newCondition();
    private final BlockingQueue<ILoggingEvent> events = new LinkedBlockingQueue<ILoggingEvent>();

    @Override
    protected void append(ILoggingEvent eventObject) {
        lock.lock();
        try {
            events.offer(eventObject);
            appendCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public ILoggingEvent awaitAppend(long delay) throws InterruptedException {
        return events.poll(delay, TimeUnit.MILLISECONDS);
    }

    public ILoggingEvent getLastEvent() {
        return events.peek();
    }

}
