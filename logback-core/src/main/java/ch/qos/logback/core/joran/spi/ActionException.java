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

  private static final long serialVersionUID = 2743349809995319806L;

  /**
   * SKIP_CHILDREN signals the {@link Interpreter} to skip processing all the
   * nested elements contained within the element causing this ActionException.
   */
  public static final int SKIP_CHILDREN = 1;

  /**
   * SKIP_SIBLINGS signals the {@link Interpreter} to skip processing all the
   * children of this element as well as all the siblings of this elements,
   * including any children they may have.
   */
  public static final int SKIP_SIBLINGS = 2;
  final Throwable rootCause;
  final int skipCode;

  public ActionException(final int skipCode) {
    this(skipCode, null);
  }

  public ActionException(final int skipCode, final Throwable rootCause) {
    this.skipCode = skipCode;
    this.rootCause = rootCause;
  }

  public Throwable getCause() {
    return rootCause;
  }

  public int getSkipCode() {
    return skipCode;
  }
}
