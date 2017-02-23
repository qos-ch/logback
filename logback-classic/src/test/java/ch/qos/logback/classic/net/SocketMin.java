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
package ch.qos.logback.classic.net;

import java.io.InputStreamReader;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Logger;

public class SocketMin {

    static Logger logger = (Logger) LoggerFactory.getLogger(SocketMin.class.getName());
    static SocketAppender s;

    public static void main(String argv[]) {
        if (argv.length == 3) {
            init(argv[0], argv[1]);
        } else {
            usage("Wrong number of arguments.");
        }

        // NDC.push("some context");
        if (argv[2].equals("true")) {
            loop();
        } else {
            test();
        }

        s.stop();
    }

    static void usage(String msg) {
        System.err.println(msg);
        System.err.println("Usage: java " + SocketMin.class + " host port true|false");
        System.exit(1);
    }

    static void init(String host, String portStr) {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        BasicConfigurator bc = new BasicConfigurator();
        bc.setContext(root.getLoggerContext());
        bc.configure(root.getLoggerContext());
        try {
            int port = Integer.parseInt(portStr);
            logger.info("Creating socket appender (" + host + "," + port + ").");
            s = new SocketAppender();
            s.setRemoteHost(host);
            s.setPort(port);
            s.setName("S");
            root.addAppender(s);
        } catch (java.lang.NumberFormatException e) {
            e.printStackTrace();
            usage("Could not interpret port number [" + portStr + "].");
        } catch (Exception e) {
            System.err.println("Could not start!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    static void loop() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        InputStreamReader in = new InputStreamReader(System.in);
        System.out.println("Type 'q' to quit");
        int i;
        int k = 0;
        while (true) {
            logger.debug("Message " + k++);
            logger.info("Message " + k++);
            logger.warn("Message " + k++);
            logger.error("Message " + k++, new Exception("Just testing"));
            try {
                i = in.read();
            } catch (Exception e) {
                return;
            }
            if (i == -1)
                break;
            if (i == 'q')
                break;
            if (i == 'r') {
                System.out.println("Removing appender S");
                root.detachAppender("S");
            }
        }
    }

    static void test() {
        int i = 0;
        logger.debug("Message " + i++);
        logger.info("Message " + i++);
        logger.warn("Message " + i++);
        logger.error("Message " + i++);
        logger.debug("Message " + i++, new Exception("Just testing."));
    }
}
