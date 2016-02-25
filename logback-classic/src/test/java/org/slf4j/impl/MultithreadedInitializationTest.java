package org.slf4j.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.read.ListAppender;

public class MultithreadedInitializationTest {

    final static int THREAD_COUNT = 4 + Runtime.getRuntime().availableProcessors() * 2;
    private static AtomicLong EVENT_COUNT = new AtomicLong(0);
    final CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT + 1);

    int diff = new Random().nextInt(10000);
    String loggerName = "org.slf4j.impl.MultithreadedInitializationTest";

    @Before
    public void setUp() throws Exception {
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, ClassicTestConstants.INPUT_PREFIX + "listAppender.xml");
        LoggerFactoryFriend.reset();
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
    }

    @Test
    public void multiThreadedInitialization() throws InterruptedException, BrokenBarrierException {
        LoggerAccessingThread[] accessors = harness();

        for (LoggerAccessingThread accessor : accessors) {
            EVENT_COUNT.getAndIncrement();
            accessor.logger.info("post harness");
        }

        Logger logger = LoggerFactory.getLogger(loggerName + ".slowInitialization-" + diff);
        logger.info("hello");
        EVENT_COUNT.getAndIncrement();

        List<ILoggingEvent> events = getRecordedEvents();
        assertEquals(EVENT_COUNT.get(), events.size());
    }

    private List<ILoggingEvent> getRecordedEvents() {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        ListAppender<ILoggingEvent> la = (ListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertNotNull(la);
        return la.list;
    }

    private static LoggerAccessingThread[] harness() throws InterruptedException, BrokenBarrierException {
        LoggerAccessingThread[] threads = new LoggerAccessingThread[THREAD_COUNT];
        final CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT + 1);
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new LoggerAccessingThread(barrier, i);
            threads[i].start();
        }

        barrier.await();
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].join();
        }
        return threads;
    }

    static class LoggerAccessingThread extends Thread {
        final CyclicBarrier barrier;
        Logger logger;
        int count;

        LoggerAccessingThread(CyclicBarrier barrier, int count) {
            this.barrier = barrier;
            this.count = count;
        }

        public void run() {
            try {
                barrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger = LoggerFactory.getLogger(this.getClass().getName() + "-" + count);
            logger.info("in run method");
            EVENT_COUNT.getAndIncrement();
        }
    };

}
