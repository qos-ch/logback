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

import java.io.Serializable;

import ch.qos.logback.core.spi.ContextAware;


/**
 * A client of a {@link ServerRunner} that receives events from a local
 * appender and logs them according to local policy.
 *
 * @author Carl Harris
 */
public interface RemoteLoggerClient extends Client, ContextAware {

  /**
   * Sets the size of the client's event queue.
   * <p>
   * Has no effect if invoked after {@link #run()} is invoked. 
   * @param queueSize the queue size to set
   */
  void setQueueSize(int queueSize);
  
  /**
   * Offers an event to the client.
   * @param event the subject event
   * @return {@code true} if the client's queue accepted the event,
   *    {@code false} if the client's queue is full
   */
  boolean offer(Serializable event);
  
}
