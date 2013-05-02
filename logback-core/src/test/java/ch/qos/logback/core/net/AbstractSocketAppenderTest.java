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

package ch.qos.logback.core.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.net.mock.MockContext;
import ch.qos.logback.core.net.server.ServerSocketUtil;
import ch.qos.logback.core.spi.PreSerializationTransformer;

/**
 * Unit tests for {@link AbstractSocketAppender}.
 *
 * @author Carl Harris
 */
public class AbstractSocketAppenderTest {

  private static final int DELAY = 10000;
  
  private ExecutorService executorService = Executors.newCachedThreadPool();
  private MockContext context = new MockContext(executorService);
  private InstrumentedSocketAppenderBase appender =
      new InstrumentedSocketAppenderBase();
  
  @Before
  public void setUp() throws Exception {
    appender.setContext(context);
  }
  
  @After
  public void tearDown() throws Exception {
    appender.stop();
    assertFalse(appender.isStarted());
    executorService.shutdownNow();
    assertTrue(executorService.awaitTermination(DELAY, TimeUnit.MILLISECONDS));
  }
  
  @Test
  public void testStartWithNoPort() throws Exception {
    appender.setPort(-1);
    appender.setRemoteHost("localhost");
    appender.setQueueSize(0);
    appender.start();
    assertFalse(appender.isStarted());
    assertTrue(context.getLastStatus().getMessage().contains("port"));
  }

  @Test
  public void testStartWithNoRemoteHost() throws Exception {
    appender.setPort(1);
    appender.setRemoteHost(null);
    appender.setQueueSize(0);
    appender.start();
    assertFalse(appender.isStarted());
    assertTrue(context.getLastStatus().getMessage().contains("remote host"));
  }
 
  @Test
  public void testStartWithNegativeQueueSize() throws Exception {
    appender.setPort(1);
    appender.setRemoteHost("localhost");
    appender.setQueueSize(-1);
    appender.start();
    assertFalse(appender.isStarted());
    assertTrue(context.getLastStatus().getMessage().contains("Queue"));
  }
 
  @Test
  public void testStartWithUnresolvableRemoteHost() throws Exception {
    appender.setPort(1);
    appender.setRemoteHost("NOT.A.VALID.REMOTE.HOST.NAME");
    appender.setQueueSize(0);
    appender.start();
    assertFalse(appender.isStarted());
    assertTrue(context.getLastStatus().getMessage().contains("unknown host"));
  }
 
  @Test
  public void testStartWithZeroQueueLength() throws Exception {
    appender.setPort(1);
    appender.setRemoteHost("localhost");
    appender.setQueueSize(0);
    appender.start();
    assertTrue(appender.isStarted());
    assertTrue(appender.lastQueue instanceof SynchronousQueue);
  }

  @Test
  public void testStartWithNonZeroQueueLength() throws Exception {
    appender.setPort(1);
    appender.setRemoteHost("localhost");
    appender.setQueueSize(1);
    appender.start();
    assertTrue(appender.isStarted());
    assertTrue(appender.lastQueue instanceof ArrayBlockingQueue);
    assertEquals(1, appender.lastQueue.remainingCapacity());
  }
  
  @Test
  public void testAppendWhenNotStarted() throws Exception {
    appender.setRemoteHost("localhost");
    appender.start();
    appender.stop();

    // make sure the appender task has stopped
    executorService.shutdownNow();
    assertTrue(executorService.awaitTermination(DELAY, TimeUnit.MILLISECONDS));
    
    appender.append("some event");
    assertTrue(appender.lastQueue.isEmpty());
  }
  
  @Test
  public void testAppendNullEvent() throws Exception {
    appender.setRemoteHost("localhost");
    appender.start();

    appender.append("some event");
    assertTrue(appender.lastQueue.isEmpty());
  }
  
  @Test
  public void testAppendEvent() throws Exception {
    appender.setRemoteHost("localhost");
    appender.setQueueSize(1);
    appender.start();

    // stop the appender task, but don't stop the appender
    executorService.shutdownNow();
    assertTrue(executorService.awaitTermination(DELAY, TimeUnit.MILLISECONDS));
    
    appender.append("some event");
    assertEquals("some event", appender.lastQueue.poll());
  }

  @Test
  public void testDispatchEvent() throws Exception {
    ServerSocket serverSocket = ServerSocketUtil.createServerSocket();
    appender.setRemoteHost(serverSocket.getInetAddress().getHostAddress());
    appender.setPort(serverSocket.getLocalPort());
    appender.setQueueSize(1);
    appender.start();
    
    Socket appenderSocket = serverSocket.accept();
    serverSocket.close();

    appender.append("some event");
    
    final int shortDelay = 100;
    for (int i = 0, retries = DELAY / shortDelay; 
        !appender.lastQueue.isEmpty() && i < retries; 
        i++) {
      Thread.sleep(shortDelay);
    }
    assertTrue(appender.lastQueue.isEmpty());
    
    ObjectInputStream ois = new ObjectInputStream(appenderSocket.getInputStream());
    assertEquals("some event", ois.readObject());
    appenderSocket.close();
 
  }
  
  private static class InstrumentedSocketAppenderBase
      extends AbstractSocketAppender<String> {

    private BlockingQueue<String> lastQueue;
    
    @Override
    protected void postProcessEvent(String event) {
    }

    @Override
    protected PreSerializationTransformer<String> getPST() {
      return new PreSerializationTransformer<String>() {
        public Serializable transform(String event) {
          return event;
        }
      };
    }

    @Override
    BlockingQueue<String> newBlockingQueue(int queueSize) {
      lastQueue = super.newBlockingQueue(queueSize);
      return lastQueue;
    }
    
  }
  
}
