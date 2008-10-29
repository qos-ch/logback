/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.status.StatusManager;

public class ContextBase implements Context {

  private String name;
  private StatusManager sm = new BasicStatusManager();
  // TODO propertyMap should be observable so that we can be notified
  // when it changes so that a new instance of propertyMap can be
  // serialized. For the time being, we ignore this shortcoming.
  Map<String, String> propertyMap = new HashMap<String, String>();
  Map<String, Object> objectMap = new HashMap<String, Object>();

  public StatusManager getStatusManager() {
    return sm;
  }

  /**
   * Set the {@link StatusManager} for this context. Note that by default this
   * context is initialized with a {@link BasicStatusManager}. A null value for
   * the 'statusManager' argument is not allowed.
   * 
   * <p>
   * A malicious attacker can set the status manager to a dummy instance,
   * disabling internal error reporting.
   * 
   * @param statusManager
   *                the new status manager
   */
  // added in response to http://jira.qos.ch/browse/LBCORE-35
  public void setStatusManager(StatusManager statusManager) {
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

  public void setName(String name) {
    if (this.name != null) {
      throw new IllegalStateException("Context has been already given a name");
    }
    this.name = name;
  }
}
