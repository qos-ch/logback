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
package ch.qos.logback.core.spi;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.helpers.CyclicBuffer;

/**
 * An interface for tracking cyclic buffers by key.
 *
 * @author Ceki G&uuml;c&uuml;
 */
public interface CyclicBufferTracker<E> {

  public static int DEFAULT_BUFFER_SIZE = 256;
  public static int DEFAULT_NUMBER_OF_BUFFERS = 64;

  static int THRESHOLD = 30 * 60 * CoreConstants.MILLIS_IN_ONE_SECOND; // 30 minutes

  public int getBufferSize();

  public void setBufferSize(int size);
  public int getMaxNumberOfBuffers();

  /**
   * Set the maximum number of tracked buffers. After reaching the maximum number of
   * buffers, the creation of a new buffer implies the removal of the least recently
   * used buffer.
   *
   * @param maxNumBuffers
   */
  public void setMaxNumberOfBuffers(int maxNumBuffers);


  /**
   * Get the cyclic buffer identified by 'key', updating its timestamp in the process.
   * If there is no such buffer, create it. If the current number of buffers is
   * above or equal to 'maxNumBuffers' then the least recently accessed buffer is removed.
   *
   * @param key
   * @param timestamp
   * @return
   */
  CyclicBuffer<E> getOrCreate(String key, long timestamp);

  /**
   * Remove a cyclic buffer identified by its key.
   */
  void removeBuffer(String key);

  /**
   * Clear (and detach) buffers which are stale.
   *
   * @param now
   */
  void clearStaleBuffers(long now);

  /**
   * The size of the internal map/list/collection holding the cyclic buffers.
   * @return  size of internal collection
   */
  int size();
}
