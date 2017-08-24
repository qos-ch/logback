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
package ch.qos.logback.core.joran.spi;

/**
 * By throwing an exception an action can signal the Interpreter to skip
 * processing of all the nested (child) elements of the element associated with
 * the action causing the exception.
 *
 * @author Ceki Gulcu
 */
public class ActionException extends Exception {

    private static final long serialVersionUID = 2743349809995319806L;

    public ActionException() {
    }

    public ActionException(final Throwable rootCause) {
        super(rootCause);
    }

}
