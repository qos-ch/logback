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
package ch.qos.logback.classic.spi;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class BasicContextListener implements LoggerContextListener {

    enum UpdateType {
        NONE, START, RESET, STOP, LEVEL_CHANGE
    }

    UpdateType updateType = UpdateType.NONE;
    LoggerContext context;
    Logger logger;
    Level level;

    boolean resetResistant;

    public void setResetResistant(final boolean resetResistant) {
        this.resetResistant = resetResistant;
    }

    @Override
    public void onReset(final LoggerContext context) {
        updateType = UpdateType.RESET;
        this.context = context;

    }

    @Override
    public void onStart(final LoggerContext context) {
        updateType = UpdateType.START;
        this.context = context;
    }

    @Override
    public void onStop(final LoggerContext context) {
        updateType = UpdateType.STOP;
        this.context = context;
    }

    @Override
    public boolean isResetResistant() {
        return resetResistant;
    }

    @Override
    public void onLevelChange(final Logger logger, final Level level) {
        updateType = UpdateType.LEVEL_CHANGE;
        this.logger = logger;
        this.level = level;
    }

}
