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
package ch.qos.logback.core.net.server;


/**
 *
 * A mock {@link Client} that notifies waiting thread when it has started,
 * and waits to be interrupted before exiting.
 *
 * @author Carl Harris
 */
class MockClient implements Client {

    private boolean running;
    private boolean closed;

    public void run() {
        synchronized (this) {
            running = true;
            notifyAll();
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void close() {
        synchronized (this) {
            running = false;
            closed = true;
            notifyAll();
        }
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized boolean isClosed() {
        return closed;
    }

}
