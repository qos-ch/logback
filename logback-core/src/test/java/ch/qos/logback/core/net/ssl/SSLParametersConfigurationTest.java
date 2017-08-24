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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.net.ssl.mock.MockSSLConfigurable;

/**
 * Unit tests for {@link SSLParametersConfiguration}.
 *
 * @author Carl Harris
 */
public class SSLParametersConfigurationTest {

    private MockSSLConfigurable configurable = new MockSSLConfigurable();

    private SSLParametersConfiguration configuration = new SSLParametersConfiguration();

    @Before
    public void setUp() throws Exception {
        configuration.setContext(new ContextBase());
    }

    @Test
    public void testSetIncludedProtocols() throws Exception {
        configurable.setSupportedProtocols(new String[] { "A", "B", "C", "D" });
        configuration.setIncludedProtocols("A,B ,C, D");
        configuration.configure(configurable);
        assertTrue(Arrays.equals(new String[] { "A", "B", "C", "D" }, configurable.getEnabledProtocols()));
    }

    @Test
    public void testSetExcludedProtocols() throws Exception {
        configurable.setSupportedProtocols(new String[] { "A", "B" });
        configuration.setExcludedProtocols("A");
        configuration.configure(configurable);
        assertTrue(Arrays.equals(new String[] { "B" }, configurable.getEnabledProtocols()));
    }

    @Test
    public void testSetIncludedAndExcludedProtocols() throws Exception {
        configurable.setSupportedProtocols(new String[] { "A", "B", "C" });
        configuration.setIncludedProtocols("A, B");
        configuration.setExcludedProtocols("B");
        configuration.configure(configurable);
        assertTrue(Arrays.equals(new String[] { "A" }, configurable.getEnabledProtocols()));
    }

    @Test
    public void testSetIncludedCipherSuites() throws Exception {
        configurable.setSupportedCipherSuites(new String[] { "A", "B", "C", "D" });
        configuration.setIncludedCipherSuites("A,B ,C, D");
        configuration.configure(configurable);
        assertTrue(Arrays.equals(new String[] { "A", "B", "C", "D" }, configurable.getEnabledCipherSuites()));
    }

    @Test
    public void testSetExcludedCipherSuites() throws Exception {
        configurable.setSupportedCipherSuites(new String[] { "A", "B" });
        configuration.setExcludedCipherSuites("A");
        configuration.configure(configurable);
        assertTrue(Arrays.equals(new String[] { "B" }, configurable.getEnabledCipherSuites()));
    }

    @Test
    public void testSetExcludedAndIncludedCipherSuites() throws Exception {
        configurable.setSupportedCipherSuites(new String[] { "A", "B", "C" });
        configuration.setIncludedCipherSuites("A, B");
        configuration.setExcludedCipherSuites("B");
        configuration.configure(configurable);
        assertTrue(Arrays.equals(new String[] { "A" }, configurable.getEnabledCipherSuites()));
    }

    @Test
    public void testSetNeedClientAuth() throws Exception {
        configuration.setNeedClientAuth(true);
        configuration.configure(configurable);
        assertTrue(configurable.isNeedClientAuth());
    }

    @Test
    public void testSetWantClientAuth() throws Exception {
        configuration.setWantClientAuth(true);
        configuration.configure(configurable);
        assertTrue(configurable.isWantClientAuth());
    }

    @Test
    public void testPassDefaultProtocols() throws Exception {
        final String[] protocols = new String[] { "A" };
        configurable.setDefaultProtocols(protocols);
        configuration.configure(configurable);
        assertTrue(Arrays.equals(protocols, configurable.getEnabledProtocols()));
    }

    @Test
    public void testPassDefaultCipherSuites() throws Exception {
        final String[] cipherSuites = new String[] { "A" };
        configurable.setDefaultCipherSuites(cipherSuites);
        configuration.configure(configurable);
        assertTrue(Arrays.equals(cipherSuites, configurable.getEnabledCipherSuites()));
    }

    @Test
    public void testPassDefaultNeedClientAuth() throws Exception {
        configurable.setNeedClientAuth(true);
        configuration.configure(configurable);
        assertTrue(configurable.isNeedClientAuth());
    }

    @Test
    public void testPassDefaultWantClientAuth() throws Exception {
        configurable.setWantClientAuth(true);
        configuration.configure(configurable);
        assertTrue(configurable.isWantClientAuth());
    }

}
