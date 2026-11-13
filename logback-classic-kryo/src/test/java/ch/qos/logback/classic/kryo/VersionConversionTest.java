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
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.Encoder;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.minlog.Log;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static ch.qos.logback.classic.kryo.KryoClassicTestConstants.TRANSLATOR_INPUT_PREFIX;
import static ch.qos.logback.core.kryo.KryoCoreConstants.KRYO_UNSAFE_PROPERTY_KEY;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Decode a Custom Kryo stream and replay every event through a FileAppender
 * with a full PatternLayout dump.
 */
@Disabled
public class VersionConversionTest {

    static final String DATE_0 = "2026-10-01";

    static final String DUMP_PATTERNX =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} %sn [%thread] %-5level %logger %marker %mdc %kvp - %message%n%caller{2}";

    static final String DUMP_PATTERN = "%caller{2}";


//    static final String DUMP_PATTERN =
//            "%mdc %kvp - %message%n";

    @BeforeAll
    public static void beforeAll() {
        System.setProperty(KRYO_UNSAFE_PROPERTY_KEY, "false");
        Log.INFO();
    }

    @AfterAll
    public static void afterAll() {
        System.clearProperty(KRYO_UNSAFE_PROPERTY_KEY);
    }

    static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of("Vanilla-to-log", DATE_0, EncoderVersionTag.VANILLA,  EncoderVersionTag.PATTERN_VANILLA),

                Arguments.of("V3-to-log", DATE_0, EncoderVersionTag.CUSTOM_V3,  EncoderVersionTag.PATTERN_V3),
                //Arguments.of("V3-to-V4", DATE_0, EncoderVersionTag.CUSTOM_V3,  EncoderVersionTag.CUSTOM_V4),
                Arguments.of("V4-to-log", DATE_0, EncoderVersionTag.CUSTOM_V4,  EncoderVersionTag.PATTERN_V4),
                //Arguments.of("V4-to-V5", DATE_0, EncoderVersionTag.CUSTOM_V4,  EncoderVersionTag.CUSTOM_V5),
                Arguments.of("V5-to-log", DATE_0, EncoderVersionTag.CUSTOM_V5,  EncoderVersionTag.PATTERN_V5)

        );
    }

    @ParameterizedTest(name = "type {0}")
    @MethodSource("parameters")
    public void dumpCustomKryoViaFileAppender(String name, String date, EncoderVersionTag decoderType, EncoderVersionTag encoderType) throws IOException {

        String inputName = TRANSLATOR_INPUT_PREFIX + date + decoderType.toSuffix();
        String outputName = TRANSLATOR_INPUT_PREFIX + date + encoderType.toSuffix();

        File inputFile = new File(inputName);
        assertTrue(inputFile.isFile(), "missing input file: " + inputFile.getAbsolutePath());

        File outputFile = new File(outputName);

        LoggerContext loggerContext = new LoggerContext();
        loggerContext.setName("FullDumpTest");
        loggerContext.setMDCAdapter(new LogbackMDCAdapter());


        Encoder<ILoggingEvent> encoder = makeEncoderFromEncoderVersionTag(encoderType);

        FileAppender<ILoggingEvent> fileAppender = buildFileAppender(loggerContext, encoder, outputFile.getPath());
        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.TRACE);
        root.addAppender(fileAppender);

        KryoDecoder decoder = new KryoDecoder(inputFile);
        KryoDecoder.DecodeStats stats = decoder.decodeAndAppend(loggerContext);

        fileAppender.stop();
        loggerContext.stop();

        System.out.println(stats);

        assertTrue(stats.count() > 0, "expected at least one event in " + inputFile.getName());
        assertTrue(outputFile.isFile() && outputFile.length() > 0,
                "expected non-empty dump file: " + outputFile.getAbsolutePath());
        System.out.println("Wrote " + stats.count() + " events to " + outputFile.getAbsolutePath()
                + " (decodeNanos=" + stats.decodeNanos()
                + ", consumeNanos=" + stats.consumeNanos() + ")");

        Serializer<LoggingEventKryoVO> serializer = decoder.kryo.getSerializer(LoggingEventKryoVO.class);
        if(serializer instanceof FieldByteAccumulatorAware) {
            FieldByteAccumulatorAware fieldByteAccumulatorAware = (FieldByteAccumulatorAware) serializer;
            System.out.println("Field byte accumulator dump for " + serializer.getClass().getSimpleName() + ":");
            fieldByteAccumulatorAware.getFieldByteAccumulator().dump(System.out);
        } else {
            System.out.println("Serializer " + serializer.getClass().getSimpleName() + " does not implement FieldByteAccumulatorAware");
        }
    }

    private Encoder<ILoggingEvent> makeEncoderFromEncoderVersionTag(EncoderVersionTag encoderVersionTag) {
        if(encoderVersionTag.serializerType() == null) {
            return makePatternEncoder(new LoggerContext());
        }

        return  makeKryoEncoder(new LoggerContext(), encoderVersionTag);

    }

    private static PatternLayoutEncoder makePatternEncoder(LoggerContext loggerContext) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern(DUMP_PATTERN);
        encoder.start();
        return encoder;
    }


    private static KryoEncoder makeKryoEncoder(LoggerContext loggerContext, EncoderVersionTag headerTuple) {

        KryoEncoder kryoEncoder = new KryoEncoder();
        kryoEncoder.setContext(loggerContext);
        kryoEncoder.setSerializerType(headerTuple.serializerType());
        kryoEncoder.setStreamVersion(headerTuple.streamVersion());

        kryoEncoder.start();
        return kryoEncoder;
    }


    private static FileAppender<ILoggingEvent> buildFileAppender(LoggerContext loggerContext, Encoder<ILoggingEvent> encoder, String filename) {

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(loggerContext);
        fileAppender.setName("FILE");
        fileAppender.setFile(filename);
        fileAppender.setAppend(false);
        fileAppender.setEncoder(encoder);
        fileAppender.start();
        return fileAppender;
    }
}
