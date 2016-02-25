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
package ch.qos.logback.classic.net.server;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.net.server.Client;
import ch.qos.logback.core.net.server.ServerRunner;

/**
 * A client of a {@link ServerRunner} that receives events from a remote
 * appender.
 *  
 * @author Carl Harris
 */
interface RemoteAppenderClient extends Client {

    /**
     * Sets the client's logger context.
     * <p>
     * This provides the local logging context to the client's service thread,
     * and is used as the destination for logging events received from the
     * client.
     * <p>
     * This method <em>must</em> be invoked before the {@link #run()} method.
     * @param lc the logger context to set
     */
    void setLoggerContext(LoggerContext lc);

}
