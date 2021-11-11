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
package ch.qos.logback.core.net.ssl.mock;

import ch.qos.logback.core.net.ssl.SSLConfigurable;

public class MockSSLConfigurable implements SSLConfigurable {

    private static final String[] EMPTY = {};

    private String[] defaultProtocols = EMPTY;
    private String[] supportedProtocols = EMPTY;
    private String[] enabledProtocols = EMPTY;
    private String[] defaultCipherSuites = EMPTY;
    private String[] supportedCipherSuites = EMPTY;
    private String[] enabledCipherSuites = EMPTY;
    private boolean needClientAuth;
    private boolean wantClientAuth;

    @Override
    public String[] getDefaultProtocols() {
        return defaultProtocols;
    }

    public void setDefaultProtocols(final String[] defaultProtocols) {
        this.defaultProtocols = defaultProtocols;
    }

    @Override
    public String[] getSupportedProtocols() {
        return supportedProtocols;
    }

    public void setSupportedProtocols(final String[] supportedProtocols) {
        this.supportedProtocols = supportedProtocols;
    }

    public String[] getEnabledProtocols() {
        return enabledProtocols;
    }

    @Override
    public void setEnabledProtocols(final String[] enabledProtocols) {
        this.enabledProtocols = enabledProtocols;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return defaultCipherSuites;
    }

    public void setDefaultCipherSuites(final String[] defaultCipherSuites) {
        this.defaultCipherSuites = defaultCipherSuites;
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return supportedCipherSuites;
    }

    public void setSupportedCipherSuites(final String[] supportedCipherSuites) {
        this.supportedCipherSuites = supportedCipherSuites;
    }

    public String[] getEnabledCipherSuites() {
        return enabledCipherSuites;
    }

    @Override
    public void setEnabledCipherSuites(final String[] enabledCipherSuites) {
        this.enabledCipherSuites = enabledCipherSuites;
    }

    public boolean isNeedClientAuth() {
        return needClientAuth;
    }

    @Override
    public void setNeedClientAuth(final boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
    }

    public boolean isWantClientAuth() {
        return wantClientAuth;
    }

    @Override
    public void setWantClientAuth(final boolean wantClientAuth) {
        this.wantClientAuth = wantClientAuth;
    }

	@Override
	public void setHostnameVerification(boolean verifyHostname) {
	}

}
