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

package ch.qos.logback.classic.util;

import java.io.IOException;

import ch.qos.logback.classic.issue.logback1159.LoggingError;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.ErrorStatus;

/**
 * This class should be in a folder relating to the issue being tested. However, we place it here for reaasons related
 * to JMPS package access rules.
 */
public class LogbackListener1159 extends ContextAwareBase implements StatusListener, LifeCycle {
    private boolean started;

    @Override
    public void start() {
        this.started = true;
    }

    @Override
    public void stop() {
        this.started = false;
    }

    @Override
    public boolean isStarted() {
        return this.started;
    }

    @Override
    public void addStatusEvent(final Status status) {
        if (status instanceof ErrorStatus && status.getThrowable() instanceof IOException) {
            System.out.println("*************************LogbackListener.addStatusEvent");
            throw new LoggingError(status.getMessage(), status.getThrowable());
        }
    }

}