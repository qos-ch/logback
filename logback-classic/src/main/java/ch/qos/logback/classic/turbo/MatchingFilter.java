/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.turbo;

import ch.qos.logback.core.spi.FilterReply;

/**
 * An abstract class containing support for {@link #onMatch} on {@link #onMismatch} 
 * attributes, shared by many but not all turbo filters.
 *  
 * @author Ceki Gulcu
 */
public abstract class MatchingFilter extends TurboFilter {

  protected FilterReply onMatch = FilterReply.NEUTRAL;
  protected FilterReply onMismatch = FilterReply.NEUTRAL;
    
  final public void setOnMatch(String action) {
    if ("NEUTRAL".equals(action)) {
      onMatch = FilterReply.NEUTRAL;
    } else if ("ACCEPT".equals(action)) {
      onMatch = FilterReply.ACCEPT;
    } else if ("DENY".equals(action)) {
      onMatch = FilterReply.DENY;
    }
  }

  final public void setOnMismatch(String action) {
    if ("NEUTRAL".equals(action)) {
      onMismatch = FilterReply.NEUTRAL;
    } else if ("ACCEPT".equals(action)) {
      onMismatch = FilterReply.ACCEPT;
    } else if ("DENY".equals(action)) {
      onMismatch = FilterReply.DENY;
    }
  }
}
