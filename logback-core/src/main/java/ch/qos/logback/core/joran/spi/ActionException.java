/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran.spi;


/**
 * By throwing an exception an action can signal the Interpreter to skip
 * processing of all the nested (child) elements of the element associated with
 * the action causing the exception.
 *
 * @author Ceki Gulcu
 */
public class ActionException extends Exception {

  
  private static final long serialVersionUID = 2743349809995319806L;

  public ActionException() {
  }

  public ActionException(final Throwable rootCause) {
    super(rootCause);
  }

}
