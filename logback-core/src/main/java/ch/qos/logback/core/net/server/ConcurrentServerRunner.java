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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * A concurrent {@link ServerRunner}.
 * <p>
 * An instance of this object is created with a {@link ServerListener} and
 * an {@link Executor}.  On invocation of the {@link #start()} method, it
 * passes itself to the given {@code Executor} and returns immediately.  On
 * invocation of its {@link #run()} method by the {@link Executor} it begins 
 * accepting client connections via its {@code ServerListener}.  As each
 * new {@link Client} is accepted, the client is configured with the 
 * runner's {@link LoggingContext} and is then passed to the {@code 
 * Executor} for concurrent execution of the client's service loop.     
 * <p>
 * On invocation of the {@link #stop()} method, the runner closes the listener
 * and each of the connected clients (by invoking {@link Client#close()} 
 * effectively interrupting any blocked I/O calls and causing these concurrent
 * subtasks to exit gracefully).  This ensures that before the {@link #stop()}
 * method returns (1) all I/O resources have been released and (2) all 
 * of the threads of the {@code Executor} are idle.
 *
 * @author Carl Harris
 */
public abstract class ConcurrentServerRunner<T extends Client> extends ContextAwareBase implements Runnable, ServerRunner<T> {

    private final Lock clientsLock = new ReentrantLock();

    private final Collection<T> clients = new ArrayList<T>();

    private final ServerListener<T> listener;
    private final Executor executor;

    private boolean running;

    /**
     * Constructs a new server runner.
     * @param listener the listener from which the server will accept new
     *    clients
     * @param executor a executor that will facilitate execution of the
     *    listening and client-handling tasks; while any {@link Executor}
     *    is allowed here, outside of unit testing the only reasonable choice
     *    is a bounded thread pool of some kind.
     */
    public ConcurrentServerRunner(ServerListener<T> listener, Executor executor) {
        this.listener = listener;
        this.executor = executor;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRunning() {
        return running;
    }

    protected void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * {@inheritDoc}
     */
    public void stop() throws IOException {
        listener.close();
        accept(new ClientVisitor<T>() {
            public void visit(T client) {
                client.close();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void accept(ClientVisitor<T> visitor) {
        Collection<T> clients = copyClients();
        for (T client : clients) {
            try {
                visitor.visit(client);
            } catch (RuntimeException ex) {
                addError(client + ": " + ex);
            }
        }
    }

    /**
     * Creates a copy of the collection of all clients that are presently
     * being tracked by the server.
     * @return collection of client objects
     */
    private Collection<T> copyClients() {
        clientsLock.lock();
        try {
            Collection<T> copy = new ArrayList<T>(clients);
            return copy;
        } finally {
            clientsLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        setRunning(true);
        try {
            addInfo("listening on " + listener);
            while (!Thread.currentThread().isInterrupted()) {
                T client = listener.acceptClient();
                if (!configureClient(client)) {
                    addError(client + ": connection dropped");
                    client.close();
                    continue;
                }
                try {
                    executor.execute(new ClientWrapper(client));
                } catch (RejectedExecutionException ex) {
                    addError(client + ": connection dropped");
                    client.close();
                }
            }
        } catch (InterruptedException ex) {
            assert true; // ok... we'll shut down
        } catch (Exception ex) {
            addError("listener: " + ex);
        }

        setRunning(false);
        addInfo("shutting down");
        listener.close();
    }

    /**
     * Configures a connected client.
     * <p>
     * A subclass implements this method to perform any necessary configuration
     * of the client object before its {@link Client#run()} method is invoked.
     * 
     * @param client the subject client
     * @return {@code true} if configuration was successful; if the return
     *    value is {@code false} the client connection will be dropped
     */
    protected abstract boolean configureClient(T client);

    /**
     * Adds a client to the collection of those being tracked by the server.
     * @param client the client to add
     */
    private void addClient(T client) {
        clientsLock.lock();
        try {
            clients.add(client);
        } finally {
            clientsLock.unlock();
        }
    }

    /**
     * Removes a client from the collection of those being tracked by the server.
     * @param client the client to remote
     */
    private void removeClient(T client) {
        clientsLock.lock();
        try {
            clients.remove(client);
        } finally {
            clientsLock.unlock();
        }
    }

    /**
     * A wrapper for a {@link Client} responsible for ensuring that client
     * tracking is performed properly.
     */
    private class ClientWrapper implements Client {

        private final T delegate;

        public ClientWrapper(T client) {
            this.delegate = client;
        }

        public void run() {
            addClient(delegate);
            try {
                delegate.run();
            } finally {
                removeClient(delegate);
            }
        }

        public void close() {
            delegate.close();
        }

    }

}
