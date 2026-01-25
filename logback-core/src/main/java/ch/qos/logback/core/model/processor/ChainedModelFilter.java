/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.model.processor;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.FilterReply;

public class ChainedModelFilter implements ModelFilter {

    List<ModelFilter> modelFilters = new ArrayList<>();

    public ChainedModelFilter() {
    }

    static public ChainedModelFilter newInstance() {
        return new ChainedModelFilter();
    }

    public ChainedModelFilter allow(Class<? extends Model> allowedType) {
        modelFilters.add(new AllowModelFilter(allowedType));
        return this;
    }

    public ChainedModelFilter deny(Class<? extends Model> allowedType) {
        modelFilters.add(new DenyModelFilter(allowedType));
        return this;
    }

    public ChainedModelFilter denyAll() {
        modelFilters.add(new DenyAllModelFilter());
        return this;
    }

    public ChainedModelFilter allowAll() {
        modelFilters.add(new AllowAllModelFilter());
        return this;
    }

    @Override
    public FilterReply decide(Model model) {

        for (ModelFilter modelFilter : modelFilters) {
            FilterReply reply = modelFilter.decide(model);

            switch (reply) {
            case ACCEPT:
            case DENY:
                return reply;
            case NEUTRAL:
                // next
            }
        }
        return FilterReply.NEUTRAL;
    }

}
