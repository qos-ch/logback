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

import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static ch.qos.logback.core.kryo.KryoCoreConstants.KRYO_UNSAFE_PROPERTY_KEY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Guards the wire format: {@link GoldenEvents} encoded by {@link KryoEncoder} must give the
 * golden streams byte for byte, and the golden streams must decode to those events.
 *
 * <p>To rewrite the golden streams after a deliberate format change, run this test with
 * {@code -Dlogback.kryo.golden.write=true}.
 */
public class GoldenStreamTest {

    @BeforeAll
    public static void beforeAll() {
        System.setProperty(KRYO_UNSAFE_PROPERTY_KEY, "false");
    }

    @AfterAll
    public static void afterAll() {
        System.clearProperty(KRYO_UNSAFE_PROPERTY_KEY);
    }

    static Stream<EncoderVersionTag> tags() {
        return Stream.of(EncoderVersionTag.VANILLA, EncoderVersionTag.CUSTOM_V5);
    }

    static byte[] encode(EncoderVersionTag tag) throws IOException {
        KryoEncoder encoder = new KryoEncoder();
        encoder.setContext(new LoggerContext());
        encoder.setSerializerType(tag.serializerType());
        encoder.setStreamVersion(tag.streamVersion());
        encoder.start();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(encoder.headerBytes());
        for (LoggingEventKryoVO event : GoldenEvents.events()) {
            out.write(encoder.encode(event));
        }
        out.write(encoder.footerBytes());
        return out.toByteArray();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tags")
    public void encoderReproducesGoldenStream(EncoderVersionTag tag) throws IOException {
        byte[] actual = encode(tag);
        if (Boolean.getBoolean(GoldenEvents.WRITE_PROPERTY)) {
            Path target = Path.of(GoldenEvents.SOURCE_FOLDER, GoldenEvents.fileName(tag));
            Files.createDirectories(target.getParent());
            Files.write(target, actual);
            System.out.println("Wrote golden stream " + target.toAbsolutePath() + " (" + actual.length + " bytes)");
            return;
        }
        assertArrayEquals(GoldenEvents.readGolden(tag), actual, "wire format of " + tag + " changed");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tags")
    public void goldenStreamDecodes(EncoderVersionTag tag) throws IOException {
        Path dir = Path.of(KryoClassicTestConstants.TEST_OUTPUT_DIR_PREFIX, "golden");
        Files.createDirectories(dir);
        File file = dir.resolve(GoldenEvents.fileName(tag)).toFile();
        Files.write(file.toPath(), GoldenEvents.readGolden(tag));

        KryoDecoder decoder = new KryoDecoder(file);
        assertEquals(tag.serializerType(), decoder.getSerializerType());
        assertEquals(tag.streamVersion(), decoder.getStreamVersion());
        List<LoggingEventKryoVO> decoded = decoder.decodeAsList();
        assertEquals(GoldenEvents.events(), decoded);
    }
}
