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
package ch.qos.logback.core;

import ch.qos.logback.core.helpers.NOPAppender;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.testUtil.DelayingListAppender;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.NPEAppender;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Ceki G&uuml;lc&uuml;
 * @author Torsten Juergeleit
 */
public class AsyncAppenderBaseTest {


  Context context = new ContextBase();
  AsyncAppenderBase<Integer> asyncAppenderBase = new AsyncAppenderBase<Integer>();
  LossyAsyncAppender lossyAsyncAppender = new LossyAsyncAppender();
  DelayingListAppender<Integer> delayingListAppender = new DelayingListAppender<Integer>();
  ListAppender<Integer> listAppender = new ListAppender<Integer>();
  OnConsoleStatusListener onConsoleStatusListener = new OnConsoleStatusListener();
  StatusChecker statusChecker = new StatusChecker(context);

  @Before
  public void setUp() {
    onConsoleStatusListener.setContext(context);
    context.getStatusManager().add(onConsoleStatusListener);
    onConsoleStatusListener.start();

    asyncAppenderBase.setContext(context);
    lossyAsyncAppender.setContext(context);

    listAppender.setContext(context);
    listAppender.setName("list");
    listAppender.start();

    delayingListAppender.setContext(context);
    delayingListAppender.setName("list");
    delayingListAppender.start();
  }

  @Test(timeout = 2000)
  public void smoke() {
    asyncAppenderBase.addAppender(listAppender);
    asyncAppenderBase.start();
    asyncAppenderBase.doAppend(0);
    asyncAppenderBase.stop();
    verify(listAppender, 1);
  }

  @Test
  public void exceptionsShouldNotCauseHalting() throws InterruptedException {
    NPEAppender npeAppender = new NPEAppender<Integer>();
    npeAppender.setName("bad");
    npeAppender.setContext(context);
    npeAppender.start();

    asyncAppenderBase.addAppender(npeAppender);
    asyncAppenderBase.start();
    assertTrue(asyncAppenderBase.isStarted());
    for (int i = 0; i < 10; i++)
      asyncAppenderBase.append(i);

    asyncAppenderBase.stop();
    assertFalse(asyncAppenderBase.isStarted());
    assertEquals(AppenderBase.ALLOWED_REPEATS, statusChecker.matchCount("Appender \\[bad\\] failed to append."));
  }

  @Test(timeout = 2000)
  public void emptyQueueShouldBeStoppable() {
    asyncAppenderBase.addAppender(listAppender);
    asyncAppenderBase.start();
    asyncAppenderBase.stop();
    verify(listAppender, 0);
  }

  @Test(timeout = 2000)
  public void workerShouldStopEvenIfInterruptExceptionConsumedWithinSubappender() {
    delayingListAppender.delay = 100;
    asyncAppenderBase.addAppender(delayingListAppender);
    asyncAppenderBase.start();
    asyncAppenderBase.doAppend(0);
    asyncAppenderBase.stop();
    verify(delayingListAppender, 1);
    assertTrue(delayingListAppender.interrupted);
  }

  @Test(timeout = 2000)
  public void noEventLoss() {
    int bufferSize = 10;
    int loopLen = bufferSize * 2;
    asyncAppenderBase.addAppender(delayingListAppender);
    asyncAppenderBase.setQueueSize(bufferSize);
    asyncAppenderBase.start();
    for (int i = 0; i < loopLen; i++) {
      asyncAppenderBase.doAppend(i);
    }
    asyncAppenderBase.stop();
    verify(delayingListAppender, loopLen);
  }

  @Test(timeout = 2000)
  public void lossyAppenderShouldOnlyLooseCertainEvents() {
    int bufferSize = 5;
    int loopLen = bufferSize * 2;
    lossyAsyncAppender.addAppender(delayingListAppender);
    lossyAsyncAppender.setQueueSize(bufferSize);
    lossyAsyncAppender.setDiscardingThreshold(1);
    lossyAsyncAppender.start();
    for (int i = 0; i < loopLen; i++) {
      lossyAsyncAppender.doAppend(i);
    }
    lossyAsyncAppender.stop();
    verify(delayingListAppender, loopLen - 2);
  }

  @Test(timeout = 2000)
  public void lossyAppenderShouldBeNonLossyIfDiscardingThresholdIsZero() {
    int bufferSize = 5;
    int loopLen = bufferSize * 2;
    lossyAsyncAppender.addAppender(delayingListAppender);
    lossyAsyncAppender.setQueueSize(bufferSize);
    lossyAsyncAppender.setDiscardingThreshold(0);
    lossyAsyncAppender.start();
    for (int i = 0; i < loopLen; i++) {
      lossyAsyncAppender.doAppend(i);
    }
    lossyAsyncAppender.stop();
    verify(delayingListAppender, loopLen);
  }

  @Test
  public void invalidQueueCapacityShouldResultInNonStartedAppender() {
    asyncAppenderBase.addAppender(new NOPAppender<Integer>());
    asyncAppenderBase.setQueueSize(0);
    assertEquals(0, asyncAppenderBase.getQueueSize());
    asyncAppenderBase.start();
    assertFalse(asyncAppenderBase.isStarted());
    statusChecker.assertContainsMatch("Invalid queue size");
  }
  
