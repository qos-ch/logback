/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.pattern;

public class ConverterUtil {

  /**
   * Start converters in the chain of converters.
   * @param head
   */
  public static void startConverters(Converter head) {
    Converter c = head;
    while (c != null) {
      if (c instanceof DynamicConverter) {
        DynamicConverter dc = (DynamicConverter) c;
        dc.start();
      } else if(c instanceof CompositeConverter){
        CompositeConverter cc = (CompositeConverter) c;
        Converter childConverter = cc.childConverter;
        startConverters(childConverter);
      }
      c = c.getNext();
    }
  }

  
  public static<E> Converter<E> findTail(Converter<E> head) {
    Converter<E> c = head;
    while (c != null) {
      Converter<E> next = c.getNext();
      if (next == null) {
        break;
      } else {
        c = next;
      }
    }
    return c;
  }
}
