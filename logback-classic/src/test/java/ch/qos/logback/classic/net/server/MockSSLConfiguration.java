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

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import ch.qos.logback.core.net.ssl.SSLConfiguration;
import ch.qos.logback.core.spi.ContextAware;

/**
 * A mock {@link SSLConfiguration} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
class MockSSLConfiguration extends SSLConfiguration {

    private boolean contextCreated;

    @Override
    public SSLContext createContext(ContextAware context) throws NoSuchProviderException, NoSuchAlgorithmException, KeyManagementException,
                    UnrecoverableKeyException, KeyStoreException, CertificateException {
        contextCreated = true;
        return super.createContext(context);
    }

    public boolean isContextCreated() {
        return contextCreated;
    }

}
