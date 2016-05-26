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
package ch.qos.logback.core.filter;

import ch.qos.logback.core.spi.FilterReply;

public abstract class AbstractMatcherFilter<E> extends Filter<E> {

    protected FilterReply onMatch = FilterReply.NEUTRAL;
    protected FilterReply onMismatch = FilterReply.NEUTRAL;

    final public void setOnMatch(FilterReply reply) {
        this.onMatch = reply;
    }

    final public void setOnMismatch(FilterReply reply) {
        this.onMismatch = reply;
    }

    final public FilterReply getOnMatch() {
        return onMatch;
    }

    final public FilterReply getOnMismatch() {
        return onMismatch;
    }
}
