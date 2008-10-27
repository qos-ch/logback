/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.spi;

import ch.qos.logback.core.filter.Filter;

/**
 * Implementation of FilterAttachable.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
final public class FilterAttachableImpl implements FilterAttachable {

  Filter headFilter;
  Filter tailFilter;

  /**
   * Add a filter to end of the filter list.
   */
  public void addFilter(Filter newFilter) {
    if (headFilter == null) {
      headFilter = newFilter;
      tailFilter = newFilter;
    } else {
      tailFilter.setNext(newFilter);
      tailFilter = newFilter;
    }
  }

  /**
   * Get first filter in the chain.
   */
  public Filter getFirstFilter() {
    return headFilter;
  }

  /**
   * Clear the filter chain
   */
  public void clearAllFilters() {
    Filter f = headFilter;
    while (f != null) {
      final Filter next = f.getNext();
      f.setNext(null);
      f = next;
    }
    f = null;
    headFilter = null;
    tailFilter = null;
  }

  /**
   * Loop through the filters in the chain. As soon as a filter decides on 
   * ACCEPT or DENY, then that value is returned. If all of the filters return
   * NEUTRAL, then  NEUTRAL is returned.
   */
  public FilterReply getFilterChainDecision(Object event) {
    Filter f = headFilter;

    while (f != null) {
      switch (f.decide(event)) {
      case DENY:
        return FilterReply.DENY;

      case ACCEPT:
        return FilterReply.ACCEPT;

      case NEUTRAL:
        f = f.getNext();
      }
    }
    return FilterReply.NEUTRAL;
  }
}
