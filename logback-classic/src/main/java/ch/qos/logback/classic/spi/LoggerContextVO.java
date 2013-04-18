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
package ch.qos.logback.classic.spi;

import java.io.Serializable;
import java.util.Map;

import ch.qos.logback.classic.LoggerContext;

/**
 * LoggerContextVO offers a restricted view of LoggerContext intended to be
 * exposed by LoggingEvent to remote systems. This restricted view is optimized
 * for serialization.
 * 
 * <p>
 * Some of the LoggerContext or Logger attributes MUST not survive
 * serialization, e.g appenders, level values etc, as these attributes may have
 * other values on the remote platform. LoggerContextVO class exposes the
 * minimal and relevant attributes to the remote host, instead of having to deal
 * with an incomplete LoggerContext with many null references.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class LoggerContextVO implements Serializable {

  private static final long serialVersionUID = 5488023392483144387L;

  final String name;
  final Map<String, String> propertyMap;
  final long birthTime;

  public LoggerContextVO(LoggerContext lc) {
    this.name = lc.getName();
    this.propertyMap = lc.getCopyOfPropertyMap();
    this.birthTime = lc.getBirthTime();
  }

  public LoggerContextVO(String name, Map<String,String> propertyMap, long birthTime) {
    this.name = name;
    this.propertyMap = propertyMap;
    this.birthTime = birthTime;
  }

  public String getName() {
    return name;
  }

  public Map<String, String> getPropertyMap() {
    return propertyMap;
  }

  public long getBirthTime() {
    return birthTime;
  }


  @Override
  public String toString() {
    return "LoggerContextVO{" +
            "name='" + name + '\'' +
            ", propertyMap=" + propertyMap +
            ", birthTime=" + birthTime +
            '}';
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LoggerContextVO)) return false;

    LoggerContextVO that = (LoggerContextVO) o;

    if (birthTime != that.birthTime) return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (propertyMap != null ? !propertyMap.equals(that.propertyMap) : that.propertyMap != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (propertyMap != null ? propertyMap.hashCode() : 0);
    result = 31 * result + (int) (birthTime ^ (birthTime >>> 32));

    return result;
  }
}
