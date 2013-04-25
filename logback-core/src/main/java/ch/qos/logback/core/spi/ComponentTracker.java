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
package ch.qos.logback.core.spi;


import ch.qos.logback.core.CoreConstants;

import java.util.Collection;
import java.util.Set;

/**
 * Interface for tracking various components by key. Components which have not
 * been accessed for more than a user-specified duration are deemed stale and
 * removed.
 *
 * @author Ceki Gulcu
 *
 * @since 1.0.12
 */
public interface ComponentTracker<C> {


  int DEFAULT_TIMEOUT = 30 * 60 * CoreConstants.MILLIS_IN_ONE_SECOND; // 30 minutes
  int DEFAULT_MAX_COMPONENTS = Integer.MAX_VALUE;

  int getComponentCount();


  /**
   * Find the component identified by 'key', no timestamp update is performed.
   *
   * @param key
   * @return
   */
  C get(String key);

  /**
   * Get the component identified by 'key', updating its timestamp in the
   * process. If there is no corresponding component, create it. If the current
   * number of components is above or equal to 'getMaxComponents' then the least
   * recently accessed component is removed.
   *
   * @param key
   * @param timestamp
   * @return
   */
  C getOrCreate(String key, long timestamp);


  /**
   * Clear (and detach) components which are stale. Components which have not
   * been accessed for more than a user-specified duration are deemed stale.
   *
   *
   * @param now  current time in milliseconds
   */
  void removeStaleComponents(long now);


  /**
   * Mark component identified by 'key' as having reached its end-of-life.
   * @param key
   */
  void endOfLife(String key);

  /**
   * The collections of all components tracked by this instance.
   * @return
   */
  Collection<C> components();


  /**
   * Set of all keys in this tracker.
   * @return
   */
  Set<String> keySet();
}
