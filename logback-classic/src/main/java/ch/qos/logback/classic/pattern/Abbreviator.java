/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.pattern;

/**
 * An interface used for abbreviating strings according to different policies.
 * 
 * @author Ceki Gulcu
 */
public interface Abbreviator {

  public String abbreviate(String in);
}
