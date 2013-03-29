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
package chapters.appenders.socket.ssl;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;


/**
 * This application uses an SSLSocketServer that log messages to a
 * server on a host and port specified by the user. It waits for the
 * user to type a message which will be sent to the server.
 * */
public class SocketServer {
  static void usage(String msg) {
    System.err.println(msg);
    System.err.println("Usage: java " + SocketServer.class.getName() +
      " configFile\n" +
      "   configFile a logback configuration file" +
      "   in XML format.");
    System.exit(1);
  }

  static public void main(String[] args) throws Exception {
    if (args.length != 1) {
      usage("Wrong number of arguments.");
    }

    String configFile = args[0];

    if (configFile.endsWith(".xml")) {
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      lc.stop();
      configurator.setContext(lc);
      configurator.doConfigure(configFile);
    }

    Thread.sleep(Long.MAX_VALUE);
  }
}
