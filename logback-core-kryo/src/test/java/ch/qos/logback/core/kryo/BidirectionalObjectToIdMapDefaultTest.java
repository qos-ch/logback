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
import static org.assertj.core.api.Assertions.*;

public class BidirectionalObjectToIdMapDefaultTest {

    @Test
    public void capacityIsCappedToMaxSerializerId() {
        BidirectionalObjectToIdMapDefault<String> map =
                new BidirectionalObjectToIdMapDefault<>(KryoCoreConstants.MAX_SERIALIZER_ID + 100);
        assertThat(map.capacity).isEqualTo(KryoCoreConstants.MAX_SERIALIZER_ID);
    }

    @Test
    public void testBasicInsertionAndRetrieval() {
        BidirectionalObjectToIdMapDefault<String> map = new BidirectionalObjectToIdMapDefault<>(10);

        Integer id1 = map.insertNewEntryOnWrite("value1");
        assertThat(id1).isEqualTo(1);
        assertThat(map.getIdByValue("value1")).isEqualTo(1);
        assertThat(map.getValueById(1)).isEqualTo("value1");

        Integer id2 = map.insertNewEntryOnWrite("value2");
        assertThat(id2).isEqualTo(2);
        assertThat(map.getIdByValue("value2")).isEqualTo(2);
        assertThat(map.getValueById(2)).isEqualTo("value2");

        // Non-existent
        assertThat(map.getIdByValue("nonexistent")).isNull();
        assertThat(map.getValueById(99)).isNull();
    }

    @Test
    public void testSpecificIdInsertion() {
        BidirectionalObjectToIdMapDefault<String> map = new BidirectionalObjectToIdMapDefault<>(10);

        String oldValue = map.insertNewEntryOnRead(5, "value5");
        assertThat(oldValue).isNull();
        assertThat(map.getIdByValue("value5")).isEqualTo(5);
        assertThat(map.getValueById(5)).isEqualTo("value5");

        // Insert again with same id, should replace
        oldValue = map.insertNewEntryOnRead(5, "newValue5");
        assertThat(oldValue).isEqualTo("value5");
        assertThat(map.getIdByValue("newValue5")).isEqualTo(5);
        assertThat(map.getValueById(5)).isEqualTo("newValue5");
        assertThat(map.getIdByValue("value5")).isNull(); // old value removed
    }

    @Test
    public void testCapacityEviction() {
        int capacity = 3;
        BidirectionalObjectToIdMapDefault<String> map = new BidirectionalObjectToIdMapDefault<>(capacity);

        map.insertNewEntryOnWrite("v1");
        map.insertNewEntryOnWrite("v2");
        map.insertNewEntryOnWrite("v3");

        // All present
        assertThat(map.getIdByValue("v1")).isEqualTo(1);
        assertThat(map.getIdByValue("v2")).isEqualTo(2);
        assertThat(map.getIdByValue("v3")).isEqualTo(3);

        // Insert fourth, should evict v1 (eldest) and reuse its id
        map.insertNewEntryOnWrite("v4");
        assertThat(map.getIdByValue("v1")).isNull();
        assertThat(map.getIdByValue("v2")).isEqualTo(2);
        assertThat(map.getIdByValue("v3")).isEqualTo(3);
        assertThat(map.getIdByValue("v4")).isEqualTo(1);
        assertThat(map.getValueById(1)).isEqualTo("v4");
    }

    @Test
    public void testLRUAccessOrder() {
        int capacity = 3;
        BidirectionalObjectToIdMapDefault<String> map = new BidirectionalObjectToIdMapDefault<>(capacity);

        map.insertNewEntryOnWrite("v1");
        map.insertNewEntryOnWrite("v2");
        map.insertNewEntryOnWrite("v3");

        map.getIdByValue("v1");

        map.insertNewEntryOnWrite("v4");
        assertThat(map.getIdByValue("v1")).isEqualTo(1);
        assertThat(map.getIdByValue("v2")).isNull();
        assertThat(map.getIdByValue("v3")).isEqualTo(3);
        assertThat(map.getIdByValue("v4")).isEqualTo(2);
        assertThat(map.getValueById(2)).isEqualTo("v4");
    }

    @Test
    public void testGetValueByIdCountsAsAccess() {
        int capacity = 3;
        BidirectionalObjectToIdMapDefault<String> map = new BidirectionalObjectToIdMapDefault<>(capacity);

        map.insertNewEntryOnWrite("v1");
        map.insertNewEntryOnWrite("v2");
        map.insertNewEntryOnWrite("v3");

        map.getValueById(1);

        map.insertNewEntryOnWrite("v4");
        assertThat(map.getValueById(1)).isEqualTo("v1");
        assertThat(map.getIdByValue("v2")).isNull();
        assertThat(map.getIdByValue("v4")).isEqualTo(2);
        assertThat(map.getValueById(2)).isEqualTo("v4");
    }

    @Test
    public void testIdCounterIncrements() {
        BidirectionalObjectToIdMapDefault<String> map = new BidirectionalObjectToIdMapDefault<>(10);

        assertThat(map.insertNewEntryOnWrite("a")).isEqualTo(1);
        assertThat(map.insertNewEntryOnWrite("b")).isEqualTo(2);
        assertThat(map.insertNewEntryOnWrite("c")).isEqualTo(3);
    }

    @Test
    public void testMapsConsistency() {
        BidirectionalObjectToIdMapDefault<String> map = new BidirectionalObjectToIdMapDefault<>(10);

        map.insertNewEntryOnWrite("test");
        assertThat(map.getIdToValueMap().get(1)).isEqualTo("test");
        assertThat(map.getValueToIdMap().get("test")).isEqualTo(1);
    }

    @Test
    public void testEvictedIdsAreReusedAndStayWithin12Bits() {
        BidirectionalObjectToIdMapDefault<String> map = new BidirectionalObjectToIdMapDefault<>(8);
        for (int i = 0; i < 20_000; i++) {
            Integer id = map.insertNewEntryOnWrite("v" + i);
            assertThat(id).isBetween(1, KryoCoreConstants.MAX_SERIALIZER_ID);
        }
        assertThat(map.getValueToIdMap()).hasSize(8);
        for (Integer id : map.getValueToIdMap().values()) {
            assertThat(id).isBetween(1, 8);
        }
    }

    @Test
    public void writerAndReaderStayInSyncWithReuse() {
        BidirectionalObjectToIdMapDefault<String> writer = new BidirectionalObjectToIdMapDefault<>(3);
        BidirectionalObjectToIdMapDefault<String> reader = new BidirectionalObjectToIdMapDefault<>(3);
        for (int i = 0; i < 50; i++) {
            String v = "v" + i;
            Integer id = writer.insertNewEntryOnWrite(v);
            reader.insertNewEntryOnRead(id, v);
            assertThat(id).isBetween(1, 3);
            assertThat(reader.getValueById(id)).isEqualTo(v);
            assertThat(writer.getValueById(id)).isEqualTo(v);
        }
    }

    @Test
    public void testReadReusesWireIdAfterEviction() {
        BidirectionalObjectToIdMapDefault<String> map = new BidirectionalObjectToIdMapDefault<>(2);
        map.insertNewEntryOnRead(1, "a");
        map.insertNewEntryOnRead(2, "b");
        map.insertNewEntryOnRead(1, "c");
        assertThat(map.getValueById(1)).isEqualTo("c");
        assertThat(map.getIdByValue("a")).isNull();
        assertThat(map.getIdByValue("c")).isEqualTo(1);
    }
}
