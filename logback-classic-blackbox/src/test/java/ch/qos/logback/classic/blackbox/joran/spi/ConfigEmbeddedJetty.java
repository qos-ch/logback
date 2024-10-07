/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.blackbox.joran.spi;

import jakarta.servlet.http.HttpServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;

import java.util.HashMap;
import java.util.Map;

public class ConfigEmbeddedJetty {

    int port;
    Server server = new Server(port);
    Map<String, HttpServlet> servletPathMap = new HashMap<>();

    public ConfigEmbeddedJetty(int port) {
        this.port = port;
    }

    public Map<String, HttpServlet> getServletMap() {
        return servletPathMap;
    }

    // new ConfigFileServlet()
    public void init() throws Exception {
        Server server = new Server(port);

        // Create a handler for the root context
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");


        servletPathMap.forEach( (path, servlet) -> context.addServlet(new ServletHolder(servlet), path));

        // Set the handler for the server
        server.setHandler(context);

        System.out.println("Starting jetty server on port: " + port);
        // Start the server
        server.start();

        System.out.println("After Jetty server start(). Joining");

        while(!server.isStarted()) {
            Thread.sleep(10);
        }
        System.out.println("Jetty server started");
    }

    public void stop() throws Exception {
        server.stop();
    }
}
