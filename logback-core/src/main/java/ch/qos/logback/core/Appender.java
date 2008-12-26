/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core;
  
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.FilterAttachable;
import ch.qos.logback.core.spi.LifeCycle;
  

public interface Appender<E> extends LifeCycle, ContextAware, FilterAttachable<E> {

  /**
   * Get the name of this appender. The name uniquely identifies the appender.
   */
  public String getName();

  /**
   * This is where an appender accomplishes its work. Note that the argument 
   * is of type Object.
   * @param event
   */
  void doAppend(E event) throws LogbackException;

  /**
   * Set the {@link Layout} for this appender.
   */
  public void setLayout(Layout<E> layout);

  /**
   * Returns this appenders layout.
   */
  public Layout<E> getLayout();

  /**
   * Set the name of this appender. The name is used by other components to
   * identify this appender.
   * 
   */
  public void setName(String name);
  
}
