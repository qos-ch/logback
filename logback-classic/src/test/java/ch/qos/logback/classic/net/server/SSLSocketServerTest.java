/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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

/**
 * Unit tests for {@link SSLSocketServer}.
 *
 * @author Carl Harris
 */
public class SSLSocketServerTest {

  private MockContext context = new MockContext();
  
  private MockSSLConfiguration ssl = new MockSSLConfiguration();
  
  private MockSSLParametersConfiguration parameters = 
      new MockSSLParametersConfiguration();
  
  private SSLSocketServer socketServer = new SSLSocketServer();
  
  @Before
  public void setUp() throws Exception {
    socketServer.setContext(context);
    socketServer.setSsl(ssl);
    ssl.setParameters(parameters);
  }
  
  @Test
  public void testGetServerSocketFactory() throws Exception {
    ServerSocketFactory socketFactory = socketServer.getServerSocketFactory();
    assertNotNull(socketFactory);
    assertTrue(ssl.isContextCreated());
    assertTrue(parameters.isContextInjected());
  }

}
