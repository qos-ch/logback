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

import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterAttachableImpl;
import ch.qos.logback.core.status.StatusManager;


public class ContextBase implements Context {

	private String name;
  StatusManager sm = new BasicStatusManager();
  // TODO propertyMap should be observable so that we can be notified
  // when it changes so that a new instance of propertyMap can be
  // serialized. For the time being, we ignore this shortcoming.
  Map<String, String> propertyMap = new HashMap<String, String>();
  Map<String, Object> objectMap = new HashMap<String, Object>();
  Map<String, String> converterMap = new HashMap<String, String>();
  private FilterAttachableImpl fai = new FilterAttachableImpl();
  
  public StatusManager getStatusManager() {
    return sm;
  }
  
  public Map<String, String> getPropertyMap() {
    return propertyMap;
  }

  public void setProperty(String key, String val) {
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
  
  public Map<String, String> getConverterMap() {
    return converterMap;
  }

  public void addFilter(Filter newFilter) {
    fai.addFilter(newFilter);
  }
  
  public Filter getFirstFilter() {
    return fai.getFirstFilter();
  }

  public void clearAllFilters() {
    fai.clearAllFilters();
  }

  public int getFilterChainDecision(Object event) {
    return fai.getFilterChainDecision(event);
  }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(this.name != null) {
			throw new IllegalStateException("Context has been already given a name");
		}
		this.name = name;
	}
}
