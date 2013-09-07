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

package ch.qos.logback.core.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.qos.logback.core.CoreConstants;

/**
 * Static utility methods for manipulating an {@link ExecutorService}.
 * 
 * @author Carl Harris
 */
public class ExecutorServiceUtil {

  /**
   * Creates an executor service suitable for use by logback components.
   * @return executor service
   */
  static public ExecutorService newExecutorService() {
    return new ThreadPoolExecutor(CoreConstants.CORE_POOL_SIZE, 
        CoreConstants.MAX_POOL_SIZE,
        0L, TimeUnit.MILLISECONDS,
        new SynchronousQueue<Runnable>());
  }
  
  /**
   * Shuts down an executor service.
   * <p>
   * @param executorService the executor service to shut down
   */
  static public void shutdown(ExecutorService executorService) {
    executorService.shutdownNow();
  }

}
