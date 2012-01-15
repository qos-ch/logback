/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.appender.AbstractAppenderTest;
import ch.qos.logback.core.helpers.NOPAppender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;

public class AsyncAppenderTest extends AbstractAppenderTest<Object> {

  protected Appender<Object> getAppender() {
    return new AsyncAppender<Object>();
  }

  protected Appender<Object> getConfiguredAppender() {
    AsyncAppender<Object> aa = new AsyncAppender<Object>();
    aa.setContext(new ContextBase());
    aa.addAppender(new NOPAppender<Object>());
    aa.start();
    return aa;
  }

  @Test
  public void testMissingAppender() {
    LoggerContext context = new LoggerContext();
    AsyncAppender<Object> aa = new AsyncAppender<Object>();
    aa.setContext(context);
    aa.start();
    assertFalse(aa.isStarted());

    // Check if the status manager has the exception logged
    StatusManager manager = context.getStatusManager();
    assertEquals(Status.ERROR, manager.getLevel());
    assertEquals(1, manager.getCount());
  }

  @Test
  public void testBadAppender() {
    LoggerContext context = new LoggerContext();

    // Start AsyncAppender with embedded NPE appender
    AsyncAppender<Object> aa = new AsyncAppender<Object>();
    aa.setContext(context);
    aa.addAppender(new NPEAppender<Object>());
    aa.start();
    assertTrue(aa.isStarted());

    // Do some logging
    ILoggingEvent le = createLoggingEvent(context);
    aa.append(le);
    aa.append(le);
    aa.append(le);
    aa.append(le);
    aa.append(le);

    // Wait for dispatcher to catch up
    sleep(100);
    assertEquals(0, aa.getQueueSize());

    // Check if the status manager has the exceptions logged
    StatusManager manager = context.getStatusManager();
    assertEquals(Status.ERROR, manager.getLevel());
    assertEquals(3, manager.getCount());

    // Stop AsyncAppender
    aa.stop();
    assertFalse(aa.isStarted());
  }

  @Test
  public void testInvalidQueueCapacity() {
    LoggerContext context = new LoggerContext();

    // Start AsyncAppender with queue capacity of zero
    AsyncAppender<Object> aa = new AsyncAppender<Object>();
    aa.setContext(context);
    aa.addAppender(new NOPAppender<Object>());
    aa.setQueueCapacity(0);
    assertEquals(-1, aa.getQueueSize());
    aa.start();
    assertFalse(aa.isStarted());

    // Check if the status manager has the exception logged
    StatusManager manager = context.getStatusManager();
    assertEquals(Status.ERROR, manager.getLevel());
    assertEquals(1, manager.getCount());
  }

  @Test
  public void testBasic() {
    LoggerContext context = new LoggerContext();

    // Start AsyncAppender with embedded list appender
    ListAppender<ILoggingEvent> la = new ListAppender<ILoggingEvent>();
    AsyncAppender<ILoggingEvent> aa = new AsyncAppender<ILoggingEvent>();
    aa.setContext(context);
    aa.addAppender(la);
    aa.start();
    assertTrue(aa.isStarted());

    // Check embedded list appender
    assertTrue(la.isStarted());
    assertEquals(0, la.list.size());

    // Do some logging and give the dispatcher thread a chance to finish its
    // work
    aa.append(createLoggingEvent(context));
    sleep(100);

    // Stop AsyncAppender
    aa.stop();
    assertFalse(aa.isStarted());

    // Check embedded list appender
    assertFalse(la.isStarted());
    assertEquals(1, la.list.size());
  }

  @Test
  public void testLogEventsPreparedForDeferredProcessing() {
    LoggerContext context = new LoggerContext();

    // Start AsyncAppender with embedded list appender
    ListAppender<ILoggingEvent> la = new ListAppender<ILoggingEvent>();
    AsyncAppender<ILoggingEvent> aa = new AsyncAppender<ILoggingEvent>();
    aa.setContext(context);
    aa.addAppender(la);
    aa.start();

    // Do some logging and give the dispatcher thread a chance to finish its
    // work
    aa.append(createLoggingEvent(context));
    sleep(100);

    // Check the event
    assertEquals(1, la.list.size());
    ILoggingEvent e = la.list.get(0);
    assertEquals(Thread.currentThread().getName(), e.getThreadName());
    assertFalse(e.hasCallerData());

    // Stop AsyncAppender
    aa.stop();
    assertFalse(aa.isStarted());
  }

  @Test
  public void testLogEventsWithCallerData() {
    LoggerContext context = new LoggerContext();

    // Start AsyncAppender with embedded list appender
    ListAppender<ILoggingEvent> la = new ListAppender<ILoggingEvent>();
    AsyncAppender<ILoggingEvent> aa = new AsyncAppender<ILoggingEvent>();
    aa.setContext(new ContextBase());
    aa.addAppender(la);
    aa.setIncludeCallerData(true);
    aa.start();

    // Do some logging and give the dispatcher thread a chance to finish its
    // work
    aa.append(createLoggingEvent(context));
    sleep(100);

    // Check the events caller data
    assertEquals(1, la.list.size());
    ILoggingEvent e = la.list.get(0);
    assertTrue(e.hasCallerData());

    // Stop AsyncAppender
    aa.stop();
    assertFalse(aa.isStarted());
  }

