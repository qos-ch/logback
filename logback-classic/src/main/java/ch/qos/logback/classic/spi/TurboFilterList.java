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
package ch.qos.logback.classic.spi;

import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
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
  public FilterReply getTurboFilterChainDecision(final Marker marker,
      final Logger logger, final Level level, final String format,
      final Object[] params, final Throwable t) {
    
    
    final int size = size();
//    if (size == 0) {
//      return FilterReply.NEUTRAL;
//    }
    if (size == 1) {
      try {
        TurboFilter tf = get(0);
        return tf.decide(marker, logger, level, format, params, t);
      } catch (IndexOutOfBoundsException iobe) {
        return FilterReply.NEUTRAL;
      }
    }
    
    Object[] tfa = toArray();
    final int len = tfa.length;
    for (int i = 0; i < len; i++) {
    //for (TurboFilter tf : this) {
      final TurboFilter tf = (TurboFilter) tfa[i];
      final FilterReply r = tf.decide(marker, logger, level, format, params, t);
      if (r == FilterReply.DENY || r == FilterReply.ACCEPT) {
        return r;
      }
    }
    return FilterReply.NEUTRAL;
  }

  // public boolean remove(TurboFilter turboFilter) {
  // return tfList.remove(turboFilter);
  // }
  //
  // public TurboFilter remove(int index) {
  // return tfList.remove(index);
  // }
  //
  // final public int size() {
  // return tfList.size();
  // }

}
