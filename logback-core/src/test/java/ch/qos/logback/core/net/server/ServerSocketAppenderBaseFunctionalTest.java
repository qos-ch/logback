/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.net.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A functional test for {@link ServerSocketAppenderBase}.
 *
 * @author Carl Harris
 */
public class ServerSocketAppenderBaseFunctionalTest {

  private static final String TEST_EVENT = "test event";

  private static final int EVENT_COUNT = 10;
  
  private MockContext context = new MockContext();
  private ServerSocket serverSocket;
  private ExecutorService executor = Executors.newFixedThreadPool(2);
  private InstrumentedServerSocketAppenderBase appender;
  
  @Before
  public void setUp() throws Exception {

    serverSocket = ServerSocketUtil.createServerSocket();
    
    appender = new InstrumentedServerSocketAppenderBase(serverSocket);
    
    appender.setThreadPool(new ThreadPoolFactoryBean() {
      @Override
      public ExecutorService createExecutor() {
        return executor;
      } 
    });
    
    appender.setContext(context);
  }
  
  @After
  public void tearDown() throws Exception {
    executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
    assertTrue(executor.isTerminated());
  }
  
  @Test
  public void testLogEventClient() throws Exception {
    appender.start();
    Socket socket = new Socket(InetAddress.getLocalHost(), 
        serverSocket.getLocalPort());
    
    socket.setSoTimeout(1000);
    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
 
    for (int i = 0; i < EVENT_COUNT; i++) {
      appender.append(TEST_EVENT + i);
      assertEquals(TEST_EVENT + i, ois.readObject());
    }
    
    socket.close();
    appender.stop();
  }

}
