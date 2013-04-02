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
package ch.qos.logback.core.net.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A factory bean that creates an {@link Executor}.
 * <p>
 * This object holds the configurable properties of a thread pool and creates a
 * properly configured {@link Executor}.
 * 
 * @author Carl Harris
 */
public class ThreadPoolFactoryBean {

  public static final int DEFAULT_POOL_SIZE = 10;

  private int corePoolSize = DEFAULT_POOL_SIZE;  
  private int maximumPoolSize;
  private long keepAliveTime;
  private int queueSize = 1;
  
  public Executor createExecutor() {
    BlockingQueue<Runnable> queue = createQueue();
    return createThreadPool(queue);
  }

  private BlockingQueue<Runnable> createQueue() {
    try {
      return new ArrayBlockingQueue<Runnable>(getQueueSize()); 
    }
    catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("illegal threadPool.queueSize");
    }
  }
  
  private Executor createThreadPool(BlockingQueue<Runnable> queue) {
    try {
      return new ThreadPoolExecutor(getCorePoolSize(), getMaximumPoolSize(), 
          getKeepAliveTime(), TimeUnit.MILLISECONDS, queue);
    }
    catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("illegal thread pool configuration: " + ex, ex);
    }
  }
  
  public int getCorePoolSize() {
    return corePoolSize;
  }

  public void setCorePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  public int getMaximumPoolSize() {
    if (maximumPoolSize < getCorePoolSize()) {
      return getCorePoolSize();
    }
    return maximumPoolSize;
  }

  public void setMaximumPoolSize(int maximumPoolSize) {
    this.maximumPoolSize = maximumPoolSize;
  }

  public long getKeepAliveTime() {
    return keepAliveTime;
  }

  public void setKeepAliveTime(long keepAliveTime) {
    this.keepAliveTime = keepAliveTime;
  }

  public int getQueueSize() {
    return queueSize;
  }

  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }

}
