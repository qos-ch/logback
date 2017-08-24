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

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;

import ch.qos.logback.core.net.ssl.ConfigurableSSLServerSocketFactory;
import ch.qos.logback.core.net.ssl.SSLComponent;
import ch.qos.logback.core.net.ssl.SSLConfiguration;
import ch.qos.logback.core.net.ssl.SSLParametersConfiguration;

/**
 * A {@link ServerSocketReceiver} that supports SSL.
 *
 * @author Carl Harris
 */
public class SSLServerSocketReceiver extends ServerSocketReceiver implements SSLComponent {

    private SSLConfiguration ssl;
    private ServerSocketFactory socketFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    protected ServerSocketFactory getServerSocketFactory() throws Exception {
        if (socketFactory == null) {
            SSLContext sslContext = getSsl().createContext(this);
            SSLParametersConfiguration parameters = getSsl().getParameters();
            parameters.setContext(getContext());
            socketFactory = new ConfigurableSSLServerSocketFactory(parameters, sslContext.getServerSocketFactory());
        }
        return socketFactory;
    }

    /**
     * Gets the server's SSL configuration.
     * @return SSL configuration; if no SSL configuration was provided
     *    a default configuration is returned
     */
    public SSLConfiguration getSsl() {
        if (ssl == null) {
            ssl = new SSLConfiguration();
        }
        return ssl;
    }

    /**
     * Gets the server's SSL configuration.
     * @param ssl the SSL configuration to set.
     */
    public void setSsl(SSLConfiguration ssl) {
        this.ssl = ssl;
    }

}
