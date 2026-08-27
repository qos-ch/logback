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
package ch.qos.logback.core.util;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An unmodifiable view of an existing {@link Map}, in the same spirit as
 * {@code Collections.UnmodifiableMap}.
 *
 * <p>The backing map is not copied. Callers that need a snapshot must pass a
 * copy.</p>
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.6.4
 */
public final class UnmodifiableWrappingMap<K, V> extends AbstractMap<K, V> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<? extends K, ? extends V> m;
    private transient Set<Entry<K, V>> entrySet;

    public UnmodifiableWrappingMap(Map<? extends K, ? extends V> m) {
        if (m == null) {
            throw new NullPointerException();
        }
        this.m = m;
    }

    @Override
    public int size() {
        return m.size();
    }

    @Override
    public boolean isEmpty() {
        return m.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return m.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return m.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return m.get(key);
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new UnmodifiableEntrySet<>(m.entrySet());
        }
        return entrySet;
    }

    @Override
    public boolean equals(Object o) {
        return o == this || m.equals(o);
    }

    @Override
    public int hashCode() {
        return m.hashCode();
    }

    /**
     * Wraps each {@link Entry} so {@link Entry#setValue(Object)} cannot mutate
     * the backing map.
     */
    private static final class UnmodifiableEntrySet<K, V> extends AbstractSet<Entry<K, V>> {

        private final Set<? extends Entry<? extends K, ? extends V>> s;

        UnmodifiableEntrySet(Set<? extends Entry<? extends K, ? extends V>> s) {
            this.s = s;
        }

        @Override
        public int size() {
            return s.size();
        }

        @Override
        public boolean contains(Object o) {
            return s.contains(o);
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new Iterator<Entry<K, V>>() {
                private final Iterator<? extends Entry<? extends K, ? extends V>> i = s.iterator();

                @Override
                public boolean hasNext() {
                    return i.hasNext();
                }

                @Override
                public Entry<K, V> next() {
                    return new SimpleImmutableEntry<>(i.next());
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
