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
package ch.qos.logback.classic.kryo;

import java.util.Objects;

/**
 * Restricts objects to types that are cheap and safe to serialize (strings and
 * primitive wrappers). Anything else is reduced via {@link Object#toString()}.
 * Used for KVP values and similar free-form fields before Kryo encoding.
 */
public class SafeObjectTransformer {

    /** Sentinel written in place of a null when a non-null object is required. */
    final public static String NULL_PROXY = "0<|null|>1";

    /**
     * Like {@link #asAllowedObject(Object)}, but maps {@code null} to {@link #NULL_PROXY}.
     */
    static Object asAllowedNonNullObject(Object o) {
        if (o == null)
            return NULL_PROXY;

        return asAllowedObject(o);
    }

    /**
     * {@code true} if {@code o} can be written by {@link SafeObjectCodec} without
     * {@link Object#toString()}. {@code o} must not be null.
     */
    static boolean isAllowedType(Object o) {
        return o instanceof String
                || o instanceof Integer
                || o instanceof Long
                || o instanceof Float
                || o instanceof Double
                || o instanceof Short;
    }

    /**
     * Returns {@code o} if it is a {@link String} or a numeric wrapper; otherwise
     * {@code o.toString()}. {@code o} must not be null.
     */
    static Object asAllowedObject(Object o) {
        Objects.requireNonNull(o);
        if (isAllowedType(o)) {
            return o;
        }
        return o.toString();
    }
}
