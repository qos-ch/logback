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
package ch.qos.logback.core.joran.conditional;

import java.util.HashMap;
import java.util.Map;

public class MapWrapperForScripts {
  public Map map = new HashMap<String, String>();
  public String name;
  
  
  public void setMap(Map map) {
    this.map = map;
  }
  public void setName(String name) {
    this.name = name;
  }
  
  public boolean isNull(String k) {
    Object o = map.get(k);
    if (o instanceof String) {
      return false;
    }
    return (System.getProperty(k) == null);
  }
  
  public String p(String k) {
    return property(k);
  }
  
  public String property(String k) {
    Object o = map.get(k);
    if (o instanceof String) {
      return (String) o;
    }
    String v = System.getProperty(k);
    if(v != null) {
      return v;
    }
    System.getProperties();
    return "";
  }
   

}
