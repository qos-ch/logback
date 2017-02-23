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
package ch.qos.logback.core;

import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.FilterAttachable;
import ch.qos.logback.core.spi.LifeCycle;

public interface Appender<E> extends LifeCycle, ContextAware, FilterAttachable<E> {

    /**
     * Get the name of this appender. The name uniquely identifies the appender.
     */
    String getName();

    /**
     * This is where an appender accomplishes its work. Note that the argument 
     * is of type Object.
     * @param event
     */
    void doAppend(E event) throws LogbackException;

    /**
     * Set the name of this appender. The name is used by other components to
     * identify this appender.
     * 
     */
    void setName(String name);

}
