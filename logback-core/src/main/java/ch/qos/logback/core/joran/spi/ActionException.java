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
 * processing, all the nested elements nested within the element throwing the
 * exception or skip all following sibling elements in the document.
 *
 * @author Ceki Gulcu
 */
public class ActionException extends Exception {

  
  public enum SkipCode {
    /**
     * SKIP_CHILDREN signals the {@link Interpreter} to skip processing all the
     * nested elements contained within the element causing this ActionException.
     * 
     * <p>It is the only recognized skipping mode in Joran.
     */
    SKIP_CHILDREN;
  }
  
  private static final long serialVersionUID = 2743349809995319806L;


  final Throwable rootCause;
  final SkipCode skipCode;

  public ActionException(final SkipCode skipCode) {
    this(skipCode, null);
  }

  public ActionException(final SkipCode skipCode, final Throwable rootCause) {
    this.skipCode = skipCode;
    this.rootCause = rootCause;
  }

  public Throwable getCause() {
    return rootCause;
  }

  public SkipCode getSkipCode() {
    return skipCode;
  }
}
