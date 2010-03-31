/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
 * 
 * This program and the accompanying materials are dual-licensed under either
 * the terms of the Eclipse Public License v1.0 as published by the Eclipse
 * Foundation
 * 
 * or (per the licensee's choosing)
 * 
 * under the terms of the GNU Lesser General Public License version 2.1 as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.PropertyDefinerBase;

public class FooPropertyDefiner extends PropertyDefinerBase {

  private String fooName;

  public String getPropertyValue() {
    return "Foo[" + fooName + "]";
  }

  public void setFooName(String fooName) {
    this.fooName = fooName;
  }
}
