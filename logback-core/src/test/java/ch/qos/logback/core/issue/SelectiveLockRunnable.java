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

import ch.qos.logback.core.contention.RunnableWithCounterAndDone;

/**
 * A runnable which behaves differently depending on the desired locking model.
 * 
 * @author Joern Huxhorn
 * @author Ceki Gulcu
 */
public class SelectiveLockRunnable extends RunnableWithCounterAndDone {

    enum LockingModel {
        NOLOCK, SYNC, FAIR, UNFAIR;
    }

    static Object LOCK = new Object();
    static Lock FAIR_LOCK = new ReentrantLock(true);
    static Lock UNFAIR_LOCK = new ReentrantLock(false);

    LockingModel model;

    SelectiveLockRunnable(LockingModel model) {
        this.model = model;
    }

    public void run() {
        switch (model) {
        case NOLOCK:
            nolockRun();
            break;
        case SYNC:
            synchronizedRun();
            break;
        case FAIR:
            fairLockRun();
            break;
        case UNFAIR:
            unfairLockRun();
            break;
        }
    }

    void fairLockRun() {
        for (;;) {
            FAIR_LOCK.lock();
            counter++;
            FAIR_LOCK.unlock();
            if (done) {
                return;
            }
        }
    }

    void unfairLockRun() {
        for (;;) {
            UNFAIR_LOCK.lock();
            counter++;
            UNFAIR_LOCK.unlock();
            if (done) {
                return;
            }
        }
    }

    void nolockRun() {
        for (;;) {
            counter++;
            if (done) {
                return;
            }
        }
    }

    void synchronizedRun() {
        for (;;) {
            synchronized (LOCK) {
                counter++;
            }
            if (done) {
                return;
            }
        }
    }

    @Override
    public String toString() {
        return "SelectiveLockRunnable " + model;
    }
}
