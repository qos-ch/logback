/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.pattern;


/**
 * Implements this to perform post compile processing for a PatternLayout.
 * 
 * For example, PatternLayot in the classic module should add a converter for
 * exception handling (otherwise exceptions would not be printed).
 * 
 * @author Ceki Gulcu
 */
public interface PostCompileProcessor<E> {

  /**
   * Post compile processing of the converter chain.
   * 
   * @param head
   *                The first converter in the chain
   */
  void process(Converter<E> head);
}
