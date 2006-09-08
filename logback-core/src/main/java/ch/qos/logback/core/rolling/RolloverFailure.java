/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.rolling;


/**
 * A RolloverFailure occurs if, for whatever reason a rollover fails.
 *
 * @author Ceki Gulcu
 */
public class RolloverFailure extends Exception {

  private static final long serialVersionUID = -4407533730831239458L;

  public RolloverFailure(String msg) {
    super(msg);
  }
}
