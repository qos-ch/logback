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

import static org.assertj.core.api.Assertions.assertThat;

public class StackTraceElementSerializerTest {

    private static final int CACHED_HEADER_BYTES = 2;

    private Kryo kryo;
    private StackTraceElementSerializer serializer;

    @BeforeEach
    public void setUp() {
        kryo = new Kryo();
        serializer = new StackTraceElementSerializer();
    }

    @Test
    public void roundTripKeepsNullModuleFieldsAndNativeLine() {
        StackTraceElement original = new StackTraceElement(
                null, null, null,
                "ch.qos.logback.translator.web.filter.LeakyBucketFilter",
                "innerFilter", null, -2);

        assertThat(writeThenRead(original)).isEqualTo(original);
    }

    @Test
    public void equalFrameSecondWriteIsCached() {
        StackTraceElement first = frame("doFilter", 109);
        StackTraceElement again = frame("doFilter", 109);

        Output out = new Output(512, -1);
        serializer.write(kryo, out, first);
        int afterFirst = out.position();
        serializer.write(kryo, out, again);
        assertThat(out.position() - afterFirst).isEqualTo(CACHED_HEADER_BYTES);
        out.flush();

        StackTraceElementSerializer reader = new StackTraceElementSerializer();
        Input in = new Input(out.toBytes());
        StackTraceElement readFirst = reader.read(kryo, in, StackTraceElement.class);
        StackTraceElement readSecond = reader.read(kryo, in, StackTraceElement.class);
        assertThat(readFirst).isEqualTo(first);
        assertThat(readSecond).isEqualTo(first).isSameAs(readFirst);
    }

    @Test
    public void sameClassDifferentLineReusesNameCaches() {
        byte[] cold = write(frame("innerFilter", 125));
        byte[] sameMethodOtherLine = write(frame("innerFilter", 149));

        assertThat(sameMethodOtherLine.length).isLessThan(cold.length);
        assertThat(sameMethodOtherLine.length).isGreaterThan(CACHED_HEADER_BYTES);
    }

    private StackTraceElement frame(String method, int line) {
        return new StackTraceElement(
                "org.apache.catalina.core.ApplicationFilterChain",
                method, "ApplicationFilterChain.java", line);
    }

    private StackTraceElement writeThenRead(StackTraceElement frame) {
        StackTraceElementSerializer reader = new StackTraceElementSerializer();
        return reader.read(kryo, new Input(write(frame)), StackTraceElement.class);
    }

    private byte[] write(StackTraceElement frame) {
        Output out = new Output(512, -1);
        serializer.write(kryo, out, frame);
        out.flush();
        return out.toBytes();
    }
}
