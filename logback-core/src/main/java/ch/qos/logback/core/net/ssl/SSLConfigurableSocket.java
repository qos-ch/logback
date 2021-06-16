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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;

/**
 * An {@link SSLConfigurable} wrapper for an {@link SSLSocket}.
 *
 * @author Carl Harris
 */
public class SSLConfigurableSocket implements SSLConfigurable {

    private final SSLSocket delegate;

    public SSLConfigurableSocket(SSLSocket delegate) {
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

    public void setDisableHostnameVerification(boolean disableHostnameVerification) {
        if (disableHostnameVerification) {
            return;
        }
        SSLParameters sslParameters = delegate.getSSLParameters();
        try {
            /*
             * SSLParameters.setEndpointIdentificationAlgorithm(...)
             * is only available from Java 7, so we use reflection
             * to check whether it is available.
             */
            Method setEndpointIdentificationAlgorithmMethod = SSLParameters.class.getMethod("setEndpointIdentificationAlgorithm", String.class);
            setEndpointIdentificationAlgorithmMethod.invoke(sslParameters, "HTTPS");
            delegate.setSSLParameters(sslParameters);
        } catch (NoSuchMethodException e) {
            // We don't do anything when the method is not available.
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
