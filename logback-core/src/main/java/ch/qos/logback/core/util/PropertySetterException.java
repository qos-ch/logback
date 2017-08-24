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
package ch.qos.logback.core.util;

/**
 * Thrown when an error is encountered whilst attempting to set a property
 * using the {@link ch.qos.logback.core.joran.util.PropertySetter} utility class.
 * 
 * @author Anders Kristensen
 */
public class PropertySetterException extends Exception {

    private static final long serialVersionUID = -2771077768281663949L;

    public PropertySetterException(String msg) {
        super(msg);
    }

    public PropertySetterException(Throwable rootCause) {
        super(rootCause);
    }

    public PropertySetterException(String message, Throwable cause) {
        super(message, cause);
    }
}
