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

import ch.qos.logback.core.spi.ConfigurationEvent;
import ch.qos.logback.core.spi.ConfigurationEventListener;

import java.util.concurrent.CountDownLatch;

class ReconfigurationDoneListener implements ConfigurationEventListener {
    CountDownLatch countDownLatch;

    ReconfigureOnChangeTask roct;
    ReconfigurationDoneListener(CountDownLatch countDownLatch,  ReconfigureOnChangeTask aRoct) {
        this.countDownLatch = countDownLatch;
        this.roct = aRoct;
    }

    @Override
    public void listen(ConfigurationEvent configurationEvent) {
        switch (configurationEvent.getEventType()) {
        case CONFIGURATION_ENDED:
            if(roct == null) {
                countDownLatch.countDown();
            } else {
                Object data = configurationEvent.getData();
                if(data instanceof ReconfigureOnChangeTask && roct == data) {
                    countDownLatch.countDown();
                }
            }
            break;
        default:
        }

    }
}
