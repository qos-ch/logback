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
package ch.qos.logback.core.net;

import java.net.Socket;
import java.util.concurrent.Callable;

import javax.net.SocketFactory;

/**
 * A {@link Runnable} that (re)connects a socket.
 * <p>
 * An implementation of this interface is responsible for repeatedly 
 * attempting to create a socket connection to a remote host.
 *
 * @author Carl Harris
 */
public interface SocketConnector extends Callable<Socket> {

    /**
     * An exception handler that is notified of all exceptions that occur
     * during the (re)connection process.
     */
    public interface ExceptionHandler {
        void connectionFailed(SocketConnector connector, Exception ex);
    }

    /**
     * Blocks the calling thread until a connection is successfully
     * established.
     * @return the connected socket
     * @throws InterruptedException
     */
    Socket call() throws InterruptedException;

    /**
     * Sets the connector's exception handler.
     * <p>
     * The handler must be set before the {@link #call()} method is invoked.
     * @param exceptionHandler the handler to set
     */
    void setExceptionHandler(ExceptionHandler exceptionHandler);

    /**
     * Sets the connector's socket factory.
     * <p>
     * If no factory is configured that connector will use the platform's
     * default factory.
     * 
     * @param socketFactory the factory to set
     */
    void setSocketFactory(SocketFactory socketFactory);

}
