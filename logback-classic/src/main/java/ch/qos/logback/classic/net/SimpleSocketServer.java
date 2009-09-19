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
package ch.qos.logback.classic.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * A simple {@link SocketNode} based server.
 * 
 * <pre>
 *      &lt;b&gt;Usage:&lt;/b&gt; java ch.qos.logback.classic.net.SimpleSocketServer port configFile
 * </pre>
 * 
 * where <em>port</em> is a port number where the server listens and
 * <em>configFile</em> is an xml configuration file fed to
 * {@link JoranConfigurator}.
 * 
 * </pre>
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 * @since 0.8.4
 */
public class SimpleSocketServer extends Thread {

  Logger logger = LoggerFactory.getLogger(SimpleSocketServer.class);

  private final int port;
  private final LoggerContext lc;
  private boolean closed = false;
  private ServerSocket serverSocket;
  private List<SocketNode> socketNodeList = new ArrayList<SocketNode>();
  
  public static void main(String argv[]) throws Exception {
    int port = -1;
    if (argv.length == 2) {
      port = parsePortNumber(argv[0]);
    } else {
      usage("Wrong number of arguments.");
    }

    String configFile = argv[1];
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    configureLC(lc, configFile);

    SimpleSocketServer sss = new SimpleSocketServer(lc, port);
    sss.start();
  }

  public SimpleSocketServer(LoggerContext lc, int port) {
    this.lc = lc;
    this.port = port;
  }

  public void run() {
    try {
      logger.info("Listening on port " + port);
      serverSocket = new ServerSocket(port);
      while (!closed) {
        logger.info("Waiting to accept a new client.");
        Socket socket = serverSocket.accept();
        logger.info("Connected to client at " + socket.getInetAddress());
        logger.info("Starting new socket node.");
        SocketNode newSocketNode = new SocketNode(this, socket, lc); 
        synchronized (socketNodeList) {
          socketNodeList.add(newSocketNode);
        }
        new Thread(newSocketNode).start();
        signalSocketNodeCreation();
      }
    } catch (Exception e) {
      if(closed) {
        logger.info("Exception in run method for a closed server. This is normal.");
      } else {
        logger.error("Unexpected failure in run method", e);
      }
    }
  }

  /**
   * Signal another thread that we have established a conneciton
   *  This is useful for testing purposes.
   */
  void signalSocketNodeCreation() {
    synchronized(this) {
      this.notifyAll();
    }
  }
  public boolean isClosed() {
    return closed;
  }

  public void close() {
    closed = true;
    if (serverSocket != null) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        logger.error("Failed to close serverSocket", e);
      } finally {
        serverSocket = null;
      }
    } 
    
    synchronized (socketNodeList) {
      for(SocketNode sn: socketNodeList) {
        sn.close();
      }      
      socketNodeList.clear();
    }
  }

  public void socketNodeClosing(SocketNode sn) {
    logger.debug("Removing {}", sn);

    // don't allow simultaneous access to the socketNodeList
    // (e.g. removal whole iterating on the list causes
    // java.util.ConcurrentModificationException
    synchronized (socketNodeList) {
      socketNodeList.remove(sn);      
    }
  }
  static void usage(String msg) {
    System.err.println(msg);
    System.err.println("Usage: java " + SimpleSocketServer.class.getName()
        + " port configFile");
    System.exit(1);
  }

  static int parsePortNumber(String portStr) {
    try {
      return Integer.parseInt(portStr);
    } catch (java.lang.NumberFormatException e) {
      e.printStackTrace();
      usage("Could not interpret port number [" + portStr + "].");
      // we won't get here
      return -1;
    }
  }

  static public void configureLC(LoggerContext lc, String configFile)
      throws JoranException {
    JoranConfigurator configurator = new JoranConfigurator();
    lc.reset();
    configurator.setContext(lc);
    configurator.doConfigure(configFile);
  }
}
