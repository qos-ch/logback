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
import java.util.List;
import java.util.concurrent.*;

import ch.qos.logback.core.BasicStatusManager;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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

  private ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool();
  private MockContext mockContext = new MockContext(executorService);
  private InstrumentedSocketAppender instrumentedAppender = new InstrumentedSocketAppender();

  @Before
  public void setUp() throws Exception {
    instrumentedAppender.setContext(mockContext);
  }

  @After
  public void tearDown() throws Exception {
    instrumentedAppender.stop();
    assertFalse(instrumentedAppender.isStarted());
    executorService.shutdownNow();
    assertTrue(executorService.awaitTermination(DELAY, TimeUnit.MILLISECONDS));
  }

  @Test
  public void appenderShouldFailToStartWithoutValidPort() throws Exception {
    instrumentedAppender.setPort(-1);
    instrumentedAppender.setRemoteHost("localhost");
    instrumentedAppender.setQueueSize(0);
    instrumentedAppender.start();
    assertFalse(instrumentedAppender.isStarted());
    assertTrue(mockContext.getLastStatus().getMessage().contains("port"));
  }

  @Test
  public void appenderShouldFailToStartWithoutValidRemoteHost() throws Exception {
    instrumentedAppender.setPort(1);
    instrumentedAppender.setRemoteHost(null);
    instrumentedAppender.setQueueSize(0);
    instrumentedAppender.start();
    assertFalse(instrumentedAppender.isStarted());
    assertTrue(mockContext.getLastStatus().getMessage().contains("remote host"));
  }

  @Test
  public void appenderShouldFailToStartWithNegativeQueueSize() throws Exception {
    instrumentedAppender.setPort(1);
    instrumentedAppender.setRemoteHost("localhost");
    instrumentedAppender.setQueueSize(-1);
    instrumentedAppender.start();
    assertFalse(instrumentedAppender.isStarted());
    assertTrue(mockContext.getLastStatus().getMessage().contains("Queue"));
  }

  @Test
  public void appenderShouldFailToStartWithUnresolvableRemoteHost() throws Exception {
    instrumentedAppender.setPort(1);
    instrumentedAppender.setRemoteHost("NOT.A.VALID.REMOTE.HOST.NAME");
    instrumentedAppender.setQueueSize(0);
    instrumentedAppender.start();
    assertFalse(instrumentedAppender.isStarted());
    assertTrue(mockContext.getLastStatus().getMessage().contains("unknown host"));
  }

  @Test
  public void appenderShouldFailToStartWithZeroQueueLength() throws Exception {
    instrumentedAppender.setPort(1);
    instrumentedAppender.setRemoteHost("localhost");
    instrumentedAppender.setQueueSize(0);
    instrumentedAppender.start();
    assertTrue(instrumentedAppender.isStarted());
    assertTrue(instrumentedAppender.lastQueue instanceof SynchronousQueue);
  }

  @Test
  public void appenderShouldStartWithValidParameters() throws Exception {
    instrumentedAppender.setPort(1);
    instrumentedAppender.setRemoteHost("localhost");
    instrumentedAppender.setQueueSize(1);
    instrumentedAppender.start();
    assertTrue(instrumentedAppender.isStarted());
    assertTrue(instrumentedAppender.lastQueue instanceof ArrayBlockingQueue);
    assertEquals(1, instrumentedAppender.lastQueue.remainingCapacity());
  }

  // this test takes 1 second and is deemed too long
  @Ignore
  @Test(timeout = 2000)
  public void appenderShouldCleanupTasksWhenStopped() throws Exception {
    mockContext.setStatusManager(new BasicStatusManager());
    instrumentedAppender.setPort(1);
    instrumentedAppender.setRemoteHost("localhost");
    instrumentedAppender.setQueueSize(1);
    instrumentedAppender.start();
    assertTrue(instrumentedAppender.isStarted());

    waitForActiveCountToEqual(executorService, 2);
    instrumentedAppender.stop();
    waitForActiveCountToEqual(executorService, 0);
    StatusPrinter.print(mockContext);
    assertEquals(0, executorService.getActiveCount());

  }

  private void waitForActiveCountToEqual(ThreadPoolExecutor executorService, int i) {
    while (executorService.getActiveCount() != i) {
      try {
        Thread.yield();
        Thread.sleep(1);
        System.out.print(".");
      } catch (InterruptedException e) {
      }
    }
  }


  @Test
  public void testAppendWhenNotStarted() throws Exception {
    instrumentedAppender.setRemoteHost("localhost");
    instrumentedAppender.start();
    instrumentedAppender.stop();

    // make sure the appender task has stopped
    executorService.shutdownNow();
    assertTrue(executorService.awaitTermination(DELAY, TimeUnit.MILLISECONDS));

    instrumentedAppender.append("some event");
    assertTrue(instrumentedAppender.lastQueue.isEmpty());
  }

  @Test(timeout = 1000)
  public void testAppendSingleEvent() throws Exception {
    instrumentedAppender.setRemoteHost("localhost");
    instrumentedAppender.start();

    instrumentedAppender.latch.await();
    instrumentedAppender.append("some event");
    assertTrue(instrumentedAppender.lastQueue.size() == 1);
  }

  @Test
  public void testAppendEvent() throws Exception {
    instrumentedAppender.setRemoteHost("localhost");
    instrumentedAppender.setQueueSize(1);
    instrumentedAppender.start();

    // stop the appender task, but don't stop the appender
    executorService.shutdownNow();
    assertTrue(executorService.awaitTermination(DELAY, TimeUnit.MILLISECONDS));

    instrumentedAppender.append("some event");
    assertEquals("some event", instrumentedAppender.lastQueue.poll());
  }

  @Test
  public void testDispatchEvent() throws Exception {
    ServerSocket serverSocket = ServerSocketUtil.createServerSocket();
    instrumentedAppender.setRemoteHost(serverSocket.getInetAddress().getHostAddress());
    instrumentedAppender.setPort(serverSocket.getLocalPort());
    instrumentedAppender.setQueueSize(1);
    instrumentedAppender.start();

    Socket appenderSocket = serverSocket.accept();
    serverSocket.close();

    instrumentedAppender.append("some event");

    final int shortDelay = 100;
    for (int i = 0, retries = DELAY / shortDelay;
         !instrumentedAppender.lastQueue.isEmpty() && i < retries;
         i++) {
      Thread.sleep(shortDelay);
    }
    assertTrue(instrumentedAppender.lastQueue.isEmpty());

    ObjectInputStream ois = new ObjectInputStream(appenderSocket.getInputStream());
    assertEquals("some event", ois.readObject());
    appenderSocket.close();

  }

  private static class InstrumentedSocketAppender extends AbstractSocketAppender<String> {

    private BlockingQueue<String> lastQueue;
    CountDownLatch latch = new  CountDownLatch(1);
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
    protected void signalEntryInRunMethod() {
        latch.countDown();
    }

    @Override
    BlockingQueue<String> newBlockingQueue(int queueSize) {
      lastQueue = super.newBlockingQueue(queueSize);
      return lastQueue;
    }

  }

}