  @Test
  public void suspendedWorkerThreadDoesNotAppendEvents() {
    int bufferSize = 1000;
    int loopLen = 5;
    ListAppender la = listAppender;
    asyncAppenderBase.addAppender(la);
    asyncAppenderBase.setQueueSize(bufferSize);
    asyncAppenderBase.setDiscardingThreshold(0);
    asyncAppenderBase.start();
    asyncAppenderBase.worker.suspend();
  
    for (int i = 0; i < loopLen; i++) {
      asyncAppenderBase.doAppend(i);
    }
    assertEquals(loopLen, asyncAppenderBase.getNumberOfElementsInQueue());
    assertEquals(0, la.list.size());
  
    asyncAppenderBase.worker.resume();
    asyncAppenderBase.stop();
  }
  
  @Test
  public void hookThreadAppendsEvents() {
    int bufferSize = 1000;
    int loopLen = 5;
    ListAppender la = listAppender;
    asyncAppenderBase.addAppender(la);
    asyncAppenderBase.setQueueSize(bufferSize);
    asyncAppenderBase.setDiscardingThreshold(0);
    asyncAppenderBase.start();
    asyncAppenderBase.worker.suspend();

    for (int i = 0; i < loopLen; i++) {
      asyncAppenderBase.doAppend(i);
    }
    assertEquals(loopLen, asyncAppenderBase.getNumberOfElementsInQueue());
    assertEquals(0, la.list.size());
  
    asyncAppenderBase.hook.run();
    assertEquals(0, asyncAppenderBase.getNumberOfElementsInQueue());
    assertEquals(loopLen, la.list.size());
    statusChecker.assertContainsMatch("Shutdown hook will flush remaining events before exiting");
      
    asyncAppenderBase.worker.resume();
    asyncAppenderBase.stop();
  }
  
  @Test
  public void hookThreadExitsWhenIdleTimeoutReached() {
    int bufferSize = 1000;
    int idleTimeout = 500;
    ListAppender la = listAppender;
    asyncAppenderBase.addAppender(la);
    asyncAppenderBase.setQueueSize(bufferSize);
    asyncAppenderBase.setDiscardingThreshold(0);
    asyncAppenderBase.setShutdownIdleDelay(idleTimeout);
    asyncAppenderBase.start();
    asyncAppenderBase.worker.suspend();
    
    asyncAppenderBase.hook.run();
      assertEquals(0, asyncAppenderBase.getNumberOfElementsInQueue());
    assertEquals(0, la.list.size());
    statusChecker.assertContainsMatch("Shutdown hook will flush remaining events before exiting");
    statusChecker.assertContainsMatch("Async queue idle for " + idleTimeout + " ms.");
      
    asyncAppenderBase.worker.resume();
    asyncAppenderBase.stop();
  }
  
  @Test
  public void hookThreadExitsWhenMaxRuntimeReached() {
    int bufferSize = 1000;
    int loopLen = 5;
    int idleTimeout = delayingListAppender.delay*2;
    int maxRuntime = delayingListAppender.delay/2;
    ListAppender la = delayingListAppender;
    asyncAppenderBase.addAppender(la);
    asyncAppenderBase.setQueueSize(bufferSize);
    asyncAppenderBase.setDiscardingThreshold(0);
    asyncAppenderBase.setShutdownIdleDelay(idleTimeout);
    asyncAppenderBase.setMaxHookRuntime(maxRuntime);
    asyncAppenderBase.start();
    asyncAppenderBase.worker.suspend();
    
    for (int i = 0; i < loopLen; i++) {
      asyncAppenderBase.doAppend(i);
    }
    assertEquals(loopLen, asyncAppenderBase.getNumberOfElementsInQueue());
    assertEquals(0, la.list.size());
    
    asyncAppenderBase.hook.run();
    assertEquals(loopLen - 1, asyncAppenderBase.getNumberOfElementsInQueue());
    assertEquals(1, la.list.size());
    statusChecker.assertContainsMatch("Shutdown hook will flush remaining events before exiting");
    statusChecker.assertContainsMatch("Max hook runtime \\(" + maxRuntime + " ms\\) exceeded. " + asyncAppenderBase.getNumberOfElementsInQueue() + " queued events will be discarded.");
        
    asyncAppenderBase.worker.resume();
    asyncAppenderBase.stop();
  }
  
  @Test
  public void hookThreadInstalledDuringStart() {
    ListAppender la = delayingListAppender;
    asyncAppenderBase.addAppender(la);
    asyncAppenderBase.start();
    
    assertTrue(Runtime.getRuntime().removeShutdownHook(asyncAppenderBase.hook));
  }
  
  @Test
  public void hookThreadRemovedDuringStop() {
    ListAppender la = delayingListAppender;
    asyncAppenderBase.addAppender(la);
    asyncAppenderBase.start();
    asyncAppenderBase.stop();
    assertFalse(Runtime.getRuntime().removeShutdownHook(asyncAppenderBase.hook));
  }

  private void verify(ListAppender la, int expectedSize) {
    assertFalse(la.isStarted());
    assertEquals(expectedSize, la.list.size());
    statusChecker.assertIsErrorFree();
    statusChecker.assertContainsMatch("Worker thread will flush remaining events before exiting.");
  }

  static class LossyAsyncAppender extends AsyncAppenderBase<Integer> {
    @Override
    protected boolean isDiscardable(Integer i) {
      return (i % 3 == 0);
    }
  }
}
