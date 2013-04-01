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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;


public class ConcurrentServerRunnerTest {

  private MockServerListener listener = new MockServerListener();
  private ExecutorService executor = Executors.newCachedThreadPool();
  private ConcurrentServerRunner runner = new ConcurrentServerRunner(listener, executor);

  @Before
  public void setUp() throws Exception {
    runner.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
  }

  @Test
  public void testStartStop() throws Exception {
    assertFalse(runner.isStarted());
    runner.start();
    assertTrue(runner.isStarted());
    int retries = 200;
    synchronized (listener) {
      while (retries-- > 0 && listener.getWaiter() == null) {
        listener.wait(10);
      }
    }
    assertNotNull(listener.getWaiter());
    runner.stop();
    assertTrue(listener.isClosed());
    assertFalse(runner.isStarted());
    executor.shutdown();
    assertTrue(executor.awaitTermination(2000, TimeUnit.MILLISECONDS));
  }
  
  @Test
  public void testRunOneClient() throws Exception {
    runner.start();
    MockClient client = new MockClient();
    listener.addClient(client);
    int retries = 200;
    synchronized (client) {
      while (retries-- > 0 && !client.isRunning()) {
        client.wait(10);
      }
    }
    assertTrue(client.isRunning());
    client.close();
    runner.stop();
    executor.shutdown();
    assertTrue(executor.awaitTermination(2000, TimeUnit.MILLISECONDS));
  }

  @Test
  public void testRunManyClients() throws Exception {
    runner.start();
    int count = 10;
    while (count-- > 0) {
      MockClient client = new MockClient();
      listener.addClient(client);
      int retries = 200;
      synchronized (client) {
        while (retries-- > 0 && !client.isRunning()) {
          client.wait(10);
        }
      }
      assertTrue(client.isRunning());
    }
    runner.stop();
    executor.shutdown();
    assertTrue(executor.awaitTermination(2000, TimeUnit.MILLISECONDS));
  }

}
