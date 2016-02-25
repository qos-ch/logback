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
package ch.qos.logback.access.net;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.net.AbstractSSLSocketAppender;
import ch.qos.logback.core.spi.PreSerializationTransformer;

/**
 * A {@link SocketAppender} that supports SSL.
 * <p>
 * For more information on this appender, please refer to the online manual
 * at http://logback.qos.ch/manual/appenders.html#SSLSocketAppender
 * 
 * @author Carl Harris
 */
public class SSLSocketAppender extends AbstractSSLSocketAppender<IAccessEvent> {

    private final PreSerializationTransformer<IAccessEvent> pst = new AccessEventPreSerializationTransformer();

    public SSLSocketAppender() {
    }

    @Override
    protected void postProcessEvent(IAccessEvent event) {
        event.prepareForDeferredProcessing();
    }

    public PreSerializationTransformer<IAccessEvent> getPST() {
        return pst;
    }

}
