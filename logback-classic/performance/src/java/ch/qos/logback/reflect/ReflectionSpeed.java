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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionSpeed {

  static final Class[] EMPTY_CLASS_PARAM = new Class[] {};
  static final Object[] EMPTY_OBJECT_PARAM = new Object[] {};
  
  public static void main(String[] args) throws Exception {
    
    Fruit f = new Fruit("red", true);
    
    Class fruitClass = Fruit.class;
    Method getColorMethod = fruitClass.getDeclaredMethod("getColor", EMPTY_CLASS_PARAM);
    loop(getColorMethod, f);
    loop(getColorMethod, f);
    loop(getColorMethod, f);
  }
  
  static void loop(Method getColorMethod, Fruit fruit) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    final int LEN = 100*1000;
    
    long start = System.nanoTime();
    for(int i = 0; i < LEN; i++) {
      getColorMethod.invoke(fruit, EMPTY_OBJECT_PARAM );
    }

    long end = System.nanoTime();
    System.out.println("avg: "+((end-start)/LEN)+" nanos");
  }

  
}
