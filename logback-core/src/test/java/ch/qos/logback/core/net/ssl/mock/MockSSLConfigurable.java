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

    private static final String[] EMPTY = new String[0];

    private String[] defaultProtocols = EMPTY;
    private String[] supportedProtocols = EMPTY;
    private String[] enabledProtocols = EMPTY;
    private String[] defaultCipherSuites = EMPTY;
    private String[] supportedCipherSuites = EMPTY;
    private String[] enabledCipherSuites = EMPTY;
    private boolean needClientAuth;
    private boolean wantClientAuth;

    public String[] getDefaultProtocols() {
        return defaultProtocols;
    }

    public void setDefaultProtocols(String[] defaultProtocols) {
        this.defaultProtocols = defaultProtocols;
    }

    public String[] getSupportedProtocols() {
        return supportedProtocols;
    }

    public void setSupportedProtocols(String[] supportedProtocols) {
        this.supportedProtocols = supportedProtocols;
    }

    public String[] getEnabledProtocols() {
        return enabledProtocols;
    }

    public void setEnabledProtocols(String[] enabledProtocols) {
        this.enabledProtocols = enabledProtocols;
    }

    public String[] getDefaultCipherSuites() {
        return defaultCipherSuites;
    }

    public void setDefaultCipherSuites(String[] defaultCipherSuites) {
        this.defaultCipherSuites = defaultCipherSuites;
    }

    public String[] getSupportedCipherSuites() {
        return supportedCipherSuites;
    }

    public void setSupportedCipherSuites(String[] supportedCipherSuites) {
        this.supportedCipherSuites = supportedCipherSuites;
    }

    public String[] getEnabledCipherSuites() {
        return enabledCipherSuites;
    }

    public void setEnabledCipherSuites(String[] enabledCipherSuites) {
        this.enabledCipherSuites = enabledCipherSuites;
    }

    public boolean isNeedClientAuth() {
        return needClientAuth;
    }

    public void setNeedClientAuth(boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
    }

    public boolean isWantClientAuth() {
        return wantClientAuth;
    }

    public void setWantClientAuth(boolean wantClientAuth) {
        this.wantClientAuth = wantClientAuth;
    }

	@Override
	public void setHostnameVerification(boolean verifyHostname) {
	}

}
