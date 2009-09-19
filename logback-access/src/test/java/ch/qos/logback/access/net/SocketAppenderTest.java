/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.access.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.access.dummy.DummyResponse;
import ch.qos.logback.access.dummy.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessContext;
import ch.qos.logback.access.spi.AccessEvent;


public class SocketAppenderTest {

  private AccessContext context;
  private MockSocketServer mockSocketServer;

  @Test
  public void testStartFailNoRemoteHost() {
    context = new AccessContext();
    SocketAppender appender = new SocketAppender();
    appender.setContext(context);
    appender.setPort(123);
    appender.start();
    assertEquals(1, context.getStatusManager().getCount());
  }

  @Test
  public void testRecieveMessage() throws InterruptedException {
    startServer(1);
    configureClient();
    
    context.callAppenders(buildNewAccessEvent());
    // Wait max 2 seconds for mock server to finish. However, it should
    // finish much sooner than that.
    mockSocketServer.join(2000);
    assertTrue(mockSocketServer.finished);
    assertEquals(1, mockSocketServer.accessEventList.size());

    AccessEvent remoteEvent = mockSocketServer.accessEventList.get(0);
    //check that the values are available although the request and response
    //objects did not survive serialization
    assertEquals("headerValue1", remoteEvent.getRequestHeader("headerName1"));
    assertEquals("testHost", remoteEvent.getRemoteHost());
  }

  private void startServer(int expectedEventNumber) throws InterruptedException {
    mockSocketServer = new MockSocketServer(expectedEventNumber);
    mockSocketServer.start();
    // give MockSocketServer head start
    Thread.sleep(100);
  }

  private void configureClient() {
    context = new AccessContext();
    context.setName("test");
    SocketAppender socketAppender = new SocketAppender();
    socketAppender.setContext(context);
    socketAppender.setName("socket");
    socketAppender.setPort(MockSocketServer.PORT);
    socketAppender.setRemoteHost("localhost");
    context.addAppender(socketAppender);
    socketAppender.start();
  }
  
  private AccessEvent buildNewAccessEvent() {
    DummyRequest request = new DummyRequest();
    DummyResponse response = new DummyResponse();
    DummyServerAdapter adapter = new DummyServerAdapter(request, response);
    
    AccessEvent ae = new AccessEvent(request, response, adapter);
    return ae;
  }
}
