/*
 * File created on Apr 8, 2013 
 *
 * Copyright 2008-2011 Virginia Polytechnic Institute and State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
