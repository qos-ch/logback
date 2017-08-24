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

/**
 * An object that has configurable SSL parameters.
 * <p>
 * This interface allows us o decouple the {@link SSLParametersConfiguration}
 * from {@link SSLSocket} and {@link SSLServerSocket} to facilitate unit
 * testing.
 *
 * @author Carl Harris
 */
public interface SSLConfigurable {

    /**
     * Gets the set of protocols that the SSL component enables by default.
     * 
     * @return protocols (generally a subset of the set returned by
     *    {@link #getSupportedProtocols()}); the return value may be 
     *    an empty array but must never be {@code null}.
     */
    String[] getDefaultProtocols();

    /**
     * Gets the set of protocols that the SSL component supports.
     * @return protocols supported protocols; the return value may be 
     *    an empty array but must never be {@code null}.
     */
    String[] getSupportedProtocols();

    /**
     * Sets the enabled protocols on the SSL component.
     * @param cipherSuites the protocols to enable
     */
    void setEnabledProtocols(String[] protocols);

    /**
     * Gets the set of cipher suites that the SSL component enables by default.
     * 
     * @return cipher suites (generally a subset of the set returned by
     *    {@link #getSupportedCipherSuites()}); the return value may be 
     *    an empty array but must never be {@code null}
     */
    String[] getDefaultCipherSuites();

    /**
     * Gets the set of cipher suites that the SSL component supports.
     * @return supported cipher suites; the return value may be 
     *    an empty array but must never be {@code null}
     */
    String[] getSupportedCipherSuites();

    /**
     * Sets the enabled cipher suites on the SSL component.
     * @param cipherSuites the cipher suites to enable
     */
    void setEnabledCipherSuites(String[] cipherSuites);

    /**
     * Sets a flag indicating whether the SSL component should require 
     * client authentication.
     * @param state the flag state to set
     */
    void setNeedClientAuth(boolean state);

    /**
     * Sets a flag indicating whether the SSL component should request 
     * client authentication.
     * @param state the flag state to set
     */
    void setWantClientAuth(boolean state);

}
