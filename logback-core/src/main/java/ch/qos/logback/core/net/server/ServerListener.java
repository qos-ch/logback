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

import java.io.Closeable;
import java.io.IOException;

/**
 * A listener that accepts {@link Client} connections on behalf of a
 * {@link ServerRunner}.
 * <p>
 * This interface exists primarily to abstract away the details of the
 * listener's underlying {@code ServerSocket} and the concurrency associated
 * with handling multiple clients. Such realities make it difficult to create
 * effective unit tests for the {@link ServerRunner} that are easy to 
 * understand and maintain.
 * <p>
 * This interface captures the only those details about the listener that the
 * {@code ServerRunner} cares about; namely, that it is something that has
 * an underlying resource (or resources) that need to be closed before the
 * listener is discarded.
 * </ol>
 */
public interface ServerListener<T extends Client> extends Closeable {

    /**
     * Accepts the next client that appears on this listener.
     * <p>
     * An implementation of this method is expected to block the calling thread
     * and not return until either a client appears or an exception occurs.
     * 
     * @return client object
     * @throws IOException
     * @throws InterruptedException
     */
    T acceptClient() throws IOException, InterruptedException;

    /**
     * Closes any underlying {@link Closeable} resources associated with this
     * listener.
     * <p>
     * Note that (as described in Doug Lea's discussion about interrupting I/O
     * operations in "Concurrent Programming in Java" (Addison-Wesley 
     * Professional, 2nd edition, 1999) this method is used to interrupt
     * any blocked I/O operation in the client when the server is shutting
     * down.  The client implementation must anticipate this potential,
     * and gracefully exit when the blocked I/O operation throws the 
     * relevant {@link IOException} subclass. 
     * <p>
     * Note also, that unlike {@link Closeable#close()} this method is not
     * permitted to propagate any {@link IOException} that occurs when closing
     * the underlying resource(s).
     */
    void close();

}
