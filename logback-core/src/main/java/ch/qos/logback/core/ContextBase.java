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
package ch.qos.logback.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import ch.qos.logback.core.spi.LogbackLock;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.ComponentFactory;
import ch.qos.logback.core.util.EnvUtil;

import static ch.qos.logback.core.CoreConstants.CONTEXT_NAME_KEY;

public class ContextBase implements Context {

  private long birthTime = System.currentTimeMillis();

  private String name;
  private StatusManager sm = new BasicStatusManager();
  /**
   * Configuration factory name
   */
  private ComponentFactory cf = null;
  // TODO propertyMap should be observable so that we can be notified
  // when it changes so that a new instance of propertyMap can be
  // serialized. For the time being, we ignore this shortcoming.
  Map<String, String> propertyMap = new HashMap<String, String>();
  Map<String, Object> objectMap = new HashMap<String, Object>();

  LogbackLock configurationLock = new LogbackLock();

  // CORE_POOL_SIZE must be 1 for JDK 1.5. For JD 1.6 or higher it's set to 0
  // so that there are no idle threads
  private static final int CORE_POOL_SIZE = EnvUtil.isJDK5() ? 1 : 0;

  // 0 (JDK 1,6+) or 1 (JDK 1.5) idle threads, 2 maximum threads, no idle waiting
  ExecutorService executorService = new ThreadPoolExecutor(CORE_POOL_SIZE, 2,
          0L, TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<Runnable>());

  public StatusManager getStatusManager() {
    return sm;
  }

  /**
   * Set the {@link StatusManager} for this context. Note that by default this
   * context is initialized with a {@link BasicStatusManager}. A null value for
   * the 'statusManager' argument is not allowed.
   * <p/>
   * <p> A malicious attacker can set the status manager to a dummy instance,
   * disabling internal error reporting.
   *
   * @param statusManager the new status manager
   */
  public void setStatusManager(StatusManager statusManager) {
    // this method was added in response to http://jira.qos.ch/browse/LBCORE-35
    if (sm == null) {
      throw new IllegalArgumentException("null StatusManager not allowed");
    }
    this.sm = statusManager;
  }
  
  public ComponentFactory getComponentFactory() {
      return cf;
  }
  
  public void setComponentFactory(ComponentFactory configurationFactory) {
      cf = configurationFactory;
  }

  public Map<String, String> getCopyOfPropertyMap() {
    return new HashMap<String, String>(propertyMap);
  }

  public void putProperty(String key, String val) {
    this.propertyMap.put(key, val);
  }

  /**
   * Given a key, return the corresponding property value. If invoked with
   * the special key "CONTEXT_NAME", the name of the context is returned.
   *
   * @param key
   * @return
   */
  public String getProperty(String key) {
    if (CONTEXT_NAME_KEY.equals(key))
      return getName();

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
   * @throws IllegalStateException if the context already has a name, other than "default".
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

  public long getBirthTime() {
    return birthTime;
  }

  public Object getConfigurationLock() {
    return configurationLock;
  }

  public ExecutorService getExecutorService() {
    return  executorService;
  }

  @Override
  public String toString() {
    return name;
  }
}