  @Test
  public void testStartStopDispatcherThread() {

    // Start AsyncAppender and wait for dispatcher to start up
    AsyncAppender<Object> aa = new AsyncAppender<Object>();
    aa.setContext(new ContextBase());
    aa.setName("testStartStopDispatcherThread");
    aa.addAppender(new NOPAppender<Object>());
    aa.start();
    assertTrue(aa.isStarted());
    sleep(100);
    assertTrue(isDispatcherThreadAlive(aa.getName()));

    // Stop AsyncAppender and check if dispatcher is stopped as well
    aa.stop();
    assertFalse(aa.isStarted());
    assertFalse(isDispatcherThreadAlive(aa.getName()));
  }

  @Test
  public void testBlockedCaller() {
    final LoggerContext context = new LoggerContext();

    // Prepare latches used for thread synchronization
    final CountDownLatch appenderCalledSignal = new CountDownLatch(1);
    final CountDownLatch unblockAppenderSignal = new CountDownLatch(1);
    final CountDownLatch loggedEventsSentSignal = new CountDownLatch(3);
    final CountDownLatch logEventsReceivedSignal = new CountDownLatch(3);

    // Start AsyncAppender with embedded blocking appender
    final AsyncAppender<Object> aa = new AsyncAppender<Object>();
    aa.addAppender(new AppenderBase<Object>() {
      protected void append(Object event) {
        logEventsReceivedSignal.countDown();
        appenderCalledSignal.countDown();
        try {
          unblockAppenderSignal.await();
        } catch (InterruptedException ignored) {}
      }
    });
    aa.setContext(new ContextBase());
    aa.setQueueCapacity(1);
    aa.start();

    // Start caller thread which appends 3 log events to the AsyncAppender
    Thread t = new Thread(new Runnable() {
      public void run() {
        ILoggingEvent le = createLoggingEvent(context);
        aa.append(le); // immediately consumed by the blocking appender
        loggedEventsSentSignal.countDown();
        aa.append(le); // fills up the event queue
        loggedEventsSentSignal.countDown();
        aa.append(le);
        loggedEventsSentSignal.countDown();
      }
    });
    t.start();

    // Wait until the blocking appender is called the first time
    try {
      appenderCalledSignal.await(100, TimeUnit.MILLISECONDS);
    } catch (InterruptedException ignored) {}
    assertEquals("Embedded blocking appender was not called", 2,
        logEventsReceivedSignal.getCount());

    // Unblock the blocking appender and check the logger thread
    assertEquals("Logger thread was not blocked", 1, aa.getQueueSize());
    unblockAppenderSignal.countDown();
    try {
      logEventsReceivedSignal.await(100, TimeUnit.MILLISECONDS);
    } catch (InterruptedException ignored) {
    }
    assertEquals("Logger thread still blocked", 0, loggedEventsSentSignal
        .getCount());

    // Stop AsyncAppender
    aa.stop();
    assertFalse(aa.isStarted());
  }

  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ignored) {
    }
  }

  private ILoggingEvent createLoggingEvent(LoggerContext context) {
    Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
    ILoggingEvent le = new LoggingEvent("Test", logger, Level.INFO,
        "Hello World", null, null);
    return le;
  }

  private boolean isDispatcherThreadAlive(String appenderName) {
    String threadNamePrefix = AsyncAppender.DISPATCHER_THREAD_NAME_PREFIX
        + " [" + appenderName + "] - ";
    ThreadGroup tg = Thread.currentThread().getThreadGroup();
    Thread[] threads = new Thread[tg.activeCount()];
    tg.enumerate(threads);
    for (Thread thread : threads) {
      if (thread.getName().startsWith(threadNamePrefix)) {
        return true;
      }
    }
    return false;

  }

  private static class NPEAppender<E> implements Appender<E> {

    public void doAppend(E event) throws LogbackException {
      throw new NullPointerException();
    }

    public Layout<E> getLayout() {
      return null;
    }

    public String getName() {
      return "NPE Appender";
    }

    public void setLayout(Layout<E> layout) {
    }

    public void setName(String name) {
    }

    public boolean isStarted() {
      return true;
    }

    public void start() {
    }

    public void stop() {
    }

    public void addError(String msg) {
    }

    public void addError(String msg, Throwable ex) {
    }

    public void addInfo(String msg) {
    }

    public void addInfo(String msg, Throwable ex) {
    }

    public void addStatus(Status status) {
    }

    public void addWarn(String msg) {
    }

    public void addWarn(String msg, Throwable ex) {
    }

    public Context getContext() {
      return null;
    }

    public void setContext(Context context) {
    }

    public void addFilter(Filter<E> newFilter) {
    }

    public void clearAllFilters() {
    }

    public List<Filter<E>> getCopyOfAttachedFiltersList() {
      return null;
    }

    public FilterReply getFilterChainDecision(E event) {
      return null;
    }

    @SuppressWarnings("unchecked")
    public Filter getFirstFilter() {
      return null;
    }
  }
}
