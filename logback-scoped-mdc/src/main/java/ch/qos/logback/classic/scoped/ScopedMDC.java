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
package ch.qos.logback.classic.scoped;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.lang.ScopedValue.CallableOp;

/**
 * A {@link ScopedValue}-based diagnostic context for use with virtual threads
 * and structured concurrency. Unlike the traditional MDC which uses
 * {@link ThreadLocal}, scoped values are automatically inherited by child
 * threads created via {@link java.util.concurrent.StructuredTaskScope#fork}.
 *
 * <p>Usage example:</p>
 * <pre>
 * ScopedMDC.put("requestId", "abc-123")
 *          .put("userId", "user-42")
 *          .run(() -&gt; {
 *              logger.info("Processing request");
 *          });
 * </pre>
 *
 * @since 1.5.33
 */
public final class ScopedMDC {

    private static final ScopedValue<Map<String, String>> SCOPED_MDC = ScopedValue.newInstance();

    private ScopedMDC() {
    }

    /**
     * Returns the value associated with the given key in the current scoped
     * context, or {@code null} if no value is bound for that key.
     *
     * @param key the key to look up
     * @return the value, or {@code null}
     */
    public static String get(String key) {
        return getPropertyMap().get(key);
    }

    /**
     * Returns an unmodifiable view of the current scoped context map.
     * Returns an empty map if no scoped context is bound.
     *
     * @return the current scoped context map, never {@code null}
     */
    public static Map<String, String> getPropertyMap() {
        return SCOPED_MDC.orElse(Collections.emptyMap());
    }

    /**
     * Creates a new {@link Binding} that will include the given key-value pair
     * merged with any values from the current scope.
     *
     * @param key   the key
     * @param value the value
     * @return a {@link Binding} to execute code within the new scope
     */
    public static Binding put(String key, String value) {
        Map<String, String> merged = new HashMap<>(getPropertyMap());
        merged.put(key, value);
        return new Binding(merged);
    }

    /**
     * Creates a new {@link Binding} that will include all entries from the
     * given map merged with any values from the current scope.
     *
     * @param entries the entries to add
     * @return a {@link Binding} to execute code within the new scope
     */
    public static Binding putAll(Map<String, String> entries) {
        Map<String, String> merged = new HashMap<>(getPropertyMap());
        merged.putAll(entries);
        return new Binding(merged);
    }

    /**
     * A binding that can be used to execute code within a scoped MDC context.
     * Additional entries can be chained via {@link #put(String, String)} before
     * calling {@link #run(Runnable)} or {@link #call(CallableOp)}.
     */
    public static final class Binding {

        private final Map<String, String> map;

        Binding(Map<String, String> map) {
            this.map = Collections.unmodifiableMap(new HashMap<>(map));
        }

        /**
         * Adds an additional key-value pair to this binding.
         *
         * @param key   the key
         * @param value the value
         * @return a new {@link Binding} with the additional entry
         */
        public Binding put(String key, String value) {
            Map<String, String> merged = new HashMap<>(map);
            merged.put(key, value);
            return new Binding(merged);
        }

        /**
         * Executes the given operation within this scoped MDC context.
         *
         * @param op the operation to run
         */
        public void run(Runnable op) {
            ScopedValue.where(SCOPED_MDC, map).run(op);
        }

        /**
         * Executes the given operation within this scoped MDC context and
         * returns its result.
         *
         * @param op  the operation to call
         * @param <R> the result type
         * @param <X> the exception type
         * @return the result of the operation
         * @throws X if the operation throws
         */
        public <R, X extends Throwable> R call(CallableOp<R, X> op) throws X {
            return ScopedValue.where(SCOPED_MDC, map).call(op);
        }
    }
}
