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
package ch.qos.logback.classic.issue.lbclassic36;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.text.SimpleDateFormat;
import java.util.Date;

//import org.joda.time.format.DateTimeFormatter;
//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.DateTime;

public class DateFormatOriginal_tzest extends TestCase {
    public static final String ISO8601_PATTERN = "yyyy-MM-dd HH:mm:ss,SSS";
    static final long NANOS_IN_ONE_SEC = 1000 * 1000 * 1000L;

    /**
     * Create the test case
     * 
     * @param testName
     *                name of the test case
     */
    public DateFormatOriginal_tzest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(DateFormatOriginal_tzest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    // public void testRaw() throws Exception {
    // SimpleDateFormat simpleFormat = new SimpleDateFormat(ISO8601_PATTERN);
    // DateTimeFormatter jodaFormat = DateTimeFormat.forPattern(ISO8601_PATTERN);
    //
    // Date date = new Date();
    // DateTime dateTime = new DateTime(date);
    //
    // long start = System.nanoTime();
    // for (int i = 0; i < 100000; ++i) {
    // jodaFormat.print(dateTime);
    // }
    // long jodaAvg = (System.nanoTime() - start) / 100000;
    //
    //
    // start = System.nanoTime();
    // for (int i = 0; i < 100000; ++i) {
    // simpleFormat.format(date);
    // }
    // long simpleAvg = (System.nanoTime() - start) / 100000;
    //
    // float diff = (((float) (simpleAvg - jodaAvg)) / simpleAvg) * 100;
    // System.out.println("Raw - JDK: " + simpleAvg + " ns Joda: " + jodaAvg
    // + " ns - Difference: " + diff + "%");
    // }

    public void testSynchronized() throws Exception {
        SynchronizedDateFormatter formatter = new SynchronizedDateFormatter();
        int threads = 10;
        int iterations = 10000;
        Thread[] formatThreads = new Thread[threads];
        Date date = new Date();

        for (int i = 0; i < threads; i++) {
            formatThreads[i] = new DateFormatThread(formatter, date, iterations);
        }
        long start = System.nanoTime();
        for (Thread thread : formatThreads) {
            thread.start();
        }
        for (Thread thread : formatThreads) {
            thread.join();
        }
        long end = System.nanoTime();
        double actual = ((double) (end - start)) / NANOS_IN_ONE_SEC;
        System.out.printf("Synchronized DateFormat: %,.4f seconds\n", actual);

    }

    public void testUnSynchronized() throws Exception {
        UnsynchronizedDateFormatter formatter = new UnsynchronizedDateFormatter();
        int threads = 10;
        int iterations = 10000;
        Thread[] formatThreads = new Thread[threads];
        Date date = new Date();

        for (int i = 0; i < threads; i++) {
            formatThreads[i] = new DateFormatThread(formatter, date, iterations);
        }
        long start = System.nanoTime();
        for (Thread thread : formatThreads) {
            thread.start();
        }
        for (Thread thread : formatThreads) {
            thread.join();
        }
        long end = System.nanoTime();
        double actual = ((double) (end - start)) / NANOS_IN_ONE_SEC;
        System.out.printf("Unsynchronized DateFormat: %,.4f seconds\n", actual);

    }

    public void testThreadLocal() throws Exception {
        ThreadLocalDateFormatter formatter = new ThreadLocalDateFormatter();
        int threads = 10;
        int iterations = 10000;
        Thread[] formatThreads = new Thread[threads];
        Date date = new Date();

        for (int i = 0; i < threads; i++) {
            formatThreads[i] = new DateFormatThread(formatter, date, iterations);
        }
        long start = System.nanoTime();
        for (Thread thread : formatThreads) {
            thread.start();
        }
        for (Thread thread : formatThreads) {
            thread.join();
        }
        long end = System.nanoTime();
        double actual = ((double) (end - start)) / NANOS_IN_ONE_SEC;
        System.out.printf("ThreadLocal DateFormat: %,.4f seconds\n", actual);

    }

    // public void testDateTimeFormatter() throws Exception {
    // int threads = 10;
    // int iterations = 10000;
    // Thread[] formatThreads = new DateTimeFormatThread[threads];
    // JodaFormatter formatter = new JodaFormatter();
    // Date date = new Date();
    // DateTime dateTime = new DateTime(date);
    //
    // for (int i = 0; i < threads; i++) {
    // formatThreads[i] = new DateTimeFormatThread(formatter, dateTime,
    // iterations);
    // }
    // long start = System.nanoTime();
    // for (Thread thread : formatThreads) {
    // thread.start();
    // }
    // for (Thread thread : formatThreads) {
    // thread.join();
    // }
    // long end = System.nanoTime();
    // double actual = ((double) (end - start)) / NANOS_IN_ONE_SEC;
    // System.out.printf("Joda DateTimeFormatter: %,.4f seconds\n", actual);
    //
    // }

    public interface Formatter {
        String format(Date date);
    }

    public static class SynchronizedDateFormatter implements Formatter {
        SimpleDateFormat simpleFormat = new SimpleDateFormat(ISO8601_PATTERN);

        public synchronized String format(Date date) {
            return simpleFormat.format(date);
        }
    }

    public static class UnsynchronizedDateFormatter implements Formatter {
        public synchronized String format(Date date) {
            return new SimpleDateFormat(ISO8601_PATTERN).format(date);
        }
    }

    public static class ThreadLocalDateFormatter implements Formatter {
        ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
            protected synchronized SimpleDateFormat initialValue() {
                return new SimpleDateFormat(ISO8601_PATTERN);
            }
        };

        public String format(Date date) {
            return formatter.get().format(date);
        }
    }

    // public static class JodaFormatter {
    // DateTimeFormatter formatter = DateTimeFormat.forPattern(ISO8601_PATTERN);
    //
    // public String format(DateTime date) {
    // return formatter.print(date);
    // }
    // }

    public static class DateFormatThread extends Thread {
        Formatter formatter;
        Date date;
        long iterCount;

        public DateFormatThread(Formatter f, Date date, long iterations) {
            this.formatter = f;
            this.date = date;
            this.iterCount = iterations;
        }

        public void run() {
            for (int i = 0; i < iterCount; i++) {
                formatter.format(this.date);
            }
        }
    }

    // public static class DateTimeFormatThread extends Thread {
    // JodaFormatter formatter;
    // DateTime date;
    // long iterCount;
    //
    // public DateTimeFormatThread(JodaFormatter f, DateTime date, long iterations) {
    // this.formatter = f;
    // this.date = date;
    // this.iterCount = iterations;
    // }
    //
    // public void run() {
    // for (int i = 0; i < iterCount; i++) {
    // formatter.format(this.date);
    // }
    // }
    // }
}
