/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.net;

import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * A simple {@link SocketNode} based server.
 * 
 * <pre>
 *     &lt;b&gt;Usage:&lt;/b&gt; java ch.qos.logback.classic.net.SimpleSocketServer port configFile
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

  static Logger logger = LoggerFactory.getLogger(SimpleSocketServer.class);

  static int port;

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
      logger.info("Listening on port " + port);
      ServerSocket serverSocket = new ServerSocket(port);
      while (true) {
        logger.info("Waiting to accept a new client.");
        Socket socket = serverSocket.accept();
        logger.info("Connected to client at " + socket.getInetAddress());
        logger.info("Starting new socket node.");
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        new Thread(new SocketNode(socket, lc)).start();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static void usage(String msg) {
    System.err.println(msg);
    System.err.println("Usage: java " + SimpleSocketServer.class.getName()
        + " port configFile");
    System.exit(1);
  }

  static void init(String portStr, String configFile) throws JoranException {
    try {
      port = Integer.parseInt(portStr);
    } catch (java.lang.NumberFormatException e) {
      e.printStackTrace();
      usage("Could not interpret port number [" + portStr + "].");
    }

    if (configFile.endsWith(".xml")) {
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      lc.reset();
      configurator.setContext(lc);
      configurator.doConfigure(configFile);
      StatusPrinter.print(lc);
    }
  }
}
