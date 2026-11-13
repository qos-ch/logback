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
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

/**
 * Serializer for {@code long} values that stores successive values as deltas.
 *
 * <p>Each written value is encoded as {@code current - lastWritten} via
 * {@link Output#writeVarLong(long, boolean)} (zig-zag). On read, the absolute
 * value is reconstructed by adding the delta to the last reconstructed value.
 * Write and read state are independent so the same instance can be used for
 * both directions without interference.
 *
 * <p>Typical for monotonically increasing timestamps in a log stream, where
 * deltas are small and varint-encode well.
 */
public class LongDeltaSerializer extends Serializer<Long> {

    private long lastWriteValue = 0;
    private long lastReadValue = 0;

    public LongDeltaSerializer() {
        setAcceptsNull(false);
        setImmutable(true);
    }

    /** Writes {@code value} as a zig-zag varint delta from the previous write. */
    public void writeLong(Output output, long value) {
        long delta = value - lastWriteValue;
        lastWriteValue = value;
        output.writeVarLong(delta, true);
    }

    /** Inverse of {@link #writeLong(Output, long)}. */
    public long readLong(Input input) {
        long delta = input.readVarLong(true);
        long value = lastReadValue + delta;
        lastReadValue = value;
        return value;
    }

    @Override
    public void write(Kryo kryo, Output output, Long value) {
        writeLong(output, value);
    }

    @Override
    public Long read(Kryo kryo, Input input, Class<? extends Long> type) {
        return readLong(input);
    }
}
