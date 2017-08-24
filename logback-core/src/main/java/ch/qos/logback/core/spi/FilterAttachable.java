/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
    void addFilter(Filter<E> newFilter);

    void clearAllFilters();

    /**
     * Get a copy of all the filters contained within this FilterAttachable
     * object.
     * 
     * @return all attached filters as a list
     */
    List<Filter<E>> getCopyOfAttachedFiltersList();

    /**
     * Loop through the filters in the chain. As soon as a filter decides on
     * ACCEPT or DENY, then that value is returned. If all of the filters return
     * NEUTRAL, then NEUTRAL is returned.
     */
    FilterReply getFilterChainDecision(E event);
}
