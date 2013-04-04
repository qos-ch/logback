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

package ch.qos.logback.classic.net;

import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.net.mock.MockContext;

/**
 * Unit tests for {@link SSLSocketRemote}.
 *
 * @author Carl Harris
 */
public class SSLSocketRemoteTest {

  private MockContext context = new MockContext();

  private SSLSocketRemote remote =
      new SSLSocketRemote();
  
  @Before
  public void setUp() throws Exception {
    remote.setContext(context);
  }
  
  @Test
  public void testUsingDefaultConfig() throws Exception {
    // should be able to start successfully with no SSL configuration at all
    remote.setHost(InetAddress.getLocalHost().getHostAddress());
    remote.setPort(6000);
    remote.start();
    assertNotNull(remote.getSocketFactory());
  }
  
}
