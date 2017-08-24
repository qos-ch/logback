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
package ch.qos.logback.core.issue;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Example code illustrating locking policies in the JDK.
 * See http://jira.qos.ch/browse/LBCORE-97 for a discussion.
 * 
 * @author Joern Huxhorn
 */
public class LBCORE97 {

    static int THREAD_COUNT = 10;

    public static void main(String args[]) throws InterruptedException {

        System.out.println("Environment:");
        System.out.println("java.runtime.name    = " + System.getProperty("java.runtime.name"));
        System.out.println("java.runtime.version = " + System.getProperty("java.runtime.version"));
        System.out.println("java.vendor          = " + System.getProperty("java.vendor"));
        System.out.println("java.version         = " + System.getProperty("java.version"));
        System.out.println("java.vm.name         = " + System.getProperty("java.vm.name"));
        System.out.println("java.vm.info         = " + System.getProperty("java.vm.info"));

        System.out.println("os.name              = " + System.getProperty("os.name"));
        System.out.println("os.version           = " + System.getProperty("os.version"));
        System.out.println("os.arch              = " + System.getProperty("os.arch"));
        System.out.println("##########################################");

        usingSynchronized(THREAD_COUNT);
        usingUnfairLock(THREAD_COUNT);
        usingFairLock(THREAD_COUNT);
    }

    public static void execute(String text, Thread[] threads) throws InterruptedException {
        System.out.println("About to execute " + text + "...");
        int threadCount = threads.length;
        for (int i = 0; i < threadCount; i++) {
            threads[i].start();
        }

        Thread.sleep(10000);

        for (int i = 0; i < threadCount; i++) {
            threads[i].interrupt();
        }
        Thread.sleep(1000); // wait a moment for termination, to lazy for join ;)
    }

    public static void print(String text, Runnable[] runnables) {
        System.out.println("Results for " + text + ":");
        for (int i = 0; i < runnables.length; i++) {
            System.out.println("runnables[" + i + "]: " + runnables[i]);
        }
        System.out.println("##########################################");
    }

    public static void usingSynchronized(int threadCount) throws InterruptedException {
        Object lockObject = new Object();
        Runnable[] runnables = new Runnable[threadCount];
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            runnables[i] = new SynchronizedRunnable(lockObject);
            threads[i] = new Thread(runnables[i]);
        }
        String text = "usingSynchronized";
        execute(text, threads);
        print(text, runnables);
    }

    public static void usingUnfairLock(int threadCount) throws InterruptedException {
        Lock lock = new ReentrantLock();
        Runnable[] runnables = new Runnable[threadCount];
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            runnables[i] = new LockRunnable(lock);
            threads[i] = new Thread(runnables[i]);
        }

        String text = "usingUnfairLock";
        execute(text, threads);
        print(text, runnables);
    }

    public static void usingFairLock(int threadCount) throws InterruptedException {
        Lock lock = new ReentrantLock(true);
        Runnable[] runnables = new Runnable[threadCount];
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            runnables[i] = new LockRunnable(lock);
            threads[i] = new Thread(runnables[i]);
        }

        String text = "usingFairLock";
        execute(text, threads);
        print(text, runnables);
    }

    public static class SynchronizedRunnable implements Runnable {
        private final Object lockObject;
        private int counter;
        private boolean running;

        public SynchronizedRunnable(Object lockObject) {
            this.lockObject = lockObject;
            this.counter = 0;
            this.running = false;
        }

        public void run() {
            running = true;
            for (;;) {
                synchronized (lockObject) {
                    counter++;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
            running = false;
        }

        public String toString() {
            return "SynchronizedRunnable[counter=" + counter + ", running=" + running + "]";
        }
    }

    public static class LockRunnable implements Runnable {
        private final Lock lock;
        private int counter;
        private boolean running;

        public LockRunnable(Lock lock) {
            this.lock = lock;
            this.counter = 0;
            this.running = false;
        }

        public void run() {
            running = true;
            for (;;) {
                lock.lock();
                try {
                    counter++;
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    break;
                } finally {
                    lock.unlock();
                }
            }
            running = false;
        }

        public String toString() {
            return "LockRunnable[counter=" + counter + ", running=" + running + "]";
        }
    }
}
