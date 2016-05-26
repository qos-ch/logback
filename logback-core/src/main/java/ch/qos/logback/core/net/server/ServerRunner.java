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

import ch.qos.logback.core.spi.ContextAware;

/**
 * An object that is responsible for the asynchronous execution of a
 * socket server.
 * <p>
 * This interface exists primarily to allow the runner to be mocked for
 * the purpose of unit testing the socket server implementation.
 * 
 * @author Carl Harris
 */
public interface ServerRunner<T extends Client> extends ContextAware, Runnable {

    /**
     * Gets a flag indicating whether the server is currently running.
     * @return flag state
     */
    boolean isRunning();

    /**
     * Stops execution of the runner.
     * <p>
     * This method must cause all I/O and thread resources associated with
     * the runner to be released.  If the receiver has not been started, this
     * method must have no effect.
     * @throws IOException
     */
    void stop() throws IOException;

    /**
     * Presents each connected client to the given visitor.   
     * @param visitor the subject visitor
     */
    void accept(ClientVisitor<T> visitor);

}
