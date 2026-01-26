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
package ch.qos.logback.classic.spi;

public interface IThrowableProxy {

    /**
     * Return the overriding message if any. This method returns null
     * if there is no overriding message.
     *
     * <p>Overriding message exists only if the original throwable implementation overrides the toString() method.</p>
     *
     * @return the overriding message or null
     * @since 1.5.22
     */
    default String getOverridingMessage() {
        return null;
    }

    String getMessage();

    String getClassName();

    StackTraceElementProxy[] getStackTraceElementProxyArray();

    int getCommonFrames();

    IThrowableProxy getCause();

    IThrowableProxy[] getSuppressed();

    /**
     * Is this instance the result of a cyclic exception?
     *
     * @return true if cyclic, false otherwise
     * @since 1.3.0
     */
    boolean isCyclic();
}
