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
package chapters.appenders.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import ch.qos.logback.core.util.Duration;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SocketAppender;

/**
 * This application uses a SocketAppender that log messages to a
 * server on a host and port specified by the user. It waits for the
 * user to type a message which will be sent to the server.
 * */
public class SocketClient1 {
    static void usage(String msg) {
        System.err.println(msg);
        System.err.println("Usage: java " + SocketClient1.class.getName() + " hostname port\n" + "   hostname the name of the remote log server\n"
                        + "   port (integer) the port number of the server\n");
        System.exit(1);
    }

    static public void main(String[] args) throws Exception {
        if (args.length != 2) {
            usage("Wrong number of arguments.");
        }

        String hostName = args[0];
        int port = Integer.parseInt(args[1]);

        // Create a SocketAppender connected to hostname:port with a
        // reconnection delay of 10000 seconds.
        SocketAppender socketAppender = new SocketAppender();
        socketAppender.setRemoteHost(hostName);
        socketAppender.setPort(port);
        socketAppender.setReconnectionDelay(new Duration(10000));
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        socketAppender.setContext(lc);

        // SocketAppender options become active only after the execution
        // of the next statement.
        socketAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger(SocketClient1.class);
        logger.addAppender(socketAppender);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.println("Type a message to send to log server at " + hostName + ":" + port + ". Type 'q' to quit.");

            String s = reader.readLine();

            if (s.equals("q")) {
                break;
            } else {
                logger.debug(s);
            }
        }
    }
}
