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
package ch.qos.logback.core.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Static utility method for {@link Closeable} objects.
 *
 * @author Carl Harris
 */
public class CloseUtil {

    /**
     * Closes a closeable while suppressing any {@code IOException} that occurs.
     * @param closeable the socket to close
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable == null)
            return;
        try {
            closeable.close();
        } catch (IOException ex) {
            assert true; // avoid an empty catch
        }
    }

    /**
     * Closes a socket while suppressing any {@code IOException} that occurs.
     * @param socket the socket to close
     */
    public static void closeQuietly(Socket socket) {
        if (socket == null)
            return;
        try {
            socket.close();
        } catch (IOException ex) {
            assert true; // avoid an empty catch
        }
    }

    /**
     * Closes a server socket while suppressing any {@code IOException} that 
     * occurs.
     * @param serverSocket the socket to close
     */
    public static void closeQuietly(ServerSocket serverSocket) {
        if (serverSocket == null)
            return;
        try {
            serverSocket.close();
        } catch (IOException ex) {
            assert true; // avoid an empty catch
        }
    }

}
