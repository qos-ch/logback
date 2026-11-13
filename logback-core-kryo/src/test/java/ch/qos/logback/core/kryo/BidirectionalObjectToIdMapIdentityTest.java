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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BidirectionalObjectToIdMapIdentityTest {

    @Test
    public void capacityIsCappedToMaxSerializerId() {
        BidirectionalObjectToIdMapIdentity<String> map =
                new BidirectionalObjectToIdMapIdentity<>(KryoCoreConstants.MAX_SERIALIZER_ID + 100);
        assertThat(map.capacity).isEqualTo(KryoCoreConstants.MAX_SERIALIZER_ID);
    }

    @Test
    public void sameInstanceHits() {
        BidirectionalObjectToIdMapIdentity<String> map = new BidirectionalObjectToIdMapIdentity<>(10);
        String a = new String("same");
        Integer id = map.insertNewEntryOnWrite(a);
        assertThat(map.getIdByValue(a)).isEqualTo(id);
        assertThat(map.getValueById(id)).isSameAs(a);
    }

    @Test
    public void equalDistinctInstancesAreDifferentKeys() {
        BidirectionalObjectToIdMapIdentity<String> map = new BidirectionalObjectToIdMapIdentity<>(10);
        String a = new String("same");
        String b = new String("same");
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotSameAs(b);

        Integer idA = map.insertNewEntryOnWrite(a);
        Integer idB = map.insertNewEntryOnWrite(b);
        assertThat(idA).isNotEqualTo(idB);
        assertThat(map.getIdByValue(a)).isEqualTo(idA);
        assertThat(map.getIdByValue(b)).isEqualTo(idB);
        assertThat(map.getIdByValue(new String("same"))).isNull();
    }

    @Test
    public void replaceOnReadById() {
        BidirectionalObjectToIdMapIdentity<String> map = new BidirectionalObjectToIdMapIdentity<>(10);
        String first = new String("v");
        String second = new String("v");
        assertThat(map.insertNewEntryOnRead(5, first)).isNull();
        assertThat(map.insertNewEntryOnRead(5, second)).isSameAs(first);
        assertThat(map.getValueById(5)).isSameAs(second);
        assertThat(map.getIdByValue(first)).isNull();
        assertThat(map.getIdByValue(second)).isEqualTo(5);
    }

    @Test
    public void lruByIdentity() {
        BidirectionalObjectToIdMapIdentity<String> map = new BidirectionalObjectToIdMapIdentity<>(3);
        String v1 = new String("v1");
        String v2 = new String("v2");
        String v3 = new String("v3");
        String v4 = new String("v4");
        map.insertNewEntryOnWrite(v1);
        map.insertNewEntryOnWrite(v2);
        map.insertNewEntryOnWrite(v3);

        map.getIdByValue(v1);

        map.insertNewEntryOnWrite(v4);
        assertThat(map.getIdByValue(v1)).isEqualTo(1);
        assertThat(map.getIdByValue(v2)).isNull();
        assertThat(map.getIdByValue(v3)).isEqualTo(3);
        assertThat(map.getIdByValue(v4)).isEqualTo(2);
        assertThat(map.getValueById(2)).isSameAs(v4);
    }

    @Test
    public void getValueByIdCountsAsAccess() {
        BidirectionalObjectToIdMapIdentity<String> map = new BidirectionalObjectToIdMapIdentity<>(3);
        String v1 = new String("v1");
        String v2 = new String("v2");
        String v3 = new String("v3");
        String v4 = new String("v4");
        map.insertNewEntryOnWrite(v1);
        map.insertNewEntryOnWrite(v2);
        map.insertNewEntryOnWrite(v3);

        map.getValueById(1);

        map.insertNewEntryOnWrite(v4);
        assertThat(map.getValueById(1)).isSameAs(v1);
        assertThat(map.getIdByValue(v2)).isNull();
        assertThat(map.getIdByValue(v4)).isEqualTo(2);
        assertThat(map.getValueById(2)).isSameAs(v4);
    }

    @Test
    public void insertionOrderEvictionWithoutAccess() {
        BidirectionalObjectToIdMapIdentity<String> map = new BidirectionalObjectToIdMapIdentity<>(3);
        String v1 = new String("v1");
        String v2 = new String("v2");
        String v3 = new String("v3");
        String v4 = new String("v4");
        map.insertNewEntryOnWrite(v1);
        map.insertNewEntryOnWrite(v2);
        map.insertNewEntryOnWrite(v3);
        map.insertNewEntryOnWrite(v4);

        assertThat(map.getIdByValue(v1)).isNull();
        assertThat(map.getIdByValue(v2)).isEqualTo(2);
        assertThat(map.getIdByValue(v3)).isEqualTo(3);
        assertThat(map.getIdByValue(v4)).isEqualTo(1);
        assertThat(map.getValueById(1)).isSameAs(v4);
    }

    @Test
    public void readRemapsSameInstanceToNewId() {
        BidirectionalObjectToIdMapIdentity<String> map = new BidirectionalObjectToIdMapIdentity<>(10);
        String v = new String("v");
        assertThat(map.insertNewEntryOnRead(1, v)).isNull();
        assertThat(map.insertNewEntryOnRead(2, v)).isNull();
        assertThat(map.getIdByValue(v)).isEqualTo(2);
        assertThat(map.getValueById(1)).isNull();
        assertThat(map.getValueById(2)).isSameAs(v);
    }

    @Test
    public void equalMapsWithDistinctIdentityMiss() {
        BidirectionalObjectToIdMapIdentity<Map<String, String>> map =
                new BidirectionalObjectToIdMapIdentity<>(10);
        Map<String, String> a = new HashMap<>();
        a.put("k", "v");
        Map<String, String> b = new HashMap<>(a);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotSameAs(b);

        Integer idA = map.insertNewEntryOnWrite(a);
        assertThat(map.getIdByValue(a)).isEqualTo(idA);
        assertThat(map.getIdByValue(b)).isNull();
    }

    @Test
    public void serializerBaseWiresIdentity() {
        final BidirectionalObjectToIdMap<?>[] slot = new BidirectionalObjectToIdMap<?>[1];
        class Probe extends SerializerBase<String> {
            Probe() {
                super(BiMapType.IDENTITY, 8);
                slot[0] = bimap;
            }

            @Override
            protected void writeNewEntry(com.esotericsoftware.kryo.kryo5.Kryo kryo,
                                         com.esotericsoftware.kryo.kryo5.io.Output output, String value) {
            }

            @Override
            protected String readNewEntry(com.esotericsoftware.kryo.kryo5.Kryo kryo,
                                          com.esotericsoftware.kryo.kryo5.io.Input input) {
                return null;
            }
        }
        new Probe();
        assertThat(slot[0]).isInstanceOf(BidirectionalObjectToIdMapIdentity.class);
    }
}
