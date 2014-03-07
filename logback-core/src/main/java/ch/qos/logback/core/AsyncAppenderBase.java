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

import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This appender and derived classes, log events asynchronously.  In order to avoid loss of logging events, this
 * appender should be closed. It is the user's  responsibility to close appenders, typically at the end of the
 * application lifecycle.
 * <p/>
 * This appender buffers events in a {@link BlockingQueue}. {@link Worker} thread created by this appender takes
 * events from the head of the queue, and dispatches them to the single appender attached to this appender.
 * <p/>
 * <p>Please refer to the <a href="http://logback.qos.ch/manual/appenders.html#AsyncAppender">logback manual</a> for
 * further information about this appender.</p>
 *
 * @param <E>
 * @author Ceki G&uuml;lc&uuml;
 * @author Torsten Juergeleit
 * @since 1.0.4
 */
public class AsyncAppenderBase<E> extends UnsynchronizedAppenderBase<E> implements AppenderAttachable<E> {

  AppenderAttachableImpl<E> aai = new AppenderAttachableImpl<E>();
  BlockingQueue<E> blockingQueue;

  /**
   * The default buffer size.
   */
  public static final int DEFAULT_QUEUE_SIZE = 256;
  int queueSize = DEFAULT_QUEUE_SIZE;

  int appenderCount = 0;

  static final int UNDEFINED = -1;
  int discardingThreshold = UNDEFINED;

  Worker worker = new Worker();

 /**
  * Shutdown hook to process any events that are left in the queue when the JVM shuts down or that are added
  * by other shutdown hooks.
  */
  Hook hook = new Hook();
	
  /**
   * The default queue idle time that must occur before
   */
  public static final int DEFAULT_SHUTDOWN_IDLE_DELAY = 1000;
  int shutdownIdleDelay = DEFAULT_SHUTDOWN_IDLE_DELAY;
  
  /**
   * The default maximum runtime allowed for the hook. If the hook runs for longer
   * than this time it will exit, discarding any items in the queue
   */
  public static final int DEFAULT_HOOK_MAX_RUNTIME = 2000;
  int maxHookRuntime = DEFAULT_HOOK_MAX_RUNTIME;
  
  /**
   * Is the eventObject passed as parameter discardable? The base class's implementation of this method always returns
   * 'false' but sub-classes may (and do) override this method.
   * <p/>
   * <p>Note that only if the buffer is nearly full are events discarded. Otherwise, when the buffer is "not full"
   * all events are logged.
   *
   * @param eventObject
   * @return - true if the event can be discarded, false otherwise
   */
  protected boolean isDiscardable(E eventObject) {
    return false;
  }


  /**
   * Pre-process the event prior to queueing. The base class does no pre-processing but sub-classes can
   * override this behavior.
   *
   * @param eventObject
   */
  protected void preprocess(E eventObject) {
  }


  @Override
  public void start() {
    if (appenderCount == 0) {
      addError("No attached appenders found.");
      return;
    }
    if (queueSize < 1) {
      addError("Invalid queue size [" + queueSize + "]");
      return;
    }
    blockingQueue = new ArrayBlockingQueue<E>(queueSize);

    if (discardingThreshold == UNDEFINED)
      discardingThreshold = queueSize / 5;
    addInfo("Setting discardingThreshold to " + discardingThreshold);
    worker.setDaemon(true);
    worker.setName("AsyncAppender-Worker-" + getName());
    // make sure this instance is marked as "started" before staring the worker Thread
    super.start();
    worker.start();

    hook.setName("AsyncAppender-Hook-" + getName());
    Runtime.getRuntime().addShutdownHook(hook);
  }

  @Override
  public void stop() {
    if (!isStarted())
      return;

    // mark this appender as stopped so that Worker can also processPriorToRemoval if it is invoking aii.appendLoopOnAppenders
    // and sub-appenders consume the interruption
    super.stop();

    // interrupt the worker thread so that it can terminate. Note that the interruption can be consumed
    // by sub-appenders
    worker.interrupt();
    try {
      worker.join(1000);
    } catch (InterruptedException e) {
      addError("Failed to join worker thread", e);
    }

    Runtime.getRuntime().removeShutdownHook(hook);
  }


  @Override
  protected void append(E eventObject) {
    if (isQueueBelowDiscardingThreshold() && isDiscardable(eventObject)) {
      return;
    }
    preprocess(eventObject);
    put(eventObject);
  }

  private boolean isQueueBelowDiscardingThreshold() {
    return (blockingQueue.remainingCapacity() < discardingThreshold);
  }

