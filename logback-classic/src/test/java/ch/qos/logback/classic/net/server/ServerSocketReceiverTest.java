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
package ch.qos.logback.classic.net.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.net.mock.MockContext;
import ch.qos.logback.core.net.server.MockServerListener;
import ch.qos.logback.core.net.server.MockServerRunner;
import ch.qos.logback.core.net.server.ServerListener;
import ch.qos.logback.core.net.server.ServerSocketUtil;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.Status;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.nullValue;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ServerSocketReceiver}.
 *
 * @author Carl Harris
 * @author Sebastian Gr&ouml;bler
 */
public class ServerSocketReceiverTest {

  private MockContext context = new MockContext();
  
  private MockServerRunner<RemoteAppenderClient> runner = 
      new MockServerRunner<RemoteAppenderClient>();
  
  private MockServerListener<RemoteAppenderClient> listener = 
      new MockServerListener<RemoteAppenderClient>();
  
  private ServerSocket serverSocket;
  private InstrumentedServerSocketReceiver receiver;
  
  @Before
  public void setUp() throws Exception {
    serverSocket = ServerSocketUtil.createServerSocket();
    receiver = new InstrumentedServerSocketReceiver(serverSocket, listener, runner);
    receiver.setContext(context);
  }
  
  @After
  public void tearDown() throws Exception {
    serverSocket.close();
  }
  
  @Test
  public void testStartStop() throws Exception {
    receiver.start();
    assertTrue(runner.isContextInjected());
    assertTrue(runner.isRunning());
    assertSame(listener, receiver.getLastListener());
    
    receiver.stop();
    assertFalse(runner.isRunning());
  }

  @Test
  public void testStartWhenAlreadyStarted() throws Exception {
    receiver.start();
    receiver.start();
    assertEquals(1, runner.getStartCount());
  }

  @Test
  public void testStopThrowsException() throws Exception {
    receiver.start();
    assertTrue(receiver.isStarted());
    IOException ex = new IOException("test exception");
    runner.setStopException(ex);
    receiver.stop();
    
    Status status = context.getLastStatus();
    assertNotNull(status);    
    assertTrue(status instanceof ErrorStatus);
    assertTrue(status.getMessage().contains(ex.getMessage()));
    assertSame(ex, status.getThrowable());
  }

  @Test
  public void testStopWhenNotStarted() throws Exception {
    receiver.stop();
    assertEquals(0, runner.getStartCount());
  }

  @Test
  public void providesDefaultValueForMaxPoolSize() throws Exception {
    assertThat(receiver.getMaxPoolSize(), is(CoreConstants.MAX_POOL_SIZE));
  }

  @Test
  public void providesDefaultValueForCorePoolSize() throws Exception {
    assertThat(receiver.getCorePoolSize(), is(CoreConstants.CORE_POOL_SIZE));
  }

  @Test
  public void allowsCustomValueForMaxPoolSize() throws Exception {
    final int customValue = 128;
    receiver.setMaxPoolSize(customValue);

    assertThat(receiver.getMaxPoolSize(), is(customValue));
  }

  @Test
  public void allowsCustomValueForCorePoolSize() throws Exception {
    final int customValue = 128;
    receiver.setCorePoolSize(128);

    assertThat(receiver.getCorePoolSize(), is(customValue));
  }

  @Test
  public void stopShutsDownConnectionPoolExecutorServiceWhenPresent() {
    final ExecutorService executorService = mock(ExecutorService.class);

    receiver.start();
    receiver.connectionPoolExecutorService = executorService;
    receiver.stop();

    verify(executorService).shutdownNow();
    assertThat(receiver.connectionPoolExecutorService, is(nullValue()));
  }

  @Test
  public void testShouldStartUsesConnectionPoolExecutorService() {

    // given
    final ServerSocketReceiver serverSocketReceiver = spy(new ServerSocketReceiver());
    final int corePoolSize = 21;
    final int maxPoolSize = 42;
    serverSocketReceiver.setCorePoolSize(corePoolSize);
    serverSocketReceiver.setMaxPoolSize(maxPoolSize);

    // when
    serverSocketReceiver.shouldStart();

    // then
    final ArgumentCaptor<Executor> captor = ArgumentCaptor.forClass(Executor.class);

    verify(serverSocketReceiver).createServerRunner(any(ServerListener.class), captor.capture());

    final Executor executor = captor.getValue();

    assertThat(executor, instanceOf(ExecutorService.class));

    final ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;

    assertThat(threadPoolExecutor.getCorePoolSize(), is(corePoolSize));
    assertThat(threadPoolExecutor.getMaximumPoolSize(), is(maxPoolSize));
  }
}
