/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2014, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.net.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import ch.qos.logback.classic.net.LoggingEventPreSerializationTransformer;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.net.server.AbstractServerSocketAppender;
import ch.qos.logback.core.net.server.RemoteReceiverClient;
import ch.qos.logback.core.net.server.ServerListener;
import ch.qos.logback.core.spi.PreSerializationTransformer;
import ch.qos.logback.core.status.Status;

/**
 * Implementation of {@link AbstractServerSocketAppender} which allows to observe how many client connections have been closed.
 *
 * @author Sebastian Gr&ouml;bler
 */
public class ConnectionDropCountingServerSocketAppender extends AbstractServerSocketAppender<ILoggingEvent> {

    private final CountDownLatch closeCounter;
    private PreSerializationTransformer<ILoggingEvent> pst = new LoggingEventPreSerializationTransformer();

    public ConnectionDropCountingServerSocketAppender(final CountDownLatch closeCounter) {
        this.closeCounter = closeCounter;
    }

    @Override
    protected void postProcessEvent(final ILoggingEvent event) {
        event.prepareForDeferredProcessing();
    }

    @Override
    protected PreSerializationTransformer<ILoggingEvent> getPST() {
        return pst;
    }

    @Override
    protected ServerListener<RemoteReceiverClient> createServerListener(final ServerSocket socket) {
        return new DropCountingListener(super.createServerListener(socket));
    }

    private class DropCountingListener implements ServerListener<RemoteReceiverClient> {

        private final ServerListener<RemoteReceiverClient> decoratedListener;

        private DropCountingListener(final ServerListener<RemoteReceiverClient> decoratedListener) {
            this.decoratedListener = decoratedListener;
        }

        public RemoteReceiverClient acceptClient() throws IOException, InterruptedException {
            return new DropCountingClient(decoratedListener.acceptClient());
        }

        public void close() {
            decoratedListener.close();
        }
    }

    private class DropCountingClient implements RemoteReceiverClient {

        private final RemoteReceiverClient decoratedClient;

        private DropCountingClient(final RemoteReceiverClient decoratedClient) {
            this.decoratedClient = decoratedClient;
        }

        public void close() {
            closeCounter.countDown();
            decoratedClient.close();
        }

        public void run() {
            decoratedClient.run();
        }

        public void setQueue(final BlockingQueue<Serializable> queue) {
            decoratedClient.setQueue(queue);
        }

        public boolean offer(final Serializable event) {
            return decoratedClient.offer(event);
        }

        public void setContext(final Context context) {
            decoratedClient.setContext(context);
        }

        public Context getContext() {
            return decoratedClient.getContext();
        }

        public void addStatus(final Status status) {
            decoratedClient.addStatus(status);
        }

        public void addInfo(final String msg) {
            decoratedClient.addInfo(msg);
        }

        public void addInfo(final String msg, final Throwable ex) {
            decoratedClient.addInfo(msg, ex);
        }

        public void addWarn(final String msg) {
            decoratedClient.addWarn(msg);
        }

        public void addWarn(final String msg, final Throwable ex) {
            decoratedClient.addWarn(msg, ex);
        }

        public void addError(final String msg) {
            decoratedClient.addError(msg);
        }

        public void addError(final String msg, final Throwable ex) {
            decoratedClient.addError(msg, ex);
        }
    }
}
