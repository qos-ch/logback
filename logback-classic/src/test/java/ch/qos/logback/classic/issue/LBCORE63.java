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
package ch.qos.logback.classic.issue;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class LBCORE63 extends Thread {
    private final static String LOGGER_CONFIGURATION_FILE = "./src/test/input/issue/lbcore63.xml";
    private final Logger logger = LoggerFactory.getLogger(LBCORE63.class);

    private final long start;

    public LBCORE63() throws JoranException {
        start = new Date().getTime();
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        lc.reset();
        configurator.setContext(lc);
        configurator.doConfigure(LOGGER_CONFIGURATION_FILE);
        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    }

    public void start() {
        ScheduledThreadPoolExecutor ex1 = new ScheduledThreadPoolExecutor(1);
        ScheduledThreadPoolExecutor ex2 = new ScheduledThreadPoolExecutor(1);
        ScheduledThreadPoolExecutor ex3 = new ScheduledThreadPoolExecutor(1);
        ScheduledThreadPoolExecutor ex4 = new ScheduledThreadPoolExecutor(1);
        ScheduledThreadPoolExecutor ex5 = new ScheduledThreadPoolExecutor(1);
        ex1.scheduleAtFixedRate(new Task("EX1"), 10, 10, TimeUnit.MICROSECONDS);
        ex2.scheduleAtFixedRate(new Task("EX2"), 10, 10, TimeUnit.MICROSECONDS);
        ex3.scheduleAtFixedRate(new Task("EX3"), 10, 10, TimeUnit.MICROSECONDS);
        ex4.scheduleAtFixedRate(new Task("EX4"), 10, 10, TimeUnit.MICROSECONDS);
        ex5.scheduleAtFixedRate(new Task("EX5"), 10, 10, TimeUnit.MICROSECONDS);

        super.start();
    }

    public void run() {
        try {
            while (true) {
                logger.debug("[MAIN] {}", new Date().getTime() - start);
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            logger.info("[MAIN]: Interrupted: {}", e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            LBCORE63 main = new LBCORE63();
            main.start();
        } catch (JoranException e) {
            System.out.println("Failed to load application: " + e.getMessage());
        }
    }

    class Task implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(Task.class);
        // private final Logger logger_main = LoggerFactory.getLogger(LBCORE63.class);
        final String name;
        private final long start;

        int counter = 0;
        public long diff;

        public Task(final String name) {
            this.name = name;
            start = new Date().getTime();
        }

        public void run() {
            counter++;
            diff = new Date().getTime() - start;
            logger.debug("counter={}", counter);
            // logger_main.debug("[MAIN] - [{}] {}", name, new Date().getTime() - start);
        }
    }
}
