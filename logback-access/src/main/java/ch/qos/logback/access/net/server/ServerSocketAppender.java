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
package ch.qos.logback.access.net.server;

import ch.qos.logback.access.net.AccessEventPreSerializationTransformer;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.net.server.AbstractServerSocketAppender;
import ch.qos.logback.core.spi.PreSerializationTransformer;

/**
 * An appender that listens on a TCP port for connections from remote
 * loggers.  Each event delivered to this appender is delivered to all
 * connected remote loggers. 
 *
 * @author Carl Harris
 */
public class ServerSocketAppender extends AbstractServerSocketAppender<IAccessEvent> {

    private static final PreSerializationTransformer<IAccessEvent> pst = new AccessEventPreSerializationTransformer();

    @Override
    protected void postProcessEvent(IAccessEvent event) {
        event.prepareForDeferredProcessing();
    }

    @Override
    protected PreSerializationTransformer<IAccessEvent> getPST() {
        return pst;
    }

}
