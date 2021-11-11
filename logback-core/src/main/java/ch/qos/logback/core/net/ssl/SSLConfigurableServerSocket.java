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

import javax.net.ssl.SSLServerSocket;

/**
 * An {@link SSLConfigurable} wrapper for an {@link SSLServerSocket}.
 *
 * @author Carl Harris
 * @author Bruno Harbulot 
 */
public class SSLConfigurableServerSocket implements SSLConfigurable {

    private final SSLServerSocket delegate;

    public SSLConfigurableServerSocket(SSLServerSocket delegate) {
        this.delegate = delegate;
    }

    public String[] getDefaultProtocols() {
        return delegate.getEnabledProtocols();
    }

    public String[] getSupportedProtocols() {
        return delegate.getSupportedProtocols();
    }

    public void setEnabledProtocols(String[] protocols) {
        delegate.setEnabledProtocols(protocols);
    }

    public String[] getDefaultCipherSuites() {
        return delegate.getEnabledCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    public void setEnabledCipherSuites(String[] suites) {
        delegate.setEnabledCipherSuites(suites);
    }

    public void setNeedClientAuth(boolean state) {
        delegate.setNeedClientAuth(state);
    }

    public void setWantClientAuth(boolean state) {
        delegate.setWantClientAuth(state);
    }

	@Override
	public void setHostnameVerification(boolean verifyHostname) {
		// This is not relevant for a server socket
	}

}
