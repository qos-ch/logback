/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.contention;

/**
 * A runnable with 'done' and 'counter' fields.
 * 
 * @author ceki
 *
 */
abstract public class RunnableWithCounterAndDone implements Runnable {

  protected boolean done = false;
  protected long counter = 0;
  
  public long getCounter() {
    return counter;
  }

  public void setDone(boolean done) {
    this.done = done;
  }

  public boolean isDone() {
    return done;
  }
  
}
