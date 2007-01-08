/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core;

import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterAttachable;
import ch.qos.logback.core.spi.FilterAttachableImpl;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.WarnStatus;

/**
 * This class is used to manage base functionnalities of all appenders.
 * 
 * For more informations about this appender, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#AppenderBase
 *
 * @author Ceki G&uuml;lc&uuml;
 */
abstract public class AppenderBase<E> extends ContextAwareBase implements
    Appender<E>, FilterAttachable {

  protected boolean started = false;

  /**
   * The guard prevents an appender from repeatedly calling its own doAppend
   * method.
   */
  private boolean guard = false;

  /**
   * Appenders are named.
   */
  protected String name;

  private FilterAttachableImpl fai = new FilterAttachableImpl();

  public String getName() {
    return name;
  }

  private int statusRepeatCount = 0;
  private int exceptionCount = 0;
  
  static final int ALLOWED_REPEATS = 5;

  
  public synchronized void doAppend(E eventObject) {
    // WARNING: The guard check MUST be the first statement in the
    // doAppend() method.

    // prevent re-entry.
    if (guard) {
      return;
    }

    try {
      guard = true;

      if (!this.started) {
        if (statusRepeatCount++ < ALLOWED_REPEATS) {
          addStatus(new WarnStatus(
              "Attempted to append to non started appender [" + name + "].",
              this));
        }
        return;
      }

      if (getFilterChainDecision(eventObject) == FilterReply.DENY) {
        return;
      }
      
      // ok, we now invoke derived class' implementation of append
      this.append(eventObject);

    } catch(Exception e) {
      if (exceptionCount++ < ALLOWED_REPEATS) {
        addError("Appender ["+name+"] failed to append.", e);
      }
    }  finally {
      guard = false;
    }
  }

  abstract protected void append(E eventObject);

  /**
   * Set the name of this appender.
   */
  public void setName(String name) {
    this.name = name;
  }

  public void start() {
    started = true;
  }

  public void stop() {
    started = false;
  }

  public boolean isStarted() {
    return started;
  }

  public String toString() {
    return this.getClass().getName() + "[" + name + "]";
  }

  public void addFilter(Filter newFilter) {
    fai.addFilter(newFilter);
  }

  public Filter getFirstFilter() {
    return fai.getFirstFilter();
  }

  public void clearAllFilters() {
    fai.clearAllFilters();
  }

  public FilterReply getFilterChainDecision(Object event) {
    return fai.getFilterChainDecision(event);
  }
  
  public Layout<E> getLayout() {
    return null;
  }

  public void setLayout(Layout<E> layout) {
  }
}
