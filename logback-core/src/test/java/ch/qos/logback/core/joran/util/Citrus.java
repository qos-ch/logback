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

package ch.qos.logback.core.joran.util;

public abstract class Citrus<T> {

    public static final String PRECARP_PROPERTY_NAME = "pericarp";
    public static final String PREFIX_PROPERTY_NAME = "prefix";

    @SuppressWarnings("unused")
    private T pericarp;

    String prefix;

    public void setPericarp(T pericarp) {
        this.pericarp = pericarp;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public abstract void foo();

}
