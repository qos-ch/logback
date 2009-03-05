/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.sift;

import ch.qos.logback.core.spi.LifeCycle;

/**
 * Implement this interface in order to compute a discriminating value for a
 * given event of type &lt;E&gt;.
 * 
 * <p>The returned value can depend on any data available at the time of the
 * call, including data contained within the currently running thread.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 * @param <E>
 */
public interface Discriminator<E> extends LifeCycle {
  
  /**
   * Given event 'e' return a discriminating value.
   * 
   * @param e
   * @return
   */
  String getDiscriminatingValue(E e);

  /**
   * 
   * @return
   */
  String getKey();
}
