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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static ch.qos.logback.core.kryo.KryoCoreConstants.MAX_SERIALIZER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SerializerIdPoolTest {

    static Stream<Supplier<SerializerIdPool>> pools() {
        return Stream.of(
                SetSerializerIdPool::new,
                DequeSerializerIdPool::new,
                DualSerializerIdPool::new
        );
    }

    @ParameterizedTest
    @MethodSource("pools")
    public void mintsThenRecycles(Supplier<SerializerIdPool> factory) {
        SerializerIdPool pool = factory.get();
        Integer a = pool.allocate();
        Integer b = pool.allocate();
        assertThat(a).isEqualTo(1);
        assertThat(b).isEqualTo(2);
        assertThat(pool.hasFree()).isFalse();

        pool.recycle(a);
        assertThat(pool.hasFree()).isTrue();
        assertThat(pool.allocate()).isEqualTo(a);
        assertThat(pool.hasFree()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("pools")
    public void unfreePreventsReuseUntilRecycle(Supplier<SerializerIdPool> factory) {
        SerializerIdPool pool = factory.get();
        Integer a = pool.allocate();
        pool.allocate();
        pool.recycle(a);
        pool.unfree(a);
        assertThat(pool.hasFree()).isFalse();
        assertThat(pool.allocate()).isEqualTo(3);
    }

    @ParameterizedTest
    @MethodSource("pools")
    public void recycleIsIdempotent(Supplier<SerializerIdPool> factory) {
        SerializerIdPool pool = factory.get();
        Integer a = pool.allocate();
        pool.recycle(a);
        pool.recycle(a);
        assertThat(pool.allocate()).isEqualTo(a);
        assertThat(pool.hasFree()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("pools")
    public void exhaustedWhenPastMaxAndNothingFree(Supplier<SerializerIdPool> factory) {
        SerializerIdPool pool = factory.get();
        for (int i = 1; i <= MAX_SERIALIZER_ID; i++) {
            assertThat(pool.allocate()).isEqualTo(i);
        }
        assertThatThrownBy(pool::allocate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exhausted");
        pool.recycle(1);
        assertThat(pool.allocate()).isEqualTo(1);
    }
}
