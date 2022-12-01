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

public class ReconfigurationTaskRegisteredConfigEventListener implements ConfigurationEventListener {

    boolean changeDetectorRegisteredEventOccurred = false;
    ReconfigureOnChangeTask reconfigureOnChangeTask;
    @Override
    public void listen(ConfigurationEvent configurationEvent) {
        switch (configurationEvent.getEventType()) {
        case CHANGE_DETECTOR_REGISTERED:
            changeDetectorRegisteredEventOccurred = true;
            Object data = configurationEvent.getData();
            if(data instanceof ReconfigureOnChangeTask)
                reconfigureOnChangeTask = (ReconfigureOnChangeTask) data;
            break;
        default:
        }
    }
}
