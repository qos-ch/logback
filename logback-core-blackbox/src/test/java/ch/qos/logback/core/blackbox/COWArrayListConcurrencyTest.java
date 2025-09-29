/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.blackbox;

//import ch.qos.logback.core.Appender;
//import ch.qos.logback.core.AppenderBase;
//import ch.qos.logback.core.Context;
//import ch.qos.logback.core.ContextBase;
//import ch.qos.logback.core.spi.AppenderAttachableImpl;
import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.Future;
//import java.util.concurrent.locks.ReentrantLock;
//
//import static org.assertj.core.api.Fail.fail;

@Disabled
public class COWArrayListConcurrencyTest {
//
//    //private static final int LIST_SIZE = 1_000_000;
//    private static final int LOOP_LEN = 1_0;
//    private static final int RECONFIGURE_DELAY = 1;
//
//    ReentrantLock reconfigureLock = new ReentrantLock(true);
//    ReentrantLock writeLock = new ReentrantLock(true);
//
//    private static int THREAD_COUNT = 200; //Runtime.getRuntime().availableProcessors()*200;
//    //private static int THREAD_COUNT = 5000;
//
//    private final ExecutorService tasksExecutor = Executors.newVirtualThreadPerTaskExecutor();
//    LoopingRunnable[] loopingThreads = new LoopingRunnable[THREAD_COUNT];
//    ReconfiguringThread[] reconfiguringThreads = new ReconfiguringThread[THREAD_COUNT];
//    Future<?>[] futures = new Future[THREAD_COUNT];
//
//    AppenderAttachableImpl<String> aai = new AppenderAttachableImpl<>();
//    Context context = new ContextBase();
//
//    void reconfigureWithDelay(AppenderAttachableImpl<String> aai) {
//        try {
//            reconfigureLock.lock();
//            aai.addAppender(makeNewNOPAppender());
//            aai.addAppender(makeNewNOPAppender());
//            delay(RECONFIGURE_DELAY);
//            aai.detachAndStopAllAppenders();
//        } finally {
//            reconfigureLock.unlock();
//        }
//    }
//
//    private Appender<String> makeNewNOPAppender() {
//        List<Long> longList = new ArrayList<>();
////        for (int j = 0; j < LIST_SIZE; j++) {
////            longList.add(0L);
////        }
//        Appender<String> nopAppenderWithDelay = new NOPAppenderWithDelay<>(longList);
//        nopAppenderWithDelay.setContext(context);
//        nopAppenderWithDelay.start();
//        return nopAppenderWithDelay;
//    }
//
//    private void delay(int delay) {
//        try {
//            Thread.sleep(delay);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Test
//    void smoke() throws InterruptedException, ExecutionException {
//
//        for (int i = 0; i < THREAD_COUNT; i++) {
//            System.out.println("i="+i);
//            ReconfiguringThread rt = new ReconfiguringThread(aai);
//            futures[i] = tasksExecutor.submit(rt);
//            reconfiguringThreads[i] = rt;
//        }
//
//        for (int i = 0; i < THREAD_COUNT; i++) {
//            LoopingRunnable loopingThread = new LoopingRunnable(i, aai);
//            tasksExecutor.submit(loopingThread);
//            loopingThreads[i] = loopingThread;
//        }
//
//        for (int i = 0; i < THREAD_COUNT; i++) {
//            futures[i].get();
//        }
//
//        //reconfiguringThread.join();
//        Arrays.stream(loopingThreads).forEach(lt -> lt.active = false);
//
//    }
//
//    public class NOPAppenderWithDelay<E> extends AppenderBase<E> {
//
//        List<Long> longList;
//
//        NOPAppenderWithDelay(List<Long> longList) {
//            this.longList = new ArrayList<>(longList);
//        }
//
//        int i = 0;
//
//        @Override
//        protected void append(E eventObject) {
//            i++;
//            try {
//                writeLock.lock();
//                if ((i & 0xF) == 0) {
//                    delay(1);
//                } else {
//                    //longList.stream().map(x-> x+1);
//                }
//            } finally {
//                writeLock.unlock();
//            }
//
//        }
//
//    }
//
//    class ReconfiguringThread extends Thread {
//
//        AppenderAttachableImpl<String> aai;
//
//        ReconfiguringThread(AppenderAttachableImpl aai) {
//            this.aai = aai;
//        }
//
//        public void run() {
//            Thread.yield();
//            for (int i = 0; i < LOOP_LEN; i++) {
//                reconfigureWithDelay(aai);
//            }
//        }
//
//
//    }
//
//
//    class LoopingRunnable implements Runnable {
//
//        int num;
//        AppenderAttachableImpl<String> aai;
//        public boolean active = true;
//
//        LoopingRunnable(int num, AppenderAttachableImpl aai) {
//           this.num = num;
//           this.aai = aai;
//        }
//
//        public void run() {
//            System.out.println("LoopingRunnable.run.num="+num);
//            int i = 0;
//            while (active) {
//                if ((i & 0xFFFFF) == 0) {
//                    long id = Thread.currentThread().threadId();
//                    System.out.println("thread=" + id + " reconfigure=" + i);
//                }
//                aai.appendLoopOnAppenders(Integer.toString(i));
//                i++;
//                //Thread.yield();
//            }
//        }
//    }


}
