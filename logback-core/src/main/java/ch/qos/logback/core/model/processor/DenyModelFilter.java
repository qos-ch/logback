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

public class DenyModelFilter implements ModelFilter {

    final Class<? extends Model> deniedModelType;

    DenyModelFilter(Class<? extends Model> deniedModelType) {
        this.deniedModelType = deniedModelType;
    }

    @Override
    public FilterReply decide(Model model) {

        if (model.getClass() == deniedModelType) {
            return FilterReply.DENY;
        }

        return FilterReply.NEUTRAL;
    }

}
