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
package chapters.appenders;

import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.util.StatusPrinter;

public class IOPerformance extends Thread {
    static String MSG = "ABCDEGHIJKLMNOPQRSTUVWXYZabcdeghijklmnopqrstuvwxyz1234567890";
    static String LOG_FILE;
    public static String PARALLEL_FILE;

    static int NUM_THREADS = 1;
    static long l;
    long len;
    boolean immediateFlush;
    Logger logger;
    LoggerContext context;
    double throughput;

    public IOPerformance(boolean _immediateFlush, long _len) {
        this.len = _len;
        this.immediateFlush = _immediateFlush;
        context = new LoggerContext();
        logger = context.getLogger("logger-" + getName());

        // A FileAppender is created according to the buffering and
        // immediate flush setting of this IO instance.
        FileAppender<ILoggingEvent> fa = new FileAppender<ILoggingEvent>();
        fa.setName("FILE");
        PatternLayoutEncoder pa = new PatternLayoutEncoder();
        pa.setPattern("%r %5p %c [%t] - %m%n");
        pa.setContext(context);
        pa.start();
        fa.setEncoder(pa);

        fa.setFile(LOG_FILE);
        fa.setAppend(true);
        fa.setContext(context);
        fa.start();

        ((ch.qos.logback.classic.Logger) logger).addAppender(fa);

        StatusPrinter.print(context);
    }

    public static void main(String[] argv) throws Exception {
        if (argv.length != 3) {
            usage("Wrong number of arguments.");
        }

        l = Integer.parseInt(argv[0]);
        LOG_FILE = argv[1];
        PARALLEL_FILE = argv[2];

        // ----------------------------------------------------
        // first test with immediate flushing
        perfCase(true, l);

        // ----------------------------------------------------
        // Second test with no immediate flushing
        perfCase(false, l);

        // There is no fourth test as buffered IO and immediate flushing
        // do not make sense.
    }

    static void usage(String msg) {
        System.err.println(msg);
        System.err.println("Usage: java " + IOPerformance.class.getName() + " runLength logFile otherFile\n"
                        + "   runLength (integer) the number of logs to generate perthread\n" + "   logFile path to a logFile\n"
                        + "   otherFile path to a second file\n");
        System.exit(1);
    }

    static void perfCase(boolean immediateFlush, long len) throws Exception {
        IOPerformance[] threads = new IOPerformance[NUM_THREADS];
        OtherIO otherIOThread = new OtherIO();
        otherIOThread.start();

        // First create the threads
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new IOPerformance(immediateFlush, len);
        }

        // then start them
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].start();
        }

        // wait for them to processPriorToRemoval, compute the average throughput
        double sum = 0;

        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].join();
            sum += threads[i].throughput;
        }

        // setting the interrupted field will cause counterThread to processPriorToRemoval
        otherIOThread.interrupted = true;
        otherIOThread.join();

        System.out.println("On total throughput of " + (sum) + " logs per microsecond.");
        System.out.println("------------------------------------------------");
    }

    public void run() {

        long before = System.nanoTime();

        for (int i = 0; i < len; i++) {
            logger.debug(MSG);
        }

        throughput = (len * 1.0) / ((System.nanoTime() - before) / 1000);
        System.out.println(getName() + ", immediateFlush: " + immediateFlush + ", throughput: " + throughput + " logs per microsecond.");
    }
}

class OtherIO extends Thread {
    public boolean interrupted = false;
    public int counter = 0;

    public void run() {
        long before = System.nanoTime();
        try {
            FileWriter fw = new FileWriter(IOPerformance.PARALLEL_FILE, true);

            while (!interrupted) {
                counter++;
                fw.write("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                fw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        double tput = (counter * 1.0) / (System.nanoTime() - before);
        System.out.println("Counter thread " + getName() + " incremented counter by " + tput + " per nanosecond.");
    }
}
