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
package ch.qos.logback.classic.jul;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.LogManager;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * Propagate level changes made to a logback logger into the equivalent logger in j.u.l.
 */
public class LevelChangePropagator extends ContextAwareBase implements LoggerContextListener, LifeCycle {

    private final Set<java.util.logging.Logger> julLoggerSet = new HashSet<>();
    boolean isStarted = false;
    boolean resetJUL = false;

    public void setResetJUL(final boolean resetJUL) {
        this.resetJUL = resetJUL;
    }

    @Override
    public boolean isResetResistant() {
        return false;
    }

    @Override
    public void onStart(final LoggerContext context) {
    }

    @Override
    public void onReset(final LoggerContext context) {
    }

    @Override
    public void onStop(final LoggerContext context) {
    }

    @Override
    public void onLevelChange(final Logger logger, final Level level) {
        propagate(logger, level);
    }

    private void propagate(final Logger logger, final Level level) {
        addInfo("Propagating " + level + " level on " + logger + " onto the JUL framework");
        final java.util.logging.Logger julLogger = JULHelper.asJULLogger(logger);
        // prevent garbage collection of jul loggers whose level we set
        // see also http://jira.qos.ch/browse/LBCLASSIC-256
        julLoggerSet.add(julLogger);
        final java.util.logging.Level julLevel = JULHelper.asJULLevel(level);
        julLogger.setLevel(julLevel);
    }

    public void resetJULLevels() {
        final LogManager lm = LogManager.getLogManager();

        final Enumeration<String> e = lm.getLoggerNames();
        while (e.hasMoreElements()) {
            final String loggerName = e.nextElement();
            final java.util.logging.Logger julLogger = lm.getLogger(loggerName);
            if (JULHelper.isRegularNonRootLogger(julLogger) && julLogger.getLevel() != null) {
                addInfo("Setting level of jul logger [" + loggerName + "] to null");
                julLogger.setLevel(null);
            }
        }
    }

    private void propagateExistingLoggerLevels() {
        final LoggerContext loggerContext = (LoggerContext) context;
        final List<Logger> loggerList = loggerContext.getLoggerList();
        for (final Logger l : loggerList) {
            if (l.getLevel() != null) {
                propagate(l, l.getLevel());
            }
        }
    }

    @Override
    public void start() {
        if (resetJUL) {
            resetJULLevels();
        }
        propagateExistingLoggerLevels();

        isStarted = true;
    }

    @Override
    public void stop() {
        isStarted = false;
    }

    @Override
    public boolean isStarted() {
        return isStarted;
    }
}
