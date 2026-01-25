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

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.FilterReply;

public class AllowModelFilter implements ModelFilter {

    final Class<? extends Model> allowedModelType;

    AllowModelFilter(Class<? extends Model> allowedType) {
        this.allowedModelType = allowedType;
    }

    @Override
    public FilterReply decide(Model model) {

        if (model.getClass() == allowedModelType) {
            return FilterReply.ACCEPT;
        }

        return FilterReply.NEUTRAL;
    }

}
