/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.corpus;

public class MessageArgumentTuple {
  
  final String message;
  final int numberOfArguments;

  MessageArgumentTuple(String message) {
    this(message, 0);
  }

  public MessageArgumentTuple(String message, int numberOfArguments) {
    this.message = message;
    this.numberOfArguments = numberOfArguments;
  }

}
