/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.rolling;


/**
 * SizeBasedTriggeringPolicy looks at size of the file being
 * currently written to.
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 */
abstract public class TriggeringPolicyBase implements TriggeringPolicy {
  
  private boolean start;

  public void start() {
    start = true;
  }

  public void stop() {
    start = false;
  }

  public boolean isStarted() {
    return start;
  }



}
