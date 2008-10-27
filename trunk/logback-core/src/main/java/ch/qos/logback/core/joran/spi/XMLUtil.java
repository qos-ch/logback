/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.joran.spi;

import java.net.URL;

import ch.qos.logback.core.status.StatusManager;

public class XMLUtil {
  

  static public final int ILL_FORMED = 1;
  static public final int UNRECOVERABLE_ERROR = 2;
  
  static public int checkIfWellFormed(URL url, StatusManager sm) {
    return 0;
  }
  
}
