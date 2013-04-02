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

package ch.qos.logback.core.net.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A {@link ServerListener} that accepts connections from remote logger
 * clients.
 *
 * @author Carl Harris
 */
public class RemoteLoggerServerListener
    extends ServerSocketListener<RemoteLoggerClient> {

  /**
   * Constructs a new listener.
   * @param serverSocket server socket from which new client connections
   *    will be accepted
   */
  public RemoteLoggerServerListener(ServerSocket serverSocket) {
    super(serverSocket);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected RemoteLoggerClient createClient(String id, Socket socket)
      throws IOException {
    return new RemoteLoggerStreamClient(id, socket);
  }

}
