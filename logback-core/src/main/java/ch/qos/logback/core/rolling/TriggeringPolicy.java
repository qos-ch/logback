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
package ch.qos.logback.core.rolling;

import java.io.File;

import ch.qos.logback.core.spi.LifeCycle;

/**
 * A <code>TriggeringPolicy</code> controls the conditions under which roll-over
 * occurs. Such conditions include time of day, file size, an 
 * external event, the log request or a combination thereof.
 *
 * @author Ceki G&uuml;lc&uuml;
 * */

public interface TriggeringPolicy<E> extends LifeCycle {

    /**
     * Should roll-over be triggered at this time?
     * 
     * @param activeFile A reference to the currently active log file. 
     * @param event A reference to the currently event. 
     * @return true if a roll-over should occur.
     */
    boolean isTriggeringEvent(final File activeFile, final E event);
}
