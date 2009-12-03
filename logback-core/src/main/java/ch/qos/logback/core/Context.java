/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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

import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.status.StatusManager;

/**
 * A context is the main anchorage point of all logback components.
 * 
 * @author Ceki Gulcu
 * 
 */
public interface Context extends PropertyContainer {

  /**
   * Return the StatusManager instance in use.
   * 
   * @return the {@link StatusManager} instance in use.
   */
  public StatusManager getStatusManager();

  /**
   * A Context can act as a store for various objects used by LOGBack
   * components.
   * 
   * @return The object stored under 'key'.
   */
  public Object getObject(String key);

  /**
   * Store an object under 'key'. If no object can be found, null is returned.
   * 
   * @param key
   * @param value
   */
  public void putObject(String key, Object value);

  /**
   * Get all the properties for this context as a Map. Note that the returned
   * cop might be a copy not the original. Thus, modifying the returned Map will
   * have no effect (on the original.)
   * 
   * @return
   */
  // public Map<String, String> getPropertyMap();
  /**
   * Get the property of this context.
   */
  public String getProperty(String key);

  /**
   * Set a property of this context.
   */
  public void putProperty(String key, String value);

  /**
   * Contexts are named objects.
   * 
   * @return the name for this context
   */
  public String getName();

  /**
   * The name of the context can be set only once.
   * 
   * @param name
   */
  public void setName(String name);
  
  /**
   * The time at which this context was created, expressed in
   * millisecond elapsed since the epoch (1.1.1970).
   * 
   * @return The time as measured when this class was created.
   */
  public long getBithTime();
  
  /**
   * Object used for synchronization purposes. 
   * INTENDED FOR INTERNAL USAGE.
   */
  public Object getConfigurationLock();

}
