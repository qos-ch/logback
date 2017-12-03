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
import java.net.BindException;
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;

/**
 * Static utility methods for obtaining a {@link ServerSocket} bound to
 * a random unused port.
 *
 * @author Carl Harris
 */
public class ServerSocketUtil {

    /**
     * Creates a new {@link ServerSocket} bound to a random unused port.
     * <p>
     * This method is a convenience overload for 
     * {@link #createServerSocket(ServerSocketFactory)} using the platform's
     * default {@link ServerSocketFactory}.
     * @return socket
     * @throws IOException
     */
    public static ServerSocket createServerSocket() throws IOException {
        return createServerSocket(ServerSocketFactory.getDefault());
    }

    /**
     * Creates a new {@link ServerSocket} bound to a random unused port.
     * @param socketFactory socket factory that will be used to create the 
     *    socket
     * @return socket
     * @throws IOException
     */
    public static ServerSocket createServerSocket(ServerSocketFactory socketFactory) throws IOException {
        ServerSocket socket = null;
        int retries = 10;
        while (retries-- > 0 && socket == null) {
            int port = (int) ((65536 - 1024) * Math.random()) + 1024;
            try {
                socket = socketFactory.createServerSocket(port);
            } catch (BindException ex) {
                // try again with different port
            }
        }
        if (socket == null) {
            throw new BindException("cannot find an unused port to bind");
        }
        return socket;
    }

}
