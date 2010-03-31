/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.spi;

import java.util.concurrent.CopyOnWriteArrayList;

import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Implementation of TurboFilterAttachable.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
final public class TurboFilterList extends CopyOnWriteArrayList<TurboFilter> {

  private static final long serialVersionUID = 1L;

  /**
   * Loop through the filters in the chain. As soon as a filter decides on
   * ACCEPT or DENY, then that value is returned. If all of the filters return
   * NEUTRAL, then NEUTRAL is returned.
   */
  public final FilterReply getTurboFilterChainDecision(final LoggingEvent event) {
    Object[] tfa = toArray();
    final int len = tfa.length;
    for (int i = 0; i < len; i++) {
      final TurboFilter tf = (TurboFilter) tfa[i];
      final FilterReply r = tf.decide(event);
      if (r == FilterReply.DENY || r == FilterReply.ACCEPT) {
        return r;
      }
    }
    return FilterReply.NEUTRAL;
  }
}
