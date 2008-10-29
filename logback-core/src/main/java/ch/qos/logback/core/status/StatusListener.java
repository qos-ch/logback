/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.status;

/**
 * A StatusListener registered with logback context's {@link StatusManager} will
 * receive notification of every incoming {@link Status status} message.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public interface StatusListener {
  void addStatusEvent(Status status);
}
