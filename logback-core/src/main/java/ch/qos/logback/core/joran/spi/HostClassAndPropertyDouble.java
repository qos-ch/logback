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

/**
 * A 2-tuple (a double) consisting of a Class and a String. The Class references
 * the hosting class of a component and the String represents the property name
 * under which a nested component is referenced the host.
 * 
 * This class is used by {@link DefaultNestedComponentRegistry}.
 * 
 * @author Ceki Gulcu
 * 
 */
public class HostClassAndPropertyDouble {

  final Class hostClass;
  final String propertyName;

  public HostClassAndPropertyDouble(Class hostClass, String propertyName) {
    this.hostClass = hostClass;
    this.propertyName = propertyName;
  }

  public Class getHostClass() {
    return hostClass;
  }

  public String getPropertyName() {
    return propertyName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((hostClass == null) ? 0 : hostClass.hashCode());
    result = prime * result
        + ((propertyName == null) ? 0 : propertyName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final HostClassAndPropertyDouble other = (HostClassAndPropertyDouble) obj;
    if (hostClass == null) {
      if (other.hostClass != null)
        return false;
    } else if (!hostClass.equals(other.hostClass))
      return false;
    if (propertyName == null) {
      if (other.propertyName != null)
        return false;
    } else if (!propertyName.equals(other.propertyName))
      return false;
    return true;
  }

}
