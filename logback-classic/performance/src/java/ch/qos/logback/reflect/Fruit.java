/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.reflect;

public class Fruit {

  String color;
  boolean sweet;
  
  Fruit(  String color, boolean sweet) {
    this.color = color;
    this.sweet = sweet;
  }
  
  public String getColor() {
    return color;
  }
  public void setColor(String color) {
    this.color = color;
  }
  public boolean isSweet() {
    return sweet;
  }
  public void setSweet(boolean sweet) {
    this.sweet = sweet;
  }
  

}
