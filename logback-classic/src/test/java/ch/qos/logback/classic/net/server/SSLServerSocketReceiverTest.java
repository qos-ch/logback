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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.net.ServerSocketFactory;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.net.mock.MockContext;

/**
 * Unit tests for {@link SSLServerSocketReceiver}.
 *
 * @author Carl Harris
 */
public class SSLServerSocketReceiverTest {

    private MockContext context = new MockContext();

    private MockSSLConfiguration ssl = new MockSSLConfiguration();

    private MockSSLParametersConfiguration parameters = new MockSSLParametersConfiguration();

    private SSLServerSocketReceiver receiver = new SSLServerSocketReceiver();

    @Before
    public void setUp() throws Exception {
        receiver.setContext(context);
        receiver.setSsl(ssl);
        ssl.setParameters(parameters);
    }

    @Test
    public void testGetServerSocketFactory() throws Exception {
        ServerSocketFactory socketFactory = receiver.getServerSocketFactory();
        assertNotNull(socketFactory);
        assertTrue(ssl.isContextCreated());
        assertTrue(parameters.isContextInjected());
    }

}
