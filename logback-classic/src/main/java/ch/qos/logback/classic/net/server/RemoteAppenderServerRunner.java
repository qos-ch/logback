/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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

import java.net.Socket;
import java.util.concurrent.Executor;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.net.server.Client;
import ch.qos.logback.core.net.server.ConcurrentServerRunner;
import ch.qos.logback.core.net.server.ServerListener;
import ch.qos.logback.core.net.server.ServerRunner;

/**
 * A {@link ServerRunner} that receives logging events from remote appender
 * clients.
 *
 * @author Carl Harris
 */
class RemoteAppenderServerRunner extends ConcurrentServerRunner {

  /**
   * Constructs a new server runner.
   *
   * @param listener the listener from which the server will accept new
   *                 clients
   * @param executor that will be used to execute asynchronous tasks
   *                 on behalf of the runner.
   */
  public RemoteAppenderServerRunner(
          ServerListener listener, Executor executor) {
    super(listener, executor);
  }

  @Override
  protected Client buildClient(String id, Socket socket) {
    return new AppenderFacingStreamInputClient(id, socket);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean configureClient(Client client) {
    AppenderFacingClient appenderFacingClient = (AppenderFacingClient) client;
    appenderFacingClient.setLoggerContext((LoggerContext) getContext());
    return true;
  }

}
