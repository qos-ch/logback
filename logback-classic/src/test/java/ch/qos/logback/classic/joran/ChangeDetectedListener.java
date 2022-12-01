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

class ChangeDetectedListener  implements ConfigurationEventListener {

    CountDownLatch countDownLatch;

    ReconfigureOnChangeTask reconfigureOnChangeTask;

    ChangeDetectedListener(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void listen(ConfigurationEvent configurationEvent) {
        switch (configurationEvent.getEventType()) {
        case CHANGE_DETECTED:
            System.out.println(this.toString() + "#listen Change detected" + " count="+countDownLatch.getCount());

            countDownLatch.countDown();
            Object data = configurationEvent.getData();
            if (data instanceof ReconfigureOnChangeTask) {
                reconfigureOnChangeTask = (ReconfigureOnChangeTask) data;
            }
            break;
        default:
        }
    }
}
