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

import static org.assertj.core.api.Assertions.assertThat;

public class FrequencyGatedBidirectionalObjectToIdMapTest {

    @Test
    public void capacityIsCappedToMaxSerializerId() {
        FrequencyGatedBidirectionalObjectToIdMap<String> map =
                new FrequencyGatedBidirectionalObjectToIdMap<>(KryoCoreConstants.MAX_SERIALIZER_ID + 100);
        assertThat(map.capacity).isEqualTo(KryoCoreConstants.MAX_SERIALIZER_ID);
        assertThat(map.getBidirectionalObjectToIdMap().capacity).isEqualTo(KryoCoreConstants.MAX_SERIALIZER_ID);
    }

    @Test
    public void testInsertNewEntryOnReadGating() {
        FrequencyGatedBidirectionalObjectToIdMap<String> map = new FrequencyGatedBidirectionalObjectToIdMap<>(10);

        // First insert: count=1 not >1, id=UNREGISTERED_ID (-1), not inserted
        Integer id1 = map.insertNewEntryOnWrite("value1");
        assertThat(id1).isEqualTo(-1);
        assertThat(map.getIdByValue("value1")).isNull(); // not inserted
        assertThat(map.getValueById(-1)).isNull(); // not inserted

        // Second insert of same value: count=2 >1, id=1, inserted
        Integer id2 = map.insertNewEntryOnWrite("value1");
        assertThat(id2).isEqualTo(1);
        assertThat(map.getIdByValue("value1")).isEqualTo(1);
        assertThat(map.getValueById(1)).isEqualTo("value1");
    }

    @Test
    public void testInsertNewEntryOnRead() {
        FrequencyGatedBidirectionalObjectToIdMap<String> map = new FrequencyGatedBidirectionalObjectToIdMap<>(10);

        // First insert "value1": count=1 not >1, id=-1, not inserted
        Integer id1 = map.insertNewEntryOnWrite("value1");
        assertThat(id1).isEqualTo(-1);
        assertThat(map.getIdByValue("value1")).isNull();

        // Second insert "value1": count=2 >1, id=1, inserted
        Integer id1_real = map.insertNewEntryOnWrite("value1");
        assertThat(id1_real).isEqualTo(1);
        assertThat(map.getValueById(1)).isEqualTo("value1");

        // First insert "value2": count=1 not >1, id=-1, not inserted
        Integer id2 = map.insertNewEntryOnWrite("value2");
        assertThat(id2).isEqualTo(-1);
        assertThat(map.getIdByValue("value2")).isNull();

        // Second insert "value2": count=2 >1, id=2, inserted
        Integer id2_real = map.insertNewEntryOnWrite("value2");
        assertThat(id2_real).isEqualTo(2);
        assertThat(map.getValueById(2)).isEqualTo("value2");
    }

    @Test
    public void testSpecificIdInsertion() {
        FrequencyGatedBidirectionalObjectToIdMap<String> map = new FrequencyGatedBidirectionalObjectToIdMap<>(10);

        String oldValue = map.insertNewEntryOnRead(5, "value5");
        assertThat(oldValue).isNull();
        assertThat(map.getValueById(5)).isEqualTo("value5");

        // Insert again with same id
        oldValue = map.insertNewEntryOnRead(5, "newValue5");
        assertThat(oldValue).isEqualTo("value5");
        assertThat(map.getValueById(5)).isEqualTo("newValue5");
    }

    @Test
    public void testCapacityEviction() {
        int capacity = 3;
        FrequencyGatedBidirectionalObjectToIdMap<String> map = new FrequencyGatedBidirectionalObjectToIdMap<>(capacity);

        // Simulate having passed gating, insert with real ids
        map.insertNewEntryOnRead(1, "v1");
        map.insertNewEntryOnRead(2, "v2");
        map.insertNewEntryOnRead(3, "v3");
        map.insertNewEntryOnRead(4, "v4"); // evicts 1

        assertThat(map.getValueById(1)).isNull(); // evicted
        assertThat(map.getValueById(2)).isEqualTo("v2");
        assertThat(map.getValueById(3)).isEqualTo("v3");
        assertThat(map.getValueById(4)).isEqualTo("v4");
    }

    @Test
    public void testRecordAccess() {
        FrequencyGatedBidirectionalObjectToIdMap<String> map = new FrequencyGatedBidirectionalObjectToIdMap<>(10);

        assertThat(map.recordAccess("test")).isEqualTo(1);
        assertThat(map.recordAccess("test")).isEqualTo(2);
        assertThat(map.recordAccess("test")).isEqualTo(3);
    }

    @Test
    public void testAccessCountMap() {
        FrequencyGatedBidirectionalObjectToIdMap<String> map = new FrequencyGatedBidirectionalObjectToIdMap<>(10);

        map.insertNewEntryOnWrite("a"); // count=1
        map.insertNewEntryOnWrite("a"); // count=2
        assertThat(map.getAccessCountMap().get("a")).isEqualTo(2);
    }
}
