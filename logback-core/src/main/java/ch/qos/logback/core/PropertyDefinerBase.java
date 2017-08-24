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
package ch.qos.logback.core;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyDefiner;

/**
 * A skeleton implementation for property definers so that they derive from {@link ContextAwareBase}.
 *  
 * @author Aleksey Didik
 */
public abstract class PropertyDefinerBase extends ContextAwareBase implements PropertyDefiner {

    static protected String booleanAsStr(boolean bool) {
        return bool ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
    }

}
