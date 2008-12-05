/** 
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2005, QOS.ch, LOGBack.com
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.pattern.parser;

public class ScanException extends Exception {

  private static final long serialVersionUID = -3132040414328475658L;

  Throwable cause;

  public ScanException(String msg) {
    super(msg);
  }

  public ScanException(String msg, Throwable rootCause) {
    super(msg);
    this.cause = rootCause;
  }

  public Throwable getCause() {
   return cause;
  }
}

