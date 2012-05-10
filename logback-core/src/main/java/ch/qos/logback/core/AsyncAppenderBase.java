/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2012, QOS.ch. All rights reserved.
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

/**
 * AsyncAppender lets users log events asynchronously. It uses a
 * bounded buffer to store logging events. By default, if buffer is 80% full it will drop
 * events meeting criteria as determined by the {@link #isDiscardable(Object)} method.
 *
 * <p>The AsyncAppender will collect the events sent to it and then
 * dispatch them to all the appenders that are attached to it. You can
 * attach multiple appenders to an AsyncAppender.
 *
 * <p>The AsyncAppender uses a separate thread to serve the events in
 * its bounded buffer.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @param <E>
 */
public class AsyncAppenderBase<E> extends UnsynchronizedAppenderBase<E> implements AppenderAttachable<E> {

  AppenderAttachableImpl<E> aai = new AppenderAttachableImpl<E>();
  BlockingQueue<E> blockingQueue;

  /** The default buffer size. */
  public static final int DEFAULT_BUFFER_SIZE = 256;
  int bufferSize = DEFAULT_BUFFER_SIZE;


  static final int UNDEFINED = -1;
  int discardingThreshold = UNDEFINED;

  Worker worker = new Worker();

  /**
   * Is the eventObject passed as parameter discardable? The base class's implementation of this method always returns
   * 'false' but sub-classes may (and do) override this method.
   *
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
  protected void preprocess(E eventObject) {}


  @Override
  public void start() {
    if(numberOfAttachedAppenders() == 0) {
      addError("No attached appenders found.");
      return;
    }
    blockingQueue = new ArrayBlockingQueue<E>(bufferSize);
    addInfo("in start");
    if(discardingThreshold == UNDEFINED)
      discardingThreshold = bufferSize / 5;

    worker.setDaemon(true);
    worker.setName("AsyncAppender-Worker-" + worker.getName());
    worker.start();
    super.start();
  }

  @Override
  public void stop() {
    if(!isStarted())
      return;

    // mark this appender as stopped so that Worker can also stop if it is invoking aii.appendLoopOnAppenders
    // and sub-appenders consume the interruption
    super.stop();

    // interrupt the worker thread so that it can terminate
    // interruption can be consumed by sub-appenders
    worker.interrupt();
    try {
      worker.join(1000);
    } catch (InterruptedException e) {
      addError("Failed to join worker thread", e);  //To change body of catch statement use File | Settings | File Templates.
    }
  }

  private int numberOfAttachedAppenders() {
    int i = 0;
    Iterator it = aai.iteratorForAppenders();
    while(it.hasNext()) {
      it.next();
      i++;
    }
    return i;
  }


  @Override
  protected void append(E eventObject) {
    if(blockingQueue.remainingCapacity() < discardingThreshold && isDiscardable(eventObject)) {
     return;
    }
    preprocess(eventObject);
    put(eventObject);
  }

  private void put(E eventObject) {
    try {
      blockingQueue.put(eventObject);
    } catch (InterruptedException e) {
    }
  }

  public int getBufferSize() {
    return bufferSize;
  }

  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  public int getDiscardingThreshold() {
    return discardingThreshold;
  }

  public void setDiscardingThreshold(int discardingThreshold) {
    this.discardingThreshold = discardingThreshold;
  }

  public void addAppender(Appender<E> newAppender) {
    aai.addAppender(newAppender);
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
      while(parent.isStarted()) {
        try {
          E e = parent.blockingQueue.take();
          aai.appendLoopOnAppenders(e);
        } catch (InterruptedException ie) {
          break;
        }
      }

      addInfo("Worker thread will flush remaining events before exiting. ");
      for(E e: parent.blockingQueue) {
        aai.appendLoopOnAppenders(e);
      }

      aai.detachAndStopAllAppenders();
    }
  }
}
