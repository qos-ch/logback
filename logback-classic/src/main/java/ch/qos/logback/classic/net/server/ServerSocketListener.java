/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * A {@link ServerListener} that accepts connections on a {@link ServerSocket}.
 *
 * @author Carl Harris
 */
class ServerSocketListener implements ServerListener {

  private final ServerSocket serverSocket;

  /**
   * Constructs a new listener.
   * @param serverSocket server socket delegate
   */
  public ServerSocketListener(ServerSocket serverSocket) {
    this.serverSocket = serverSocket;
  }

  /**
   * {@inheritDoc}
   */
  public Client acceptClient() throws IOException {
    Socket socket = serverSocket.accept();
    return new StreamClient(
        socketAddressToString(socket.getRemoteSocketAddress()), 
        socket.getInputStream());
  }

  /**
   * {@inheritDoc}
   */
  public void close() {
    try {
      serverSocket.close();
    }
    catch (IOException ex) {
      ex.printStackTrace(System.err);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return socketAddressToString(serverSocket.getLocalSocketAddress());
  }

  /**
   * Converts a socket address to a reasonable display string.
   * @param address the subject socket address
   * @return display string
   */
  private String socketAddressToString(SocketAddress address) {
    String addr = address.toString();
    int i = addr.indexOf("/");
    if (i >= 0) {
      addr = addr.substring(i + 1);
    }
    return addr;
  }

}
