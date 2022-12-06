/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.util;

import ch.qos.logback.core.contention.AbstractMultiThreadedHarness;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InvocationGateTest {

    private static final int ONCE_EVERY = 100;
    private static final int MAX_TRAVERSAL_COUNT = 10_000;
    private static final int THREAD_COUNT = 16;


    static final int MASK = 0xAFF;

    AtomicLong currentTime = new AtomicLong(1);



    @Test
    public void smoke() {
        InvocationGate sig = new SimpleInvocationGate();
        int currentTime = SimpleInvocationGate.DEFAULT_INCREMENT + 1;
        assertFalse(sig.isTooSoon(currentTime));
        currentTime++;
        assertTrue(sig.isTooSoon(currentTime));
    }

    @Disabled
    @Test
    void checkThreadSafety() throws InterruptedException {
        InvocationGate sig = new SimpleInvocationGate(1);

        long initialTime = currentTime.get();
        sig.isTooSoon(initialTime); // sync invocation gate with current time

        AtomicInteger traversalCount = new AtomicInteger(0);
        RunnableWithCounterAndDone[] runnables = buildRunnables(sig, traversalCount);
        SimpleInvocationGateHarness harness = new SimpleInvocationGateHarness(traversalCount);
        harness.execute(runnables);

        int tc = traversalCount.get();
        long ct = currentTime.get();
        long diff = ct - initialTime - MAX_TRAVERSAL_COUNT;
        int traversalCountMismatch = tc - MAX_TRAVERSAL_COUNT;
        assertTrue(traversalCountMismatch >=0, "traversalCountMismatch must be a positive number");
        int tolerance = 6;
        assertTrue(traversalCountMismatch < tolerance, "traversalCountMismatch must be less than "+tolerance+ " actual value "+traversalCountMismatch);

        assertTrue(diff >=0, "time difference must be a positive number");
        assertTrue(diff < tolerance, "time difference must be less than "+tolerance+" actual value "+diff);



    }

    private RunnableWithCounterAndDone[] buildRunnables(InvocationGate invocationGate, AtomicInteger traversalCount ) {

        RunnableWithCounterAndDone[] runnables = new RunnableWithCounterAndDone[THREAD_COUNT + 1];
        runnables[0] = new TimeUpdater(currentTime);
        for(int i = 1; i < runnables.length; i++) {
            runnables[i] = new InvocationGateChecker(invocationGate, traversalCount);
        }
        return runnables;
    }

    class SimpleInvocationGateHarness extends AbstractMultiThreadedHarness {

        AtomicInteger traversalCount;

        public SimpleInvocationGateHarness(AtomicInteger traversalCount) {
            this.traversalCount = traversalCount;
        }

        public void waitUntilEndCondition() throws InterruptedException {
            while(traversalCount.get() < MAX_TRAVERSAL_COUNT) {
                Thread.yield();
            }
        }
    }

    private class TimeUpdater extends RunnableWithCounterAndDone {

        Random random = new Random(69923259L);
        AtomicLong currentTime;
        public TimeUpdater(AtomicLong currentTime) {
            this.currentTime = currentTime;
        }
        @Override
        public void run() {
            sleep(10);
            while(!isDone()) {
                if (0 == random.nextInt(ONCE_EVERY)) {
                    long ct = currentTime.incrementAndGet();
                    if((ct & MASK) == MASK) {
                        System.out.println("Time increment ct="+ct);
                    }
                }
               Thread.yield();
            }
        }

        private void sleep(int duration) {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class InvocationGateChecker extends RunnableWithCounterAndDone {

        InvocationGate invocationGate;
        AtomicInteger traversalCount;
        public InvocationGateChecker(InvocationGate invocationGate, AtomicInteger traversalCount) {
            this.invocationGate = invocationGate;
            this.traversalCount = traversalCount;
        }

        @Override
        public void run() {
            while(!isDone()) {
                if (!invocationGate.isTooSoon(currentTime.get())) {
                    int tc = traversalCount.incrementAndGet();
                    if((tc & MASK) == MASK) {
                        System.out.println("traversalCount="+tc);
                    }
                }
                Thread.yield();
            }
        }
    }
}
