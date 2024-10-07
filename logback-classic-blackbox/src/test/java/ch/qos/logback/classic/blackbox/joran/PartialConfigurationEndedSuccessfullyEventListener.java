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

package ch.qos.logback.classic.blackbox.joran;

import ch.qos.logback.core.spi.ConfigurationEvent;
import ch.qos.logback.core.spi.ConfigurationEventListener;

import java.util.concurrent.CountDownLatch;

class PartialConfigurationEndedSuccessfullyEventListener implements ConfigurationEventListener {

    CountDownLatch countDownLatch;

    PartialConfigurationEndedSuccessfullyEventListener(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void listen(ConfigurationEvent configurationEvent) {
        switch (configurationEvent.getEventType()) {
        case PARTIAL_CONFIGURATION_ENDED_SUCCESSFULLY:
            //System.out.println(this.toString() + "#listen PARTIAL_CONFIGURATION_ENDED_SUCCESSFULLY detected " + configurationEvent +" count="+countDownLatch.getCount());

            countDownLatch.countDown();
            break;
        default:
        }
    }
}
