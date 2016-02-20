/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.net.mock.MockContext;
import ch.qos.logback.core.spi.PreSerializationTransformer;
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
 * Unit tests for {@link AbstractServerSocketAppender}.
 *
 * @author Carl Harris
 * @author Sebastian Gr&ouml;bler
 */
public class AbstractServerSocketAppenderTest {


  private MockContext context = new MockContext();
  
  private MockServerRunner<RemoteReceiverClient> runner = 
      new MockServerRunner<RemoteReceiverClient>();
  
  private MockServerListener<RemoteReceiverClient> listener = 
      new MockServerListener<RemoteReceiverClient>();
  
  private ServerSocket serverSocket;
  private InstrumentedServerSocketAppenderBase appender;
  
  @Before
  public void setUp() throws Exception {
    serverSocket = ServerSocketUtil.createServerSocket();
    appender = new InstrumentedServerSocketAppenderBase(serverSocket, listener, runner);
    appender.setContext(context);
  }
  
  @After
  public void tearDown() throws Exception {
    serverSocket.close();
  }
  
  @Test
  public void testStartStop() throws Exception {
    appender.start();
    assertTrue(runner.isContextInjected());
    assertTrue(runner.isRunning());
    assertSame(listener, appender.getLastListener());
    
    appender.stop();
    assertFalse(runner.isRunning());
  }

  @Test
  public void testStartWhenAlreadyStarted() throws Exception {
    appender.start();
    appender.start();
    assertEquals(1, runner.getStartCount());
  }

  @Test
  public void testStopThrowsException() throws Exception {
    appender.start();
    assertTrue(appender.isStarted());
    IOException ex = new IOException("test exception");
    runner.setStopException(ex);
    appender.stop();
    
    Status status = context.getLastStatus();
    assertNotNull(status);    
    assertTrue(status instanceof ErrorStatus);
    assertTrue(status.getMessage().contains(ex.getMessage()));
    assertSame(ex, status.getThrowable());
  }

  @Test
  public void testStopWhenNotStarted() throws Exception {
    appender.stop();
    assertEquals(0, runner.getStartCount());
  }

  @Test
  public void providesDefaultValueForMaxPoolSize() throws Exception {
    assertThat(appender.getMaxPoolSize(), is(CoreConstants.MAX_POOL_SIZE));
  }

  @Test
  public void providesDefaultValueForCorePoolSize() throws Exception {
    assertThat(appender.getCorePoolSize(), is(CoreConstants.CORE_POOL_SIZE));
  }

  @Test
  public void allowsCustomValueForMaxPoolSize() throws Exception {
    final int customValue = 128;
    appender.setMaxPoolSize(customValue);

    assertThat(appender.getMaxPoolSize(), is(customValue));
  }

  @Test
  public void allowsCustomValueForCorePoolSize() throws Exception {
    final int customValue = 128;
    appender.setCorePoolSize(128);

    assertThat(appender.getCorePoolSize(), is(customValue));
  }

  @Test
  public void stopShutsDownConnectionPoolExecutorServiceWhenPresent() {
    final ExecutorService executorService = mock(ExecutorService.class);

    appender.start();
    appender.connectionPoolExecutorService = executorService;
    appender.stop();

    verify(executorService).shutdownNow();
    assertThat(appender.connectionPoolExecutorService, is(nullValue()));
  }

  @Test
  public void testStartUsesConnectionPoolExecutorService() {

    // given
    final AbstractServerSocketAppender serverSocketAppender = spy(new MockServerSocketAppender());
    final int corePoolSize = 21;
    final int maxPoolSize = 42;
    serverSocketAppender.setCorePoolSize(corePoolSize);
    serverSocketAppender.setMaxPoolSize(maxPoolSize);

    // when
    serverSocketAppender.start();

    // then
    final ArgumentCaptor<Executor> captor = ArgumentCaptor.forClass(Executor.class);

    verify(serverSocketAppender).createServerRunner(any(ServerListener.class), captor.capture());

    final Executor executor = captor.getValue();

    assertThat(executor, instanceOf(ExecutorService.class));

    final ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;

    assertThat(threadPoolExecutor.getCorePoolSize(), is(corePoolSize));
    assertThat(threadPoolExecutor.getMaximumPoolSize(), is(maxPoolSize));
  }

  /**
   * Simple NOP implementation of abstract methods of {@link AbstractServerSocketAppender}.
   */
  private static class MockServerSocketAppender extends AbstractServerSocketAppender<Object> {

    @Override
    protected void postProcessEvent(Object event) {
      // NOP
    }

    @Override
    protected PreSerializationTransformer<Object> getPST() {
      final PreSerializationTransformer<Object> pst = mock(PreSerializationTransformer.class);
      return pst;
    }
  }
}
