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
package ch.qos.logback.core.net.server.test;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ch.qos.logback.core.net.server.Client;
import ch.qos.logback.core.net.server.ServerListener;

/**
 * A mock {@link ServerListener} that has a blocking queue to pass a client
 * to a {@link #acceptClient()} caller.  If the {@link #close()} method is
 * called while a caller is blocked waiting to take from the queue, the
 * caller's thread is interrupted.
 *
 * @author Carl Harris
 */
public class MockServerListener<T extends Client> implements ServerListener<T> {

    private final BlockingQueue<T> queue = new LinkedBlockingQueue<T>();

    private boolean closed;
    private Thread waiter;

    public synchronized Thread getWaiter() {
        return waiter;
    }

    public synchronized void setWaiter(Thread waiter) {
        this.waiter = waiter;
    }

    public synchronized boolean isClosed() {
        return closed;
    }

    public synchronized void setClosed(boolean closed) {
        this.closed = closed;
    }

    public T acceptClient() throws IOException, InterruptedException {
        if (isClosed()) {
            throw new IOException("closed");
        }
        setWaiter(Thread.currentThread());
        try {
            return queue.take();
        } finally {
            setWaiter(null);
        }
    }

    public void addClient(T client) {
        queue.offer(client);
    }

    public synchronized void close() {
        setClosed(true);
        Thread waiter = getWaiter();
        if (waiter != null) {
            waiter.interrupt();
        }
    }

}
