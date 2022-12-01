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

package ch.qos.logback.classic.joran;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.contention.AbstractMultiThreadedHarness;
import ch.qos.logback.core.status.InfoStatus;

import java.util.concurrent.CountDownLatch;

class ReconfigureOnChangeTaskHarness extends AbstractMultiThreadedHarness {

    private final LoggerContext loggerContext;

    private final CountDownLatch countDownLatch;

    ReconfigureOnChangeTaskHarness(LoggerContext loggerContext, int aChangeCountLimit) {
        this.loggerContext = loggerContext;
        this.countDownLatch = new CountDownLatch(aChangeCountLimit);
        ChangeDetectedListener cdl = new ChangeDetectedListener(countDownLatch);
        loggerContext.addConfigurationEventListener(cdl);
    }

    public void waitUntilEndCondition() throws InterruptedException {

        String classname = this.getClass().getSimpleName();

        loggerContext.getStatusManager()
                .add(new InfoStatus("Entering " + classname + ".waitUntilEndCondition()", this));
        countDownLatch.await();
        loggerContext.getStatusManager()
                .add(new InfoStatus("*****Exiting " + classname + ".waitUntilEndCondition()", this));
    }

}
