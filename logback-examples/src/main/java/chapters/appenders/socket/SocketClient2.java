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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * This application uses a SocketAppender that log messages to a
 * server on a host and port specified by the user. It waits for the
 * user to type a message which will be sent to the server.
 * */
public class SocketClient2 {
    static void usage(String msg) {
        System.err.println(msg);
        System.err.println("Usage: java " + SocketClient2.class.getName() + " configFile\n" + "   configFile a logback configuration file"
                        + "   in XML format.");
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

        Logger logger = LoggerFactory.getLogger(SocketClient2.class);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.println("Type a message to send to log server. Type 'q' to quit.");

            String s = reader.readLine();

            if (s.equals("q")) {
                break;
            } else {
                logger.debug(s);
            }
        }
    }
}
