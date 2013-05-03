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

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;


public class ConcurrentServerRunnerTest {

  private static final int DELAY = 10000;
  private static final int SHORT_DELAY = 10;
  private static final int RETRIES = DELAY / SHORT_DELAY;

  private Context context = new ContextBase();
  private MockServerListener listener = new MockServerListener();

  private ExecutorService executor = Executors.newCachedThreadPool();
  private InstrumentedConcurrentServerRunner runner =
          new InstrumentedConcurrentServerRunner(listener, executor);

  @Before
  public void setUp() throws Exception {
    assertFalse(runner.isRunning());
    executor.execute(runner);
    runner.setContext(context);
  }

  @After
  public void tearDown() throws Exception {
    executor.shutdownNow();
    assertTrue(executor.awaitTermination(DELAY, TimeUnit.MILLISECONDS));
  }

  private void waitForClientToStart(WaitingClient client) throws InterruptedException {
    int retries = RETRIES;
    synchronized (client) {
      while (retries-- > 0 && !client.isRunning()) {
        client.awaitStartCondition(SHORT_DELAY);
      }
    }
  }

  private void waitForClientToStop(WaitingClient client) throws InterruptedException {
    int retries = RETRIES;
    synchronized (client) {
      while (retries-- > 0 && client.isRunning()) {
        client.awaitStopCondition(SHORT_DELAY);
      }
    }
  }

  @Test
  public void runnerShouldStartAndStopProperly() throws Exception {
    assertTrue(runner.awaitRunState(true, DELAY));
    int retries = DELAY / SHORT_DELAY;
    synchronized (listener) {
      while (retries-- > 0 && listener.getWaiter() == null) {
        listener.wait(SHORT_DELAY);
      }
    }
    assertNotNull(listener.getWaiter());
    runner.stop();
    assertTrue(listener.isClosed());
    assertFalse(runner.awaitRunState(false, DELAY));
  }

  @Test
  public void testLifeCycleOfOneClient() throws Exception {
    listener.addSocket(new Socket());
    assertTrue(runner.awaitRunState(true, DELAY));
    waitForRunnerClientListSizeToEqual(1);
    WaitingClient client = runner.clientList.get(0);
    waitForClientToStart(client);
    client.close();
    waitForClientToStop(client);
    runner.stop();
  }


  @Test
  public void testRunManyClients() throws Exception {
    int clientCount = 10;

    for (int i = 0; i < clientCount; i++) {
      listener.addSocket(new Socket());
    }

    waitForRunnerClientListSizeToEqual(clientCount);
    for (WaitingClient client : runner.clientList)
      waitForClientToStart(client);
    assertTrue(runner.awaitRunState(true, DELAY));
    runner.stop();
    for (WaitingClient client : runner.clientList)
      waitForClientToStop(client);
  }


  @Test
  public void testRunClientAndVisit() throws Exception {
    listener.addSocket(new Socket());
    // wait until all clients have been created
    waitForRunnerClientListSizeToEqual(1);
    WaitingClient client = runner.clientList.get(0);
    waitForClientToStart(client);

    assertTrue(runner.awaitRunState(true, DELAY));
    MockClientVisitor visitor = new MockClientVisitor();
    runner.accept(visitor);
    assertSame(client, visitor.getLastVisited());
    runner.stop();
    waitForClientToStop(client);
  }

  private void waitForRunnerClientListSizeToEqual(int count) throws InterruptedException {
    int retries = RETRIES;
    while (retries-- > 0 && runner.clientList.size() != count) {
      Thread.sleep(SHORT_DELAY);
    }
  }

  //  InstrumentedConcurrentServerRunner ================================================
  static class InstrumentedConcurrentServerRunner extends ConcurrentServerRunner<WaitingClient> {

    private final Lock lock = new ReentrantLock();
    private final Condition runningCondition = lock.newCondition();
    volatile List<WaitingClient> clientList = new ArrayList<WaitingClient>();

    public InstrumentedConcurrentServerRunner(ServerListener listener, Executor executor) {
      super(listener, executor);
    }

    @Override
    protected boolean configureClient(WaitingClient client) {
      return true;
    }

    @Override
    protected void setRunning(boolean running) {
      lock.lock();
      try {
        super.setRunning(running);
        runningCondition.signalAll();
      } finally {
        lock.unlock();
      }
    }

    @Override
    protected WaitingClient buildClient(String id, Socket socket) {
      WaitingClient client = new WaitingClient();
      clientList.add(client);
      return client;
    }

    public boolean awaitRunState(boolean state,
                                 long delay) throws InterruptedException {
      lock.lock();
      try {
        while (isRunning() != state) {
          runningCondition.await(delay, TimeUnit.MILLISECONDS);
        }
        return isRunning();
      } finally {
        lock.unlock();
      }
    }
  }

}
