/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran.spi;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry which maps a property in a host class to a default class.
 * 
 * @author Cek G&uuml;lc&uuml;
 * 
 */
public class DefaultNestedComponentRegistry {

  Map<HostClassAndPropertyDouble, Class> defaultComponentMap = new HashMap<HostClassAndPropertyDouble, Class>();

  public void add(Class hostClass, String propertyName, Class componentClass) {
    HostClassAndPropertyDouble hpDouble = new HostClassAndPropertyDouble(
        hostClass, propertyName.toLowerCase());
    defaultComponentMap.put(hpDouble, componentClass);
  }

  public Class findDefaultComponentType(Class hostClass, String propertyName) {
    propertyName = propertyName.toLowerCase();
    while (hostClass != null) {
      Class componentClass = oneShotFind(hostClass, propertyName);
      if (componentClass != null) {
        return componentClass;
      }
      hostClass = hostClass.getSuperclass();
    }
    return null;
  }

  private Class oneShotFind(Class hostClass, String propertyName) {
    HostClassAndPropertyDouble hpDouble = new HostClassAndPropertyDouble(
        hostClass, propertyName);
    return defaultComponentMap.get(hpDouble);
  }

}
