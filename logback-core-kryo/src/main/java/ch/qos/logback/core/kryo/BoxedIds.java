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
package ch.qos.logback.core.kryo;

import static ch.qos.logback.core.kryo.KryoCoreConstants.MAX_SERIALIZER_ID;

/**
 * Interns serializer ids in {@code 1..MAX_SERIALIZER_ID} so map lookups do not
 * allocate a new {@link Integer} per call.
 */
final class BoxedIds {

    private static final Integer[] TABLE = new Integer[MAX_SERIALIZER_ID + 1];

    private BoxedIds() {
    }

    static Integer of(int id) {
        if (id > 0 && id <= MAX_SERIALIZER_ID) {
            Integer boxed = TABLE[id];
            if (boxed == null) {
                boxed = Integer.valueOf(id);
                TABLE[id] = boxed;
            }
            return boxed;
        }
        return Integer.valueOf(id);
    }
}
