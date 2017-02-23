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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.spi.ContextAware;

/**
 * Allows programmatic initialization and configuration of Logback.
 * The ServiceLoader is typically used to instantiate implementations and
 * thus implementations will need to follow the guidelines of the ServiceLoader 
 * specifically a no-arg constructor is required.
 */
public interface Configurator extends ContextAware {

    /**
     * The context will also be set before this method is called via
     * {@link ContextAware#setContext(ch.qos.logback.core.Context)}.
     */
    public void configure(LoggerContext loggerContext);

}
