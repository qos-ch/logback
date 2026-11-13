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

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.KeyValuePair;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyValuePairSerializerV3Test {

    private Kryo kryo;
    private KeyValuePairSerializerV3 pairSerializer;
    private KeyValuePairCollectionSerializerV3 collectionSerializer;

    @BeforeEach
    public void setUp() {
        pairSerializer = new KeyValuePairSerializerV3(256);
        collectionSerializer = new KeyValuePairCollectionSerializerV3(pairSerializer);
        kryo = new Kryo();
        kryo.register(KeyValuePair.class, pairSerializer);
        kryo.register(ArrayList.class, collectionSerializer);
    }

    @Test
    public void roundTripAllValueTypes() {
        List<KeyValuePair> original = new ArrayList<>();
        original.add(new KeyValuePair("s", "hello"));
        original.add(new KeyValuePair("n", null));
        original.add(new KeyValuePair("i", 42));
        original.add(new KeyValuePair("l", 99L));
        original.add(new KeyValuePair("h", (short) 7));
        original.add(new KeyValuePair("f", 1.5f));
        original.add(new KeyValuePair("d", 2.5d));
        original.add(new KeyValuePair("other", new Object() {
            @Override
            public String toString() {
                return "stringified";
            }
        }));

        byte[] bytes = writeCollection(original);
        @SuppressWarnings("unchecked")
        ArrayList<KeyValuePair> restored = kryo.readObject(new Input(bytes), ArrayList.class, collectionSerializer);

        assertThat(restored).hasSize(original.size());
        assertThat(restored.get(0).key).isEqualTo("s");
        assertThat(restored.get(0).value).isEqualTo("hello");
        assertThat(restored.get(1).key).isEqualTo("n");
        assertThat(restored.get(1).value).isNull();
        assertThat(restored.get(2).value).isEqualTo(42);
        assertThat(restored.get(2).value).isInstanceOf(Integer.class);
        assertThat(restored.get(3).value).isEqualTo(99L);
        assertThat(restored.get(3).value).isInstanceOf(Long.class);
        assertThat(restored.get(4).value).isEqualTo((short) 7);
        assertThat(restored.get(4).value).isInstanceOf(Short.class);
        assertThat(restored.get(5).value).isEqualTo(1.5f);
        assertThat(restored.get(6).value).isEqualTo(2.5d);
        assertThat(restored.get(7).value).isEqualTo("stringified");
        assertThat(restored.get(7).value).isInstanceOf(String.class);
    }

    @Test
    public void stringKeysAndValuesShareCache() {
        List<KeyValuePair> original = new ArrayList<>();
        original.add(new KeyValuePair("k", "v"));
        original.add(new KeyValuePair("k", "v"));
        original.add(new KeyValuePair("k", "v"));

        byte[] once = writeCollection(List.of(new KeyValuePair("k", "v")));
        byte[] thrice = writeCollection(original);

        // three pairs should not be ~3x one pair once the string cache is warm
        assertThat(thrice.length).isLessThan(once.length * 3);
    }

    private byte[] writeCollection(List<KeyValuePair> list) {
        // fresh serializers so cache state is controlled per call
        KeyValuePairCollectionSerializerV3 ser = new KeyValuePairCollectionSerializerV3(256);
        Kryo k = new Kryo();
        Output out = new Output(512, -1);
        k.writeObject(out, new ArrayList<>(list), ser);
        out.flush();
        return out.toBytes();
    }
}
