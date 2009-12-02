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

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.status.StatusManager;

public class ContextBase implements Context {

  private long birthTime = System.currentTimeMillis();
  
  private String name;
  private StatusManager sm = new BasicStatusManager();
  // TODO propertyMap should be observable so that we can be notified
  // when it changes so that a new instance of propertyMap can be
  // serialized. For the time being, we ignore this shortcoming.
  Map<String, String> propertyMap = new HashMap<String, String>();
  Map<String, Object> objectMap = new HashMap<String, Object>();

  Object configurationLock = new Object();
  
  public StatusManager getStatusManager() {
    return sm;
  }

  /**
   * Set the {@link StatusManager} for this context. Note that by default this
   * context is initialized with a {@link BasicStatusManager}. A null value for
   * the 'statusManager' argument is not allowed.
   * 
   * <p> A malicious attacker can set the status manager to a dummy instance,
   * disabling internal error reporting.
   * 
   * @param statusManager
   *                the new status manager
   */
  public void setStatusManager(StatusManager statusManager) {
    // this method was added in response to http://jira.qos.ch/browse/LBCORE-35
    if (sm == null) {
      throw new IllegalArgumentException("null StatusManager not allowed");
    }
    this.sm = statusManager;
  }

  public Map<String, String> getCopyOfPropertyMap() {
    return new HashMap<String, String>(propertyMap);
  }

  public void putProperty(String key, String val) {
    this.propertyMap.put(key, val);
  }

  public String getProperty(String key) {
    return (String) this.propertyMap.get(key);
  }

  public Object getObject(String key) {
    return objectMap.get(key);
  }

  public void putObject(String key, Object value) {
    objectMap.put(key, value);
  }

  public String getName() {
    return name;
  }

  /**
   * Clear the internal objectMap and all properties.
   */
  public void reset() {
    propertyMap.clear();
    objectMap.clear();
  }

  /**
   * The context name can be set only if it is not already set, or if the
   * current name is the default context name, namely "default", or if the
   * current name and the old name are the same.
   * 
   * @throws IllegalStateException
   *                 if the context already has a name, other than "default".
   */
  public void setName(String name) throws IllegalStateException {
    if (name != null && name.equals(this.name)) {
      return; // idempotent naming
    }
    if (this.name == null
        || CoreConstants.DEFAULT_CONTEXT_NAME.equals(this.name)) {
      this.name = name;
    } else {
      throw new IllegalStateException("Context has been already given a name");
    }
  }

  public long getBithTime() {
    return birthTime;
  }

  public Object getConfigurationLock() {
    return configurationLock;
  }
}
