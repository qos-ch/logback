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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.LoggingEventVO;

/**
 * A functional test for {@link SocketServer}.
 * <p>
 * In this test we create a SocketServer, connect to it over the local
 * network interface, and validate that it receives messages and delivers
 * them to its appender.
 */
public class SocketServerFunctionalTest {

  private static final int EVENT_COUNT = 10;
  
  private MockAppender appender;
  private Logger logger;
  private ServerSocket serverSocket;
  private ExecutorService executor = Executors.newFixedThreadPool(2);
  private InstrumentedSocketServer socketServer;
  
  @Before
  public void setUp() throws Exception {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    
    appender = new MockAppender();
    appender.start();
   
    logger = lc.getLogger(getClass());
    logger.addAppender(appender);

    serverSocket = ServerSocketUtil.createServerSocket();
    
    socketServer = new InstrumentedSocketServer(serverSocket);
    
    socketServer.setThreadPool(new ThreadPoolFactoryBean() {
      @Override
      public Executor createExecutor() {
        return executor;
      } 
    });
    
    socketServer.setContext(lc);
  }
  
  @After
  public void tearDown() throws Exception {
    socketServer.stop();
  }
  
  @Test
  public void testLogEventFromClient() throws Exception {
    ILoggingEvent event = new LoggingEvent(logger.getName(), logger, 
        Level.DEBUG, "test message", null, new Object[0]);
    socketServer.start();
    Socket socket = new Socket(InetAddress.getLocalHost(), 
        serverSocket.getLocalPort());
    
    try {      
      LoggingEventVO eventVO = LoggingEventVO.build(event);
      
      ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
      for (int i = 0; i < EVENT_COUNT; i++) {
        oos.writeObject(eventVO);
      }

      oos.writeObject(eventVO);
      oos.flush();
    }
    finally {
      socket.close();
      serverSocket.close();
    }

    executor.shutdown();
    executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
    assertTrue(executor.isTerminated());
    
    ILoggingEvent rcvdEvent = appender.getLastEvent();
    assertNotNull(rcvdEvent);
    assertEquals(event.getLoggerName(), rcvdEvent.getLoggerName());
    assertEquals(event.getLevel(), rcvdEvent.getLevel());
    assertEquals(event.getMessage(), rcvdEvent.getMessage());
  }

}
