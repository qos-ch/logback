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
package ch.qos.logback.core.net.ssl;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;

/**
 * An {@link SSLConfigurable} wrapper for an {@link SSLSocket}.
 *
 * @author Carl Harris
 * @author Bruno Harbulot 
 */
public class SSLConfigurableSocket implements SSLConfigurable {

    private final SSLSocket delegate;

    public SSLConfigurableSocket(final SSLSocket delegate) {
        this.delegate = delegate;
    }

    @Override
    public String[] getDefaultProtocols() {
        return delegate.getEnabledProtocols();
    }

    @Override
    public String[] getSupportedProtocols() {
        return delegate.getSupportedProtocols();
    }

    @Override
    public void setEnabledProtocols(final String[] protocols) {
        delegate.setEnabledProtocols(protocols);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getEnabledCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public void setEnabledCipherSuites(final String[] suites) {
        delegate.setEnabledCipherSuites(suites);
    }

    @Override
    public void setNeedClientAuth(final boolean state) {
        delegate.setNeedClientAuth(state);
    }

    @Override
    public void setWantClientAuth(final boolean state) {
        delegate.setWantClientAuth(state);
    }

    @Override
    public void setHostnameVerification(boolean hostnameVerification) {
        if (!hostnameVerification) {
           return;
        }
        SSLParameters sslParameters = delegate.getSSLParameters();
        sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
        delegate.setSSLParameters(sslParameters);
    }
    
}
