/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.access;


import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.AsyncAppenderBase;

/**
 * Asynchronous appender for logback-access.
 *
 * @author Konstantin Pavlov
 * @since 1.1.3
 */
public class AsyncAppender extends AsyncAppenderBase<IAccessEvent> {

    /**
     * Prepares {@code eventObject} for deferred processing.
     *
     * @param eventObject an event to preprocess
     */
    @Override
    protected void preprocess(IAccessEvent eventObject) {
        eventObject.prepareForDeferredProcessing();
    }

    /**
     * No events are discardable.
     *
     * @param event an event to check
     * @return false always.
     */
    protected boolean isDiscardable(IAccessEvent event) {
        return false;
    }
}
