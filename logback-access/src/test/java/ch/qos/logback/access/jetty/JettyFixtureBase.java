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
package ch.qos.logback.access.jetty;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JettyFixtureBase {
    final protected RequestLogImpl requestLogImpl;
    protected Handler handler = new BasicHandler();
    private final int port;
    Server server;
    protected String url;

    public JettyFixtureBase(final RequestLogImpl impl, final int port) {
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
        final ServerConnector connector = new ServerConnector (server);
        connector.setPort(port);
        server.setConnectors(new Connector[] { connector });

        final RequestLogHandler requestLogHandler = new RequestLogHandler();
        configureRequestLogImpl();
        requestLogHandler.setRequestLog(requestLogImpl);

        final HandlerList handlers = new HandlerList();
        handlers.addHandler(requestLogHandler);
        handlers.addHandler(getRequestHandler());

        server.setHandler(handlers);
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
        server = null;
    }

    protected void configureRequestLogImpl() {
        requestLogImpl.start();
    }

    protected Handler getRequestHandler() {
        return handler;
    }

    class BasicHandler extends AbstractHandler {
        @Override
        @SuppressWarnings("resource")
        public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
            final OutputStream out = response.getOutputStream();
            final ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer();
            writer.write("hello world");
            writer.flush();
            response.setContentLength(writer.size());
            writer.writeTo(out);
            out.flush();

            baseRequest.setHandled(true);

        }
    }
}
