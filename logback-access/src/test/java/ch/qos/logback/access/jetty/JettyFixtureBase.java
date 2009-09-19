/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.access.jetty;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;

abstract public class JettyFixtureBase {
  protected RequestLogImpl requestLogImpl;

  private final int port;
  Server server;
  String url;
  
  public JettyFixtureBase(RequestLogImpl impl, int port) {
    requestLogImpl = impl;
    this.port = port;
    url = "http://localhost:" + port + "/";
  }

  public String getName() {
    return "Jetty Test Setup";
  }

  public String getUrl() {
    return url;
  }

  public void start() throws Exception {
    server = new Server();
    Connector connector = new SelectChannelConnector();
    connector.setPort(port);
    server.setConnectors(new Connector[] { connector });

    ContextHandler context = new ContextHandler();
    context.setContextPath("/");
    context.setResourceBase(".");
    context.setClassLoader(Thread.currentThread().getContextClassLoader());
    server.addHandler(context);

    RequestLogHandler requestLogHandler = new RequestLogHandler();
    buildContext();
    requestLogHandler.setRequestLog(requestLogImpl);
    server.addHandler(requestLogHandler);

    Handler handler = getHandler();
    context.addHandler(handler);

    server.start();
  }

  public void stop() throws Exception {
    // System.out.println("into tearDown");
    server.stop();
    server = null;
    requestLogImpl = null;
  }

  abstract protected void buildContext();
  abstract protected Handler getHandler();
}