  private void put(E eventObject) {
    try {
      blockingQueue.put(eventObject);
    } catch (InterruptedException e) {
    }
  }

  public int getQueueSize() {
    return queueSize;
  }

  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }

  public int getDiscardingThreshold() {
    return discardingThreshold;
  }

  public void setDiscardingThreshold(int discardingThreshold) {
    this.discardingThreshold = discardingThreshold;
  }
  
  public int getShutdownIdleDelay() {
	return shutdownIdleDelay;
  }
  
  public void setShutdownIdleDelay(int shutdownIdleDelay) {
	this.shutdownIdleDelay = shutdownIdleDelay;
  }
 
  public int getMaxHookRuntime() {
	return maxHookRuntime;
  }
  
  public void setMaxHookRuntime(int maxHookRuntime) {
	this.maxHookRuntime = maxHookRuntime;
  }

  /**
   * Returns the number of elements currently in the blocking queue.
   *
   * @return number of elements currently in the queue.
   */
  public int getNumberOfElementsInQueue() {
    return blockingQueue.size();
  }

  /**
   * The remaining capacity available in the blocking queue.
   *
   * @return the remaining capacity
   * @see {@link java.util.concurrent.BlockingQueue#remainingCapacity()}
   */
  public int getRemainingCapacity() {
    return blockingQueue.remainingCapacity();
  }



  public void addAppender(Appender<E> newAppender) {
    if (appenderCount == 0) {
      appenderCount++;
      addInfo("Attaching appender named ["+newAppender.getName()+"] to AsyncAppender.");
      aai.addAppender(newAppender);
    } else {
      addWarn("One and only one appender may be attached to AsyncAppender.");
      addWarn("Ignoring additional appender named [" + newAppender.getName() + "]");
    }
  }

  public Iterator<Appender<E>> iteratorForAppenders() {
    return aai.iteratorForAppenders();
  }

  public Appender<E> getAppender(String name) {
    return aai.getAppender(name);
  }

  public boolean isAttached(Appender<E> eAppender) {
    return aai.isAttached(eAppender);
  }

  public void detachAndStopAllAppenders() {
    aai.detachAndStopAllAppenders();
  }

  public boolean detachAppender(Appender<E> eAppender) {
    return aai.detachAppender(eAppender);
  }

  public boolean detachAppender(String name) {
    return aai.detachAppender(name);
  }

  class Worker extends Thread {

    public void run() {
      AsyncAppenderBase<E> parent = AsyncAppenderBase.this;
      AppenderAttachableImpl<E> aai = parent.aai;

      // loop while the parent is started
      while (parent.isStarted()) {
        try {
          E e = parent.blockingQueue.take();
          aai.appendLoopOnAppenders(e);
        } catch (InterruptedException ie) {
          break;
        }
      }

      addInfo("Worker thread will flush remaining events before exiting. ");
      for (E e : parent.blockingQueue) {
        aai.appendLoopOnAppenders(e);
      }

      aai.detachAndStopAllAppenders();
    }
  }
  
  class Hook extends Thread {
    @Override
    public void run() {
      AsyncAppenderBase<E> parent = AsyncAppenderBase.this;
      AppenderAttachableImpl<E> aai = parent.aai;
        
      int count = 0;
        
      addInfo("Shutdown hook will flush remaining events before exiting");
      long start = System.currentTimeMillis();
      long delay = 0;
        
      while(true) {
        try {
          E event = parent.blockingQueue.poll(parent.shutdownIdleDelay, TimeUnit.MILLISECONDS);
      
          if(event != null) {
            count++;
            aai.appendLoopOnAppenders(event);  
            
            delay = System.currentTimeMillis() - start;
          } else {
            addInfo("Async queue idle for " + parent.shutdownIdleDelay + " ms.");
            break;
          }
        } catch (InterruptedException e) {
          addError("Interrupted while polling for log event from the asynchronous queue", e);
          //Since this is running in a shutdown hook, it is safe to swallow this exception and not
          //reset the interrupted status on the thread (generally speaking, bad practice)
        }
        
        if(delay >= parent.maxHookRuntime) {
          addWarn("Max hook runtime (" + parent.maxHookRuntime + " ms) exceeded. " + parent.blockingQueue.size() + " queued events will be discarded.");
          break;
        }
      }
        
      long end = System.currentTimeMillis();
      addInfo("Processed " + count + " log entries in " + (end - start) + " ms. Exiting");
      
      aai.detachAndStopAllAppenders();
    }
  }
}
