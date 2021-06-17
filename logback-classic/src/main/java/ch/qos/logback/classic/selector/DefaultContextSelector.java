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
package ch.qos.logback.classic.selector;

import java.util.Arrays;
import java.util.List;

import ch.qos.logback.classic.LoggerContext;

public class DefaultContextSelector implements ContextSelector {

    private LoggerContext defaultLoggerContext;

    public DefaultContextSelector(LoggerContext context) {
        this.defaultLoggerContext = context;
    }

    @Override
    public LoggerContext getLoggerContext() {
        return getDefaultLoggerContext();
    }

    @Override
    public LoggerContext getDefaultLoggerContext() {
        return defaultLoggerContext;
    }

    @Override
    public LoggerContext detachLoggerContext(String loggerContextName) {
        return defaultLoggerContext;
    }

    @Override
    public List<String> getContextNames() {
        return Arrays.asList(defaultLoggerContext.getName());
    }

    @Override
    public LoggerContext getLoggerContext(String name) {
        if (defaultLoggerContext.getName().equals(name)) {
            return defaultLoggerContext;
        } else {
            return null;
        }
    }
}
