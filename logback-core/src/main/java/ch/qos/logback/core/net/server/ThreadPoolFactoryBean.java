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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A factory bean that creates an {@link Executor}.
 * <p>
 * This object holds the configurable properties of a thread pool and creates a
 * properly configured {@link Executor}.
 * <p>
 * The default configuration of this bean creates a {@link ThreadPoolExecutor}
 * that is functionally equivalent to the executor service created by
 * {@link java.util.concurrent.Executors#newCachedThreadPool()}.  It configures
 * the pool with a single core thread and an (effectively) unbounded maximum
 * pool size.  It uses a {@link SynchronousQueue} for direct handoff of 
 * submitted tasks to a thread in the pool, with no queueing.  This
 * configuration is suitable for most applications.
 * <p>  
 * When the {@code queueSize} property is set to a non-zero value, the 
 * resulting thread pool uses a bounded queue to hold submitted tasks while
 * the total number of active tasks in the pool is at the limit specified by
 * the {@code maximumPoolSize} property.  In general this behavior <em>is
 * not</em> desirable for applications that use a listener socket to accept
 * new clients, which are then submitted to the pool as asynchronous clients.
 * In such applications, it is best to allow new clients that cannot be 
 * accommodated (because all threads in the pool are assigned to existing 
 * clients) to simply remain in the listener socket's queue, since this 
 * has the least impact on local resources.
 * 
 * @author Carl Harris
 */
public class ThreadPoolFactoryBean {

  public static final int DEFAULT_CORE_POOL_SIZE = 1;
  public static final int DEFAULT_MAXIMUM_POOL_SIZE = Integer.MAX_VALUE;
  public static final int DEFAULT_KEEP_ALIVE_TIME = 60000;
  public static final int DEFAULT_QUEUE_SIZE = 0;

  private int corePoolSize = DEFAULT_CORE_POOL_SIZE;  
  private int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE;
  private long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
  private int queueSize = DEFAULT_QUEUE_SIZE;
  
  /**
   * Creates the executor service based on the bean's configuration.
   * @return executor service
   */
  public ExecutorService createExecutor() {
    return createThreadPool(createQueue());
  }

  /**
   * Creates the queue for the thread pool.
   * @return a synchronous queue or bounded queue, depending on the specified
   *    queue size
   */
  private BlockingQueue<Runnable> createQueue() {
    try {
      return queueSize == 0 ?
          new SynchronousQueue<Runnable>() : 
              new ArrayBlockingQueue<Runnable>(getQueueSize()); 
    }
    catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("illegal threadPool.queueSize");
    }
  }
  
  /**
   * Creates a thread pool with a given work queue.
   * @param queue work queue for the thread pool
   * @return
   */
  private ExecutorService createThreadPool(BlockingQueue<Runnable> queue) {
    try {
      return new ThreadPoolExecutor(getCorePoolSize(), getMaximumPoolSize(), 
          getKeepAliveTime(), TimeUnit.MILLISECONDS, queue);
    }
    catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(
          "illegal thread pool configuration: " + ex, ex);
    }
  }
  
  /**
   * Gets the core size of the thread pool.
   * @return number of idle threads that will be maintained in the pool even
   *    when no tasks are executing
   */
  public int getCorePoolSize() {
    return corePoolSize;
  }

  /**
   * Sets the core pool size of the thread pool.
   * @param corePoolSize number of idle threads that will be maintained in the
   *    pool when no tasks are executing; note that a core thread is created 
   *    only in response to a task submission
   */
  public void setCorePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  /**
   * Gets the maximum size of the thread pool.
   * @return maximum number of threads that are allowed in the pool
   */
  public int getMaximumPoolSize() {
    return maximumPoolSize;
  }

  /**
   * Sets the maximum size of the thread pool.
   * @param maximumPoolSize number of threads that are allowed in the pool
   */
  public void setMaximumPoolSize(int maximumPoolSize) {
    this.maximumPoolSize = maximumPoolSize;
  }

  /**
   * Gets the keep alive time for idle threads in the pool.
   * @return number of milliseconds that an idle thread will remain in the
   *    pool; note that when the number of threads in the pool is less than
   *    or equal to {@code corePoolSize} the remaining threads are retained 
   *    in the pool indefinitely. 
   */
  public long getKeepAliveTime() {
    return keepAliveTime;
  }

  /**
   * Sets the keep alive time for idle threads in the pool.
   * @param keepAliveTime number of milliseconds that an idle thread will
   *    remain in the pool; note that when the number of threads in the pool 
   *    is less than or equal to {@code corePoolSize} the remaining threads 
   *    are retained in the pool indefinitely.
   */
  public void setKeepAliveTime(long keepAliveTime) {
    this.keepAliveTime = keepAliveTime;
  }

  /**
   * Gets the size of the thread pool's work queue.
   * @return queue size
   */
  public int getQueueSize() {
    return queueSize;
  }

  /**
   * Sets the size of the thread pool's work queue.
   * @param queueSize the queue size to set.  Note that when the queue size is
   *    zero, the work queue will effectively inhibit queuing of submitted 
   *    tasks; each task will either be assigned to a thread or will be
   *    rejected immediately, if the maximum number of threads has been
   *    reached.  A positive queue size will result in a bounded work queue
   *    of the given size.
   */
  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }

}
