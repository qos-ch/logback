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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A compact, unmodifiable {@link Map} of {@link String} keys and values for
 * small snapshots such as the MDC property map.
 *
 * <p>Entries are copied into parallel arrays at construction, so later
 * mutations of the source map are not visible. {@link #get(Object)} looks up
 * by linear index.</p>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.6.4
 */
public final class MinimalUnmodifiableMap extends AbstractMap<String, String> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final MinimalUnmodifiableMap EMPTY = new MinimalUnmodifiableMap();

    private transient String[] keys;
    private transient String[] values;
    private transient Set<Entry<String, String>> entrySet;

    /**
     * Returns a shared empty instance.
     */
    public static MinimalUnmodifiableMap emptyMap() {
        return EMPTY;
    }

    private MinimalUnmodifiableMap() {
        this.keys = EMPTY_STRING_ARRAY;
        this.values = EMPTY_STRING_ARRAY;
    }

    public MinimalUnmodifiableMap(Map<String, String> map) {
        int n = map.size();
        this.keys = new String[n];
        this.values = new String[n];
        int i = 0;
        for (Entry<String, String> e : map.entrySet()) {
            keys[i] = e.getKey();
            values[i] = e.getValue();
            i++;
        }
    }

    @Override
    public int size() {
        return keys.length;
    }

    @Override
    public boolean isEmpty() {
        return keys.length == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return indexOf(key) >= 0;
    }

    @Override
    public String get(Object key) {
        int i = indexOf(key);
        return i >= 0 ? values[i] : null;
    }

    private int indexOf(Object key) {
        if (!(key instanceof String)) {
            return -1;
        }
        for (int i = 0; i < keys.length; i++) {
            if (key.equals(keys[i])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    private final class EntrySet extends AbstractSet<Entry<String, String>> {

        @Override
        public int size() {
            return keys.length;
        }

        @Override
        public Iterator<Entry<String, String>> iterator() {
            return new Iterator<Entry<String, String>>() {
                private int i = 0;

                @Override
                public boolean hasNext() {
                    return i < keys.length;
                }

                @Override
                public Entry<String, String> next() {
                    if (i >= keys.length) {
                        throw new NoSuchElementException();
                    }
                    Entry<String, String> entry = new SimpleImmutableEntry<>(keys[i], values[i]);
                    i++;
                    return entry;
                }
            };
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(keys.length);
        for (int i = 0; i < keys.length; i++) {
            out.writeObject(keys[i]);
            out.writeObject(values[i]);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int n = in.readInt();
        keys = new String[n];
        values = new String[n];
        for (int i = 0; i < n; i++) {
            keys[i] = (String) in.readObject();
            values[i] = (String) in.readObject();
        }
    }

    private Object readResolve() {
        return keys.length == 0 ? EMPTY : this;
    }
}
