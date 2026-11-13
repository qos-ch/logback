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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusListenerConfigHelper;
import com.esotericsoftware.kryo.kryo5.Serializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static ch.qos.logback.core.kryo.KryoCoreConstants.KRYO_UNSAFE_PROPERTY_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class KryoEncoderTest {


    static String EXISTING_V4_FILENAME = "2026-09-26-custom-v4.kryo";

    ///KryoEncoder.SerializerType serializerType = KryoEncoder.SerializerType.Vanilla;
    //byte streamVersion = KryoClassicConstants.STREAM_VERSION;

    LoggerContext loggerContext = new LoggerContext();
    int diff = RandomUtil.getPositiveInt();
    LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();

    static Stream<Arguments> kryoFactories() {
        return Stream.of(
                Arguments.of("Vanilla",  KryoEncoder.SerializerType.Vanilla, KryoClassicConstants.STREAM_VERSION_0),
                Arguments.of("V3", KryoEncoder.SerializerType.Custom, KryoClassicConstants.STREAM_VERSION_3),
                Arguments.of("V4", KryoEncoder.SerializerType.Custom, KryoClassicConstants.STREAM_VERSION_4),
                Arguments.of("V5", KryoEncoder.SerializerType.Custom, KryoClassicConstants.STREAM_VERSION_5)
        );
    }

    @BeforeAll
    public static void beforeAll() {
        System.setProperty(KRYO_UNSAFE_PROPERTY_KEY, "false");
    }

    @AfterAll
    public static void afterAll() {
        System.clearProperty(KRYO_UNSAFE_PROPERTY_KEY);
    }



    @BeforeEach
    public void setUp() {
        loggerContext.setName("KryoEncoderTest");
        loggerContext.setMDCAdapter(logbackMDCAdapter);
        StatusListenerConfigHelper.addOnConsoleListenerInstance(loggerContext, new OnConsoleStatusListener());
    }

    FileAppender<ILoggingEvent> setupFileAppender(String filename, KryoEncoder.SerializerType serializerType, byte streamVersion) {
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(loggerContext);
        fileAppender.setName("FILE");
        fileAppender.setFile(filename);

        KryoEncoder kryoEncoder = new KryoEncoder();
        kryoEncoder.setSerializerType(serializerType);
        kryoEncoder.setStreamVersion(streamVersion);
        kryoEncoder.setContext(loggerContext);
        kryoEncoder.start();

        fileAppender.setEncoder(kryoEncoder);
        fileAppender.start();

        return fileAppender;

    }

    @ParameterizedTest(name = "Encoder {0}")
    @MethodSource("kryoFactories")
    public void testEncode(String label,  KryoEncoder.SerializerType serializerType,byte streamVersion) throws IOException {
        runEncodeDecodeRoundTrip(label, serializerType, streamVersion);
    }


    void runEncodeDecodeRoundTrip(String label, KryoEncoder.SerializerType serializerType, byte streamVersion) throws IOException {
        Logger logger = loggerContext.getLogger(KryoEncoderTest.class);

        String filename = KryoClassicTestConstants.TEST_OUTPUT_DIR_PREFIX + diff + "/" + label + ".kryo";
        List<LoggingEventKryoVO> witnessList = new ArrayList<>();

        FileAppender<ILoggingEvent> appender = setupFileAppender(filename, serializerType, streamVersion);

        final String message = "TEST MESSAGE {}";

        for (int i = 0; i < 1; i++) {
            Object[] argArray = new Object[]{i};
            LoggingEvent loggingEvent = new LoggingEvent("x", logger, Level.INFO, message,  null, argArray);
            LoggingEventKryoVO loggerEventKryoVO = new LoggingEventKryoVO(loggingEvent);
            appender.doAppend(loggingEvent);
            witnessList.add(loggerEventKryoVO);
        }
        appender.stop();


        verify(filename, witnessList, serializerType, streamVersion);

    }



    void verify(String filename, List<LoggingEventKryoVO> witnessList, KryoEncoder.SerializerType serializerType, byte streamVersion) throws IOException {
        KryoDecoder decoder = new KryoDecoder(new File(filename));
        assertEquals(serializerType, decoder.getSerializerType());
        assertEquals(streamVersion, decoder.getStreamVersion());
        List<LoggingEventKryoVO> list =  decoder.decodeAsList();

        assertEquals(witnessList.size(), list.size());

        for (int i = 0; i < witnessList.size(); i++) {

            LoggingEventKryoVO expected = witnessList.get(i);
            LoggingEventKryoVO actual = list.get(i);
            System.out.println("expected[" + i + "]=" + expected);
            System.out.println("actual[" + i + "]=" + actual);

            assertEquals(expected, actual, "at index " + i);

        }

        assertEquals(witnessList, list);

        Serializer<LoggingEventKryoVO> serializer = decoder.kryo.getSerializer(LoggingEventKryoVO.class);
        if (serializer instanceof FieldByteAccumulatorAware) {
            FieldByteAccumulatorAware aware = (FieldByteAccumulatorAware) serializer;
            assertTrue(aware.getFieldByteAccumulator().total() > 0,
                    "field byte accumulator should retain totals after decode");
        }
    }

    @Test
    public void v4FramesEventsAndResetsEpoch() throws IOException {
        Logger logger = loggerContext.getLogger(KryoEncoderTest.class);
        String filename = KryoClassicTestConstants.TEST_OUTPUT_DIR_PREFIX + diff + "/V4-framed.kryo";
        java.nio.file.Files.createDirectories(java.nio.file.Path.of(filename).getParent());

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(loggerContext);
        fileAppender.setName("FILE");
        fileAppender.setFile(filename);

        KryoEncoder encoder = new KryoEncoder();
        encoder.setSerializerType(KryoEncoder.SerializerType.Custom);
        encoder.setStreamVersion(KryoClassicConstants.STREAM_VERSION_4);
        encoder.setMaxEventsBeforeReset(4);
        encoder.setContext(loggerContext);
        encoder.start();

        fileAppender.setEncoder(encoder);
        fileAppender.start();

        List<LoggingEventKryoVO> witnessList = new ArrayList<>();
        final int n = 9;
        for (int i = 0; i < n; i++) {
            LoggingEvent loggingEvent = new LoggingEvent("x", logger, Level.INFO,
                    "TEST MESSAGE {}", null, new Object[]{i});
            fileAppender.doAppend(loggingEvent);
            witnessList.add(new LoggingEventKryoVO(loggingEvent));
        }
        fileAppender.stop();

        byte[] fileBytes = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(filename));
        // 4 + 4 + 1 events → two mid-stream CLOSE+HEADER plus footer CLOSE
        assertEquals(3, StreamFrame.countOccurrences(fileBytes, StreamFrame.CLOSE_CODE));
        assertEquals(3, StreamFrame.countOccurrences(fileBytes, KryoClassicConstants.STREAM_MAGIC));

        verify(filename, witnessList, KryoEncoder.SerializerType.Custom,
                KryoClassicConstants.STREAM_VERSION_4);
    }

    @Test
    public void v5FramesEventsAndResetsEpoch() throws IOException {
        Logger logger = loggerContext.getLogger(KryoEncoderTest.class);
        String filename = KryoClassicTestConstants.TEST_OUTPUT_DIR_PREFIX + diff + "/V5-framed.kryo";
        java.nio.file.Files.createDirectories(java.nio.file.Path.of(filename).getParent());

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(loggerContext);
        fileAppender.setName("FILE");
        fileAppender.setFile(filename);

        KryoEncoder encoder = new KryoEncoder();
        encoder.setSerializerType(KryoEncoder.SerializerType.Custom);
        encoder.setStreamVersion(KryoClassicConstants.STREAM_VERSION_5);
        encoder.setMaxEventsBeforeReset(4);
        encoder.setContext(loggerContext);
        encoder.start();

        fileAppender.setEncoder(encoder);
        fileAppender.start();

        List<LoggingEventKryoVO> witnessList = new ArrayList<>();
        final int n = 9;
        for (int i = 0; i < n; i++) {
            LoggingEvent loggingEvent = new LoggingEvent("x", logger, Level.INFO,
                    "TEST MESSAGE {}", null, new Object[]{i});
            fileAppender.doAppend(loggingEvent);
            witnessList.add(new LoggingEventKryoVO(loggingEvent));
        }
        fileAppender.stop();

        byte[] fileBytes = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(filename));
        assertEquals(3, StreamFrame.countOccurrences(fileBytes, StreamFrameV5.closeCode()));
        assertEquals(3, StreamFrame.countOccurrences(fileBytes, KryoClassicConstants.STREAM_MAGIC));

        verify(filename, witnessList, KryoEncoder.SerializerType.Custom,
                KryoClassicConstants.STREAM_VERSION_5);
    }

    @Test
    public void decoderReadsExistingV4Fixture() throws IOException {
        File v4 = new File(KryoClassicTestConstants.TRANSLATOR_INPUT_PREFIX + EXISTING_V4_FILENAME);
        // the fixture is a real application log, kept out of the repository
        assumeTrue(v4.isFile(), v4.getAbsolutePath());
        KryoDecoder decoder = new KryoDecoder(v4);
        assertEquals(KryoClassicConstants.STREAM_VERSION_4, decoder.getStreamVersion());
        List<LoggingEventKryoVO> events = decoder.decodeAsList();
        assertTrue(events.size() > 0);
    }

}
