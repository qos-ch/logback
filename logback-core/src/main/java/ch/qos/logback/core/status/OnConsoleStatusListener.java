/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.status;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.StatusPrinter;

import java.util.List;

/**
 * Print all new incoming status messages on the console.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class OnConsoleStatusListener extends ContextAwareBase implements StatusListener, LifeCycle {


    boolean isStarted = false;

    private void print(Status status) {
        StringBuilder sb = new StringBuilder();
        StatusPrinter.buildStr(sb, "", status);
        System.out.print(sb);
    }
    public void addStatusEvent(Status status) {
        if (!isStarted)
            return;
        print(status);
      }

    public void start() {
        isStarted = true;
        StatusManager sm = context.getStatusManager();
        List<Status>statusList = sm.getCopyOfStatusList();
        for(sta)
    }

    public void stop() {
        isStarted = false;
    }

    public boolean isStarted() {
        return isStarted;
    }
}
