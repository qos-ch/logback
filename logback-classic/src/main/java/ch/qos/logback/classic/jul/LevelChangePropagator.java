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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.LogManager;

/**
 * Propagate level changes made to a logback logger into the equivalent logger in j.u.l.
 */
public class LevelChangePropagator extends ContextAwareBase implements LoggerContextListener, LifeCycle {

    private Set<java.util.logging.Logger> julLoggerSet = new HashSet<java.util.logging.Logger>();
    boolean isStarted = false;
    boolean resetJUL = false;

    public void setResetJUL(boolean resetJUL) {
        this.resetJUL = resetJUL;
    }

    public boolean isResetResistant() {
        return false;
    }

    public void onStart(LoggerContext context) {
    }

    public void onReset(LoggerContext context) {
    }

    public void onStop(LoggerContext context) {
    }

    public void onLevelChange(Logger logger, Level level) {
        propagate(logger, level);
    }

    private void propagate(Logger logger, Level level) {
        addInfo("Propagating " + level + " level on " + logger + " onto the JUL framework");
        java.util.logging.Logger julLogger = JULHelper.asJULLogger(logger);
        // prevent garbage collection of jul loggers whose level we set
        // see also http://jira.qos.ch/browse/LBCLASSIC-256
        julLoggerSet.add(julLogger);
        java.util.logging.Level julLevel = JULHelper.asJULLevel(level);
        julLogger.setLevel(julLevel);
    }

    public void resetJULLevels() {
        LogManager lm = LogManager.getLogManager();

        Enumeration<String> e = lm.getLoggerNames();
        while (e.hasMoreElements()) {
            String loggerName = e.nextElement();
            java.util.logging.Logger julLogger = lm.getLogger(loggerName);
            if (JULHelper.isRegularNonRootLogger(julLogger) && julLogger.getLevel() != null) {
                addInfo("Setting level of jul logger [" + loggerName + "] to null");
                julLogger.setLevel(null);
            }
        }
    }

    private void propagateExistingLoggerLevels() {
        LoggerContext loggerContext = (LoggerContext) context;
        List<Logger> loggerList = loggerContext.getLoggerList();
        for (Logger l : loggerList) {
            if (l.getLevel() != null) {
                propagate(l, l.getLevel());
            }
        }
    }

    public void start() {
        if (resetJUL) {
            resetJULLevels();
        }
        propagateExistingLoggerLevels();

        isStarted = true;
    }

    public void stop() {
        isStarted = false;
    }

    public boolean isStarted() {
        return isStarted;
    }
}
