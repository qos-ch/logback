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

import ch.qos.logback.core.util.Duration;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SocketAppender;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.util.StatusPrinter;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

/**
 * Created with IntelliJ IDEA. User: ceki Date: 27.06.12 Time: 19:35 To change
 * this template use File | Settings | File Templates.
 */
public class ConsolePluginClient {

    static String LONG_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum lectus augue, pulvinar quis cursus nec, imperdiet nec ante. Cras sit amet arcu et enim adipiscing pellentesque. Suspendisse mi felis, dictum a lobortis nec, placerat in diam. Proin lobortis tortor at nunc facilisis aliquet. Praesent eget dignissim orci. Ut iaculis bibendum.";

    static String LOGGER_NAME = "com.acme.myapp.foo";
    static String UGLY_BETTY_LOGGER_NAME = "com.acme.myapp.UglyBetty";
    static long SLEEP = 1;
    static long RUN_LENGTH = 200 * 1000;

    static public void main(String[] args) throws Exception {
        // Create a SocketAppender connected to hostname:port with a
        // reconnection delay of 10000 seconds.
        String hostname = "localhost";
        int port = 4321;
        SocketAppender socketAppender = new SocketAppender();
        socketAppender.setRemoteHost(hostname);
        socketAppender.setPort(port);
        socketAppender.setIncludeCallerData(true);
        socketAppender.setReconnectionDelay(new Duration(10000));

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        socketAppender.setContext(lc);

        lc.reset();

        lc.getStatusManager().add(new OnConsoleStatusListener());
        // SocketAppender options become active only after the execution
        // of the next statement.
        socketAppender.start();

        Logger rootLogger = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);
        rootLogger.addAppender(socketAppender);

        org.slf4j.Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

        UglyBetty ub = new UglyBetty("ugly-betty-thread-234");
        ub.start();
        for (int i = 0; i < RUN_LENGTH; i++) {
            if (i % 3 == 0) {
                logger.warn(i + " is divisible by 3");
            } else {
                toto(logger, i);
            }
            Thread.sleep(SLEEP);
        }
        ub.join();

        StatusPrinter.print(lc);
    }

    /**
     * @param logger
     * @param i
     */
    /**
     * @param logger
     * @param i
     */
    static void toto(org.slf4j.Logger logger, int i) {
        logger.debug("this is message number " + i);
    }

    static class UglyBetty extends Thread {
        org.slf4j.Logger logger = LoggerFactory.getLogger(UGLY_BETTY_LOGGER_NAME);

        public UglyBetty(String name) {
            super(name);
        }

        public void run() {
            for (int i = 0; i < RUN_LENGTH; i++) {
                if (i % 23 == 0) {
                    logger.warn(LONG_TEXT);
                } else if (i % 47 == 0) {
                    logger.error("this is an exception", new Exception("test"));
                } else {
                    count(logger, i);
                }
                try {
                    Thread.sleep(SLEEP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        void count(org.slf4j.Logger logger, int i) {
            logger.debug("Betty counts to " + i);
        }
    }
}
