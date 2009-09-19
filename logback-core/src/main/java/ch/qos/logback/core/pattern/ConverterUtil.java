/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
