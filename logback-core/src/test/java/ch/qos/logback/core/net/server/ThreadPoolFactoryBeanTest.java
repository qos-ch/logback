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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * Unit tests for {@link ThreadPoolFactoryBean}.
 *
 * @author Carl Harris
 */
public class ThreadPoolFactoryBeanTest {

  private ThreadPoolFactoryBean factoryBean = new ThreadPoolFactoryBean();
  
  @Test
  public void testDefaults() throws Exception {
    ThreadPoolExecutor executor = (ThreadPoolExecutor) 
        factoryBean.createExecutor();
    assertTrue(executor.getQueue() instanceof SynchronousQueue);
    assertEquals(ThreadPoolFactoryBean.DEFAULT_CORE_POOL_SIZE, 
        executor.getCorePoolSize());
    assertEquals(ThreadPoolFactoryBean.DEFAULT_MAXIMUM_POOL_SIZE,
        executor.getMaximumPoolSize());
    assertEquals(ThreadPoolFactoryBean.DEFAULT_KEEP_ALIVE_TIME,
        executor.getKeepAliveTime(TimeUnit.MILLISECONDS));
  }
  
  @Test
  public void testPositiveQueueSize() throws Exception {
    factoryBean.setQueueSize(1);
    ThreadPoolExecutor executor = (ThreadPoolExecutor) 
        factoryBean.createExecutor();
    assertTrue(executor.getQueue() instanceof ArrayBlockingQueue);
    assertEquals(factoryBean.getQueueSize(), 
        ((ArrayBlockingQueue) executor.getQueue()).remainingCapacity());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testNegativeQueueSize() throws Exception {
    factoryBean.setQueueSize(-1);
    factoryBean.createExecutor();
  }
  

}
