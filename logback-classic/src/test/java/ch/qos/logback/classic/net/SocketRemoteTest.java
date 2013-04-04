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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.mock.MockAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.LoggingEventVO;
import ch.qos.logback.core.net.SocketConnector;
import ch.qos.logback.core.net.mock.MockContext;
import ch.qos.logback.core.net.server.ServerSocketUtil;

/**
 * Unit tests for {@link SocketRemote}.
 *
 * @author Carl Harris
 */
public class SocketRemoteTest {

  private static final int DELAY = 200;
  private static final String TEST_HOST_NAME = "NOT.A.VALID.HOST.NAME";


  private ServerSocket serverSocket;
  private Socket socket;
  private ExecutorService executor = Executors.newCachedThreadPool();
  private MockContext context = new MockContext();
  private MockSocketFactory socketFactory = new MockSocketFactory();
  private MockSocketConnector connector;
  private MockAppender appender;
  private LoggerContext lc;
  private Logger logger;
  
  private InstrumentedSocketRemote remote = 
      new InstrumentedSocketRemote();
  
  @Before
  public void setUp() throws Exception {
    serverSocket = ServerSocketUtil.createServerSocket();
    socket = new Socket(serverSocket.getInetAddress(), 
        serverSocket.getLocalPort());
    connector = new MockSocketConnector(socket);
    remote.setContext(context);    

    lc = (LoggerContext) LoggerFactory.getILoggerFactory();    
    appender = new MockAppender();
    appender.start();
    logger = lc.getLogger(getClass());
    logger.addAppender(appender);
  }
  
  @After
  public void tearDown() throws Exception {
    remote.stop();
    if (!remote.isExecutorCreated()) {
      executor.shutdownNow();
    }
    assertTrue(executor.awaitTermination(DELAY, TimeUnit.MILLISECONDS));
    socket.close();
    serverSocket.close();
    lc.stop();
  }
  
  @Test
  public void testStartNoRemoteAddress() throws Exception {
    remote.start();
    assertTrue(context.getLastStatus().getMessage().contains("host"));
  }

  @Test
  public void testStartNoPort() throws Exception {
    remote.setHost(TEST_HOST_NAME);
    remote.start();
    assertTrue(context.getLastStatus().getMessage().contains("port"));
  }

  @Test
  public void testStartUnknownHost() throws Exception {
    remote.setPort(6000);
    remote.setHost(TEST_HOST_NAME);
    remote.start();
    assertTrue(context.getLastStatus().getMessage().contains("unknown host"));
  }
  
  @Test
  public void testStartStop() throws Exception {
    remote.setHost(InetAddress.getLocalHost().getHostName());
    remote.setPort(6000);
    remote.setAcceptConnectionTimeout(DELAY / 2);
    remote.start();
    assertTrue(remote.isStarted());
    remote.awaitConnectorCreated(DELAY);
    remote.stop();
    assertFalse(remote.isStarted());
  }
  
  @Test
  public void testServerSlowToAcceptConnection() throws Exception {
    remote.setHost(InetAddress.getLocalHost().getHostName());
    remote.setPort(6000);
    remote.setAcceptConnectionTimeout(DELAY / 4);
    remote.start();
    assertTrue(remote.awaitConnectorCreated(DELAY / 2));
    // note that we don't call serverSocket.accept() here
    // but stop (in tearDown) should still clean up everything
  }

  @Test
  public void testServerDropsConnection() throws Exception {
    remote.setHost(InetAddress.getLocalHost().getHostName());
    remote.setPort(6000);
    remote.start();
    assertTrue(remote.awaitConnectorCreated(DELAY));
    Socket socket = serverSocket.accept();
    socket.close();
  }
  
  @Test
  public void testDispatchEventForEnabledLevel() throws Exception {
    remote.setHost(InetAddress.getLocalHost().getHostName());
    remote.setPort(6000);
    remote.start();
    assertTrue(remote.awaitConnectorCreated(DELAY));
    Socket socket = serverSocket.accept();

    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
    
    logger.setLevel(Level.DEBUG);
    ILoggingEvent event = new LoggingEvent(logger.getName(), logger, 
        Level.DEBUG, "test message", null, new Object[0]);

    LoggingEventVO eventVO = LoggingEventVO.build(event);
    oos.writeObject(eventVO);
    oos.flush();

    appender.awaitAppend(DELAY);
    ILoggingEvent rcvdEvent = appender.getLastEvent();
    assertNotNull(rcvdEvent);
    assertEquals(event.getLoggerName(), rcvdEvent.getLoggerName());
    assertEquals(event.getLevel(), rcvdEvent.getLevel());
    assertEquals(event.getMessage(), rcvdEvent.getMessage());
  }

  @Test
  public void testNoDispatchEventForDisabledLevel() throws Exception {
    remote.setHost(InetAddress.getLocalHost().getHostName());
    remote.setPort(6000);
    remote.start();
    assertTrue(remote.awaitConnectorCreated(DELAY));
    Socket socket = serverSocket.accept();

    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
    
    logger.setLevel(Level.INFO);
    ILoggingEvent event = new LoggingEvent(logger.getName(), logger, 
        Level.DEBUG, "test message", null, new Object[0]);

    LoggingEventVO eventVO = LoggingEventVO.build(event);
    oos.writeObject(eventVO);
    oos.flush();

    assertFalse(appender.awaitAppend(DELAY));
  }

  /**
   * A {@link SocketRemote} with instrumentation for unit testing.
   */
  private class InstrumentedSocketRemote extends SocketRemote {

    private boolean connectorCreated;
    private boolean executorCreated;

    @Override
    protected synchronized SocketConnector newConnector(
        InetAddress address, int port, int initialDelay, int retryDelay) {
      connectorCreated = true;
      notifyAll();
      return connector;
    }

    @Override
    protected SocketFactory getSocketFactory() {
      return socketFactory;
    }

    @Override
    protected ExecutorService createExecutorService() {
      return executor;
    }

    public synchronized boolean awaitConnectorCreated(long delay) 
        throws InterruptedException {
      while (!connectorCreated) {
        wait(delay);
      }
      return connectorCreated;
    }

    public boolean isExecutorCreated() {
      return executorCreated;
    }
    
  }
  
  /**
   * A {@link SocketConnector} with instrumentation for unit testing.
   */
  private static class MockSocketConnector implements SocketConnector {

    private final Socket socket;
    
    public MockSocketConnector(Socket socket) {
      this.socket = socket;
    }

    public void run() {
    }

    public Socket awaitConnection() throws InterruptedException {
      return awaitConnection(Long.MAX_VALUE);
    }

    public Socket awaitConnection(long delay) throws InterruptedException {
      return socket;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
    }

    public void setSocketFactory(SocketFactory socketFactory) {
    }

  }

  /**
   * A no-op {@link SocketFactory} to support unit testing.
   */
  private static class MockSocketFactory extends SocketFactory {

    @Override
    public Socket createSocket(InetAddress address, int port,
        InetAddress localAddress, int localPort) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost,
        int localPort) throws IOException, UnknownHostException {
      throw new UnsupportedOperationException();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException,
        UnknownHostException {
      throw new UnsupportedOperationException();
    }
    
  }
  
}
