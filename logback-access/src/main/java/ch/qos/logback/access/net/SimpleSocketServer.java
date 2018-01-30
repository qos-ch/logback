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
package ch.qos.logback.access.net;

import java.net.ServerSocket;
import java.net.Socket;

import ch.qos.logback.access.joran.JoranConfigurator;
import ch.qos.logback.access.spi.AccessContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * A simple {@link SocketNode} based server.
 * 
 * <pre>
 *     &lt;b&gt;Usage:&lt;/b&gt; java ch.qos.logback.access.net.SimpleSocketServer port configFile
 *    
 *     where
 * <em>
 * port
 * </em>
 *     is a part number where the server listens and
 * <em>
 * configFile
 * </em>
 *     is an xml configuration file fed to {@link JoranConfigurator}.
 * </pre>
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 * @since 0.8.4
 */
public class SimpleSocketServer {

    static int port;

    private static AccessContext basicContext;

    public static void main(String argv[]) throws Exception {
        if (argv.length == 2) {
            init(argv[0], argv[1]);
        } else {
            usage("Wrong number of arguments.");
        }

        runServer();
    }

    static void runServer() {
        try {
            System.out.println("Listening on port " + port);
            @SuppressWarnings("resource")
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                System.out.println("Waiting to accept a new client.");
                Socket socket = serverSocket.accept();
                System.out.println("Connected to client at " + socket.getInetAddress());
                System.out.println("Starting new socket node.");
                new Thread(new SocketNode(socket, basicContext)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void usage(String msg) {
        System.err.println(msg);
        System.err.println("Usage: java " + SimpleSocketServer.class.getName() + " port configFile");
        System.exit(1);
    }

    static void init(String portStr, String configFile) throws JoranException {
        try {
            port = Integer.parseInt(portStr);
        } catch (java.lang.NumberFormatException e) {
            e.printStackTrace();
            usage("Could not interpret port number [" + portStr + "].");
        }

        basicContext = new AccessContext();
        if (configFile.endsWith(".xml")) {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(basicContext);
            configurator.doConfigure(configFile);
            StatusPrinter.print(basicContext);
        }
    }
}
