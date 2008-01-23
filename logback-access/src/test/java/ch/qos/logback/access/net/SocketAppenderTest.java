/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.access.net;

import junit.framework.TestCase;
import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.access.dummy.DummyResponse;
import ch.qos.logback.access.dummy.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.BasicContext;


public class SocketAppenderTest extends TestCase {

  private BasicContext context;
  private MockSocketServer mockSocketServer;

  public void testStartFailNoRemoteHost() {
    context = new BasicContext();
    SocketAppender appender = new SocketAppender();
    appender.setContext(context);
    appender.setPort(123);
    appender.start();
    assertEquals(1, context.getStatusManager().getCount());
  }

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
    context = new BasicContext();
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
