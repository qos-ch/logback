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
package ch.qos.logback.core.net;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;

import ch.qos.logback.core.net.ssl.ConfigurableSSLSocketFactory;
import ch.qos.logback.core.net.ssl.SSLComponent;
import ch.qos.logback.core.net.ssl.SSLConfiguration;
import ch.qos.logback.core.net.ssl.SSLParametersConfiguration;

/**
 * An abstract base for module specific {@code SSLSocketAppender}
 * implementations located in other logback modules.
 *
 * @author Carl Harris
 */
public abstract class AbstractSSLSocketAppender<E> extends AbstractSocketAppender<E> implements SSLComponent {

    private SSLConfiguration ssl;
    private SocketFactory socketFactory;

    /**
     * Constructs a new appender.
     */
    protected AbstractSSLSocketAppender() {
    }

    /**
     * Gets an {@link SocketFactory} that produces SSL sockets using an
     * {@link SSLContext} that is derived from the appender's configuration.
     * @return socket factory
     */
    @Override
    protected SocketFactory getSocketFactory() {
        return socketFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        try {
            SSLContext sslContext = getSsl().createContext(this);
            SSLParametersConfiguration parameters = getSsl().getParameters();
            parameters.setContext(getContext());
            socketFactory = new ConfigurableSSLSocketFactory(parameters, sslContext.getSocketFactory());
            super.start();
        } catch (Exception ex) {
            addError(ex.getMessage(), ex);
        }
    }

    /**
     * Gets the SSL configuration.
     * @return SSL configuration; if no configuration has been set, a
     *    default configuration is returned
     */
    public SSLConfiguration getSsl() {
        if (ssl == null) {
            ssl = new SSLConfiguration();
        }
        return ssl;
    }

    /**
     * Sets the SSL configuration.
     * @param ssl the SSL configuration to set
     */
    public void setSsl(SSLConfiguration ssl) {
        this.ssl = ssl;
    }

}
