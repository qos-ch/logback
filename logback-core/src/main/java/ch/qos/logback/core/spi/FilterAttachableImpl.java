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

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.util.COWArrayList;

/**
 * Implementation of FilterAttachable.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
final public class FilterAttachableImpl<E> implements FilterAttachable<E> {

    @SuppressWarnings("unchecked")
    COWArrayList<Filter<E>> filterList = new COWArrayList<Filter<E>>(new Filter[0]);

    /**
     * Add a filter to end of the filter list.
     */
    public void addFilter(Filter<E> newFilter) {
        filterList.add(newFilter);
    }

    /**
     * Clear the filter chain
     */
    public void clearAllFilters() {
        filterList.clear();
    }

    /**
     * Loop through the filters in the list. As soon as a filter decides on
     * ACCEPT or DENY, then that value is returned. If all of the filters return
     * NEUTRAL, then NEUTRAL is returned.
     */
    public FilterReply getFilterChainDecision(E event) {

        final Filter<E>[] filterArrray = filterList.asTypedArray();
        final int len = filterArrray.length;

        for (int i = 0; i < len; i++) {
            final FilterReply r = filterArrray[i].decide(event);
            if (r == FilterReply.DENY || r == FilterReply.ACCEPT) {
                return r;
            }
        }

        // no decision
        return FilterReply.NEUTRAL;
    }

    public List<Filter<E>> getCopyOfAttachedFiltersList() {
        return new ArrayList<Filter<E>>(filterList);
    }
}
