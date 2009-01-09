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

import java.util.List;

import ch.qos.logback.core.filter.Filter;

/**
 * Interface for attaching filters to objects.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public interface FilterAttachable<E> {
  /**
   * Add a filter.
   */
  public void addFilter(Filter<E> newFilter);

  /**
   * Get first filter in the chain.
   */
  public Filter getFirstFilter();

  public void clearAllFilters();

  /**
   * Get a copy of all the filters contained within this FilterAttachable
   * object.
   * 
   * @return all attached filters as a list
   */
  public List<Filter<E>> getCopyOfAttachedFiltersList();

  /**
   * Loop through the filters in the chain. As soon as a filter decides on
   * ACCEPT or DENY, then that value is returned. If all of the filters return
   * NEUTRAL, then NEUTRAL is returned.
   */
  public FilterReply getFilterChainDecision(E event);
}
