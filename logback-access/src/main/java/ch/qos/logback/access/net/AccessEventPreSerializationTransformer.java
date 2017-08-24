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
package ch.qos.logback.access.net;

import java.io.Serializable;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.spi.PreSerializationTransformer;

public class AccessEventPreSerializationTransformer implements PreSerializationTransformer<IAccessEvent> {

    @Override
    public Serializable transform(IAccessEvent event) {
        if (event instanceof AccessEvent) {
            return (AccessEvent) event;
        } else {
            throw new IllegalArgumentException("Unsupported type " + event.getClass().getName());
        }
    }

}
