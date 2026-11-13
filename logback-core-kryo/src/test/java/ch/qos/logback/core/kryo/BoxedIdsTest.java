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

import org.junit.jupiter.api.Test;

import static ch.qos.logback.core.kryo.KryoCoreConstants.MAX_SERIALIZER_ID;
import static org.assertj.core.api.Assertions.assertThat;

public class BoxedIdsTest {

    @Test
    public void internsInRange() {
        assertThat(BoxedIds.of(1)).isSameAs(BoxedIds.of(1));
        assertThat(BoxedIds.of(128)).isSameAs(BoxedIds.of(128));
        assertThat(BoxedIds.of(MAX_SERIALIZER_ID)).isSameAs(BoxedIds.of(MAX_SERIALIZER_ID));
    }

    @Test
    public void outOfRangeStillBoxes() {
        assertThat(BoxedIds.of(0)).isEqualTo(0);
        assertThat(BoxedIds.of(-1)).isEqualTo(-1);
        assertThat(BoxedIds.of(MAX_SERIALIZER_ID + 1)).isEqualTo(MAX_SERIALIZER_ID + 1);
    }
}
