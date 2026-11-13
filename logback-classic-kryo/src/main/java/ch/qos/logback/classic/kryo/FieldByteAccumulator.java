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

import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.LongSupplier;

/**
 * Accumulates bytes written to an {@link Output} or read from an {@link Input}, keyed by field name.
 * <p>
 * Write accounting uses {@link Output#position()} (suitable for a fixed buffer with no flush).
 * Read accounting uses {@link Input#total()} so deltas remain correct across buffer refills.
 */
public class FieldByteAccumulator {

    @FunctionalInterface
    public interface InputAction<T> {
        T run();
    }

    private final Map<String, Long> bytesByField = new LinkedHashMap<>();

    /**
     * Runs {@code writeAction} and adds the number of bytes written to {@code field}.
     *
     * @return bytes written by this call
     */
    public int write(Output output, String field, Runnable writeAction) {
        int before = output.position();
        writeAction.run();
        int written = output.position() - before;
        accumulate(field, written);
        return written;
    }

    /**
     * Runs {@code readAction} and adds the number of bytes read to {@code field}.
     *
     * @return the value produced by {@code readAction}
     */
    public <T> T read(Input input, String field, InputAction<T> readAction) {
        long before = input.total();
        T value = readAction.run();
        long read = input.total() - before;
        accumulate(field, read);
        return value;
    }

    /**
     * Like {@link #read(Input, String, InputAction)} but for a primitive {@code long},
     * so the result is not boxed.
     */
    public long readLong(Input input, String field, LongSupplier readAction) {
        long before = input.total();
        long value = readAction.getAsLong();
        accumulate(field, input.total() - before);
        return value;
    }

    private void accumulate(String field, long bytes) {
        bytesByField.merge(field, bytes, Long::sum);
    }

    /** Unmodifiable view of cumulative bytes per field. */
    public Map<String, Long> snapshot() {
        return Collections.unmodifiableMap(bytesByField);
    }

    public long total() {
        long sum = 0;
        for (long v : bytesByField.values()) {
            sum += v;
        }
        return sum;
    }

    public void reset() {
        bytesByField.clear();
    }

    /**
     * Adds {@code other}'s field totals into this instance. Used when Kryo is rebuilt
     * on a V4 CLOSE/HEADER so accounting survives a codec-epoch reset.
     */
    public void merge(FieldByteAccumulator other) {
        if (other == null || other == this) {
            return;
        }
        for (Entry<String, Long> entry : other.bytesByField.entrySet()) {
            bytesByField.merge(entry.getKey(), entry.getValue(), Long::sum);
        }
    }

    /** Dumps all accumulated field byte counts and the total to {@link System#out}. */
    public void dump() {
        dump(System.out);
    }

    /** Dumps all accumulated field byte counts and the total to {@code out}. */
    public void dump(PrintStream out) {
        out.println("FieldByteAccumulator:");
        if (bytesByField.isEmpty()) {
            out.println("  (empty)");
            return;
        }
        long sum = 0;
        for (Entry<String, Long> entry : bytesByField.entrySet()) {
            long bytes = entry.getValue();
            sum += bytes;
            out.println("  " + entry.getKey() + "=" + bytes);
        }
        out.println("  total=" + sum);
    }

    @Override
    public String toString() {
        return bytesByField.toString();
    }
}
