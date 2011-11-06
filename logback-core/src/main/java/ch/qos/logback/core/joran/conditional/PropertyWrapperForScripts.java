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

import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.util.OptionHelper;

public class PropertyWrapperForScripts {

  PropertyContainer local;
  PropertyContainer context;

  // this method is invoked by reflection in PropertyEvalScriptBuilder
  public void setPropertyContainers(PropertyContainer local, PropertyContainer context) {
    this.local = local;
    this.context = context;
  }

  public boolean isNull(String k) {
    String val = OptionHelper.propertyLookup(k, local, context);
    return (val == null);
  }

  public boolean isDefined(String k) {
    String val = OptionHelper.propertyLookup(k, local, context);
    return (val != null);
  }

  public String p(String k) {
    return property(k);
  }
  
  public String property(String k) {
    String val = OptionHelper.propertyLookup(k, local, context);
    if(val != null)
      return val;
    else
      return "";
  }
}
