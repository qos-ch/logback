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

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;

/**
 * A {@link ServerRunner} that listens for connections from remote receiver
 * component clients and delivers logging events to all connected clients.
 *
 * @author Carl Harris
 */
class RemoteReceiverServerRunner extends ConcurrentServerRunner<RemoteReceiverClient> {

    private final int clientQueueSize;

    /**
     * Constructs a new server runner.
     * @param listener the listener from which the server will accept new
     *    clients
     * @param executor that will be used to execute asynchronous tasks 
     *    on behalf of the runner.
     * @param queueSize size of the event queue that will be maintained for
     *    each client
     */
    public RemoteReceiverServerRunner(ServerListener<RemoteReceiverClient> listener, Executor executor, int clientQueueSize) {
        super(listener, executor);
        this.clientQueueSize = clientQueueSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean configureClient(RemoteReceiverClient client) {
        client.setContext(getContext());
        client.setQueue(new ArrayBlockingQueue<Serializable>(clientQueueSize));
        return true;
    }

}
