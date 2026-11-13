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
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyVO;
import ch.qos.logback.core.testUtil.RandomUtil;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.KryoException;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.minlog.Log;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.helpers.BasicMarkerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static ch.qos.logback.core.kryo.KryoCoreConstants.KRYO_UNSAFE_PROPERTY_KEY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoggingEventKryoVOSerializerTest {


    LoggerContext loggerContext = new LoggerContext();
    LoggerContextVO loggerContextVO;
    int diff = RandomUtil.getPositiveInt();
    static int MAX_BUFFER_SIZE = 1024 * 64;

    byte[] buffer = new byte[MAX_BUFFER_SIZE];
    Output output = new Output(buffer);

    IMarkerFactory markerFactory = new BasicMarkerFactory();
    FileOutputStream fileOutputStream;

    static int LOOP_COUNT = 100;

    @BeforeAll
    public static void beforeAll() {
        System.setProperty(KRYO_UNSAFE_PROPERTY_KEY, "false");
        Log.set(Log.LEVEL_DEBUG);
    }

    @AfterAll
    public static void afterAll() {
        System.clearProperty(KRYO_UNSAFE_PROPERTY_KEY);
    }

    @BeforeEach
    public void setUp() throws FileNotFoundException {
        this.loggerContext.setName("LoggerContextKryoVOSerializerTest");
        this.loggerContextVO = loggerContext.getLoggerContextRemoteView();
    }

    private void createDirs(String filename) {
        File file = new File(filename);
        file.getParentFile().mkdirs();
    }

    static Stream<Arguments> customKryoFactories() {
        return Stream.of(
                Arguments.of("Vanilla", (Supplier<Kryo>) KryoBuilder::buildLoggerEventKryoVO_Vanilla),
                Arguments.of("V3", (Supplier<Kryo>) KryoBuilder::buildLoggerEventKryoVO_CustomV3),
                Arguments.of("V4", (Supplier<Kryo>) KryoBuilder::buildLoggerEventKryoVO_CustomV4),
                Arguments.of("V5", (Supplier<Kryo>) KryoBuilder::buildLoggerEventKryoVO_CustomV5)
        );
    }

    byte[] encode(Kryo kryo, LoggingEventKryoVO loggingEventKryoVO) {
        output.setPosition(0);
        kryo.writeObject(output, loggingEventKryoVO);
        byte[] data = new byte[output.position()];
        System.arraycopy(buffer, 0, data, 0, output.position());
        return data;
    }


    List<LoggingEventKryoVO> decodeAsListFromInputStream(Kryo kryo, InputStream inputStream) throws IOException {
        List<LoggingEventKryoVO> loggingEventVOList = new ArrayList<>();
        Input input = new Input(inputStream);
        try {
            while (input.available() > 0) {
                //System.out.println("Decoding event...");
                LoggingEventKryoVO loggingEventKryoVO = kryo.readObject(input, LoggingEventKryoVO.class);
                //System.out.println("Decoded loggingEventKryoVO: " + loggerContextVO);
                loggingEventVOList.add(loggingEventKryoVO);
            }
            System.out.println("Decoded " + loggingEventVOList.size() + " events.");
            return loggingEventVOList;
        } catch (EOFException | KryoException e) {
            // expected
            e.printStackTrace();
            return loggingEventVOList;
        } finally {
            input.close();
        }
    }


    List<LoggingEventKryoVO> decodeAsListFromByteBuffer(Kryo kryo, byte[] buffer) throws IOException {
        return decodeAsListFromInputStream(kryo, new ByteArrayInputStream(buffer));
    }

    List<LoggingEventKryoVO> decodeAsList(Kryo kryo, String filename) {
        try {
            return decodeAsListFromInputStream(kryo, new BufferedInputStream(new FileInputStream(filename)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @ParameterizedTest(name = "Custom {0}")
    @MethodSource("customKryoFactories")
    public void testEncode(String versionLabel, Supplier<Kryo> kryoFactory) throws IOException {
        String outputTarget = KryoClassicTestConstants.TEST_OUTPUT_DIR_PREFIX + diff + "/loggingEventVO_"+versionLabel+".kryo";
        createDirs(outputTarget);
        this.fileOutputStream = new FileOutputStream(outputTarget);
        List<LoggingEventKryoVO> witnessList = new ArrayList<>();
        Kryo kryoOut = kryoFactory.get();
        Kryo kryoRead =  kryoFactory.get();
        InputStream inputStream = filenameToInputStream(outputTarget);
        loopCompare(kryoOut, fileOutputStream, kryoRead, inputStream, new BasicEventSupplier(loggerContextVO));
    }

    InputStream filenameToInputStream(String filename) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(filename));
    }



    void loopCompare(Kryo kryoOut, FileOutputStream fileOutputStream, Kryo kryoRead, InputStream inputStream, Supplier<LoggingEventKryoVO> supplier) throws IOException {
        List<LoggingEventKryoVO> witnessList = new ArrayList<>();

        for (int i = 0; i < LOOP_COUNT; i++) {
            LoggingEventKryoVO loggingEventKryoVO = supplier.get();
            byte[] bytes = encode(kryoOut, loggingEventKryoVO);
            fileOutputStream.write(bytes);
            witnessList.add(loggingEventKryoVO);
        }
        verify(kryoRead, inputStream, witnessList);
    }

    @ParameterizedTest(name = "Custom {0}")
    @MethodSource("customKryoFactories")
    public void nullMessageTest(String versionLabel, Supplier<Kryo> kryoFactory) throws IOException {
        Kryo kryo = kryoFactory.get();
        NullMessageEventSupplier supplier = new NullMessageEventSupplier(loggerContextVO);
        serializeSingleEventAndCompare(kryo, supplier);
    }

    @ParameterizedTest(name = "Custom {0}")
    @MethodSource("customKryoFactories")
    public void largeMessageTest(String versionLabel, Supplier<Kryo> kryoFactory) throws IOException {
        Kryo kryo = kryoFactory.get();
        LargeMessageEventSupplier supplier = new LargeMessageEventSupplier(loggerContextVO);
        serializeSingleEventAndCompare(kryo, supplier);
    }

    @ParameterizedTest(name = "Custom {0}")
    @MethodSource("customKryoFactories")
    public void largeStringArgumentTest(String versionLabel, Supplier<Kryo> kryoFactory) throws IOException {
        Kryo kryo = kryoFactory.get();
        serializeSingleEventAndCompare(kryo, new LargeStringArgSupplier(loggerContextVO));
    }

    private void verify(Kryo kryo, InputStream inputStream, List<LoggingEventKryoVO> witnessList) throws IOException {

        List<LoggingEventKryoVO> list = decodeAsListFromInputStream(kryo, inputStream);
        assertEquals(witnessList.size(), list.size());

        for (int i = 0; i < witnessList.size(); i++) {

            LoggingEventKryoVO expected = witnessList.get(i);
            LoggingEventKryoVO actual = list.get(i);
            //System.out.println("expected[" + i + "]=" + expected);
            //System.out.println("actual[" + i + "]=" + actual);

            assertEquals(expected, actual, "at index " + i);

        }

        assertEquals(witnessList, list);
    }

    public void serializeSingleEventAndCompare(Kryo kryo, Supplier<LoggingEventKryoVO> supplier) throws IOException {

        LoggingEventKryoVO eventKryoVO = supplier.get();

        // serialize to bytes
        output.setPosition(0);
        kryo.writeObject(output, eventKryoVO);
        byte[] bytes = new byte[output.position()];
        System.arraycopy(buffer, 0, bytes, 0, output.position());

        // deserialize from bytes
        Input input = new Input(new ByteArrayInputStream(bytes));
        LoggingEventKryoVO result = kryo.readObject(input, LoggingEventKryoVO.class);
        input.close();


        // assert fields
        assertEquals(eventKryoVO.getThreadName(), result.getThreadName());
        assertEquals(eventKryoVO.getLoggerName(), result.getLoggerName());
        assertEquals(eventKryoVO.getLevel().levelInt, result.getLevel().levelInt);
        assertEquals(eventKryoVO.getMessage(), result.getMessage());
        assertArrayEquals(eventKryoVO.getArgumentArray(), result.getArgumentArray());
        assertEquals(eventKryoVO.getLoggerContextVO(), result.getLoggerContextVO());
        assertEquals(eventKryoVO.getMarkerList(), result.getMarkerList());
        assertEquals(eventKryoVO.getThrowableProxy(), result.getThrowableProxy());
        if (eventKryoVO.getThrowableProxy() != null) {
            // ThrowableProxyVO.equals does not include message; verify it explicitly
            assertEquals(eventKryoVO.getThrowableProxy().getMessage(), result.getThrowableProxy().getMessage());
            assertEquals(eventKryoVO.getThrowableProxy().getClassName(), result.getThrowableProxy().getClassName());
            assertEquals(eventKryoVO.getThrowableProxy().getCommonFrames(), result.getThrowableProxy().getCommonFrames());
            assertEquals(eventKryoVO.getThrowableProxy().isCyclic(), result.getThrowableProxy().isCyclic());
        }
        assertEquals(eventKryoVO.getKeyValuePairs(), result.getKeyValuePairs());

        assertEquals(eventKryoVO, result);
    }


    @ParameterizedTest(name = "Custom {0}")
    @MethodSource("customKryoFactories")
    public void testBasicEvent_Custom(String versionLabel, Supplier<Kryo> kryoFactory) throws IOException {
        Kryo kryo = kryoFactory.get();
        BasicEventSupplier supplier = new BasicEventSupplier(loggerContextVO);
        serializeSingleEventAndCompare(kryo, supplier);
    }


    @ParameterizedTest(name = "Custom {0}")
    @MethodSource("customKryoFactories")
    public void testFullEvent_Custom(String versionLabel, Supplier<Kryo> kryoFactory) throws IOException {
        Kryo kryo = kryoFactory.get();
        FullEventSupplier supplier = new FullEventSupplier(loggerContextVO, markerFactory);
        serializeSingleEventAndCompare(kryo, supplier);
    }

    /**
     * JDK stack frames typically have a null module name. V4 must not reject those
     * or encode() aborts after stateful codecs (e.g. MultiEventStringSerializer)
     * have already advanced, desynchronizing the rest of the stream.
     */
    @Test
    public void v4RoundTrip_throwableWithNullModuleNames() throws IOException {
        Kryo kryo = KryoBuilder.buildLoggerEventKryoVO_CustomV4();
        serializeSingleEventAndCompare(kryo, new FullEventSupplier(loggerContextVO, markerFactory));
    }

    /**
     * Fluent {@code LOGGER.atInfo().addKeyValue(...).log()} has a null message
     * and a key-value list.
     */
    @ParameterizedTest(name = "Custom {0}")
    @MethodSource("customKryoFactories")
    public void nullMessageWithKeyValuePairs(String versionLabel, Supplier<Kryo> kryoFactory) throws IOException {
        serializeSingleEventAndCompare(kryoFactory.get(),
                new NullMessageWithKvpSupplier(loggerContextVO));
    }

    /**
     * V5 packs optional-field presence into one byte and E3-packs timestamp/nanos.
     * V5 payloads are therefore strictly smaller than V4 for the same event.
     *
     * <p>Events with a throwable are compared over a stream: V5 writes frame class names
     * with {@link BasicStringSerializer}, so the first sighting of a stack trace can be
     * larger than V4's package-prefix encoding. Cached frames make V5 smaller again.
     */
    @Test
    public void v5PayloadIsSmallerThanV4() {
        LoggingEventKryoVO sparse = new BasicEventSupplier(loggerContextVO).get();
        LoggingEventKryoVO nullMessage = new NullMessageEventSupplier(loggerContextVO).get();
        assertTrue(encode(KryoBuilder.buildLoggerEventKryoVO_CustomV4(), sparse).length
                > encode(KryoBuilder.buildLoggerEventKryoVO_CustomV5(), sparse).length);
        assertTrue(encode(KryoBuilder.buildLoggerEventKryoVO_CustomV4(), nullMessage).length
                > encode(KryoBuilder.buildLoggerEventKryoVO_CustomV5(), nullMessage).length);

        Kryo kryoV4 = KryoBuilder.buildLoggerEventKryoVO_CustomV4();
        Kryo kryoV5 = KryoBuilder.buildLoggerEventKryoVO_CustomV5();
        FullEventSupplier fullEventSupplier = new FullEventSupplier(loggerContextVO, markerFactory);
        int v4Total = 0;
        int v5Total = 0;
        for (int i = 0; i < LOOP_COUNT; i++) {
            LoggingEventKryoVO full = fullEventSupplier.get();
            v4Total += encode(kryoV4, full).length;
            v5Total += encode(kryoV5, full).length;
        }
        assertTrue(v4Total > v5Total, "V4=" + v4Total + " V5=" + v5Total);
    }

    @ParameterizedTest(name = "Custom {0}")
    @MethodSource("customKryoFactories")
    public void callerDataPresentAndAbsent(String versionLabel, Supplier<Kryo> kryoFactory) throws IOException {
        Kryo kryo = kryoFactory.get();
        LoggingEventKryoVO without = new BasicEventSupplier(loggerContextVO).get();
        LoggingEventKryoVO with = new LoggingEventKryoVO.Builder()
                .withThreadName("main")
                .withLoggerContextVO(loggerContextVO)
                .withLoggerName("test")
                .withLevelInt(Level.INFO.levelInt)
                .withMessage("with caller")
                .withCallerData(new StackTraceElement[] {
                        new StackTraceElement("com.example.App", "main", "App.java", 42),
                        new StackTraceElement("com.example.App", "run", "App.java", 17),
                })
                .build();

        serializeSingleEventAndCompare(kryo, () -> without);
        serializeSingleEventAndCompare(kryo, () -> with);

        int withoutLen = encode(kryoFactory.get(), without).length;
        int withLen = encode(kryoFactory.get(), with).length;
        assertTrue(withLen > withoutLen, versionLabel);
    }

    @Test
    public void packEpochRoundTrip() {
        long timestamp = 1_700_000_000_123L;
        int nanoseconds = 123_456_789;
        long packed = LoggingEventKryoVOSerializerV5.packEpoch(timestamp, nanoseconds);
        assertEquals(timestamp, LoggingEventKryoVOSerializerV5.unpackTimestamp(packed));
        assertEquals(nanoseconds, LoggingEventKryoVOSerializerV5.unpackNanoseconds(packed));
    }

    @Test
    public void packEpochPutsNanoOfSecondInLow30Bits() {
        int nanoseconds = 123_456_789;
        long packed = LoggingEventKryoVOSerializerV5.packEpoch(5_000, nanoseconds);
        assertEquals(nanoseconds, packed & LoggingEventKryoVOSerializerV5.NANO_MASK);
        assertEquals(5L, packed >>> LoggingEventKryoVOSerializerV5.NANO_BITS);
    }

    @Test
    public void v5SequentialEventsPreserveTimestampAndNanos() throws IOException {
        Kryo kryo = KryoBuilder.buildLoggerEventKryoVO_CustomV5();
        long ts = 1_700_000_000_123L;
        LoggingEventKryoVO first = timeEvent(ts, 123_000_001);
        LoggingEventKryoVO sameMs = timeEvent(ts, 123_000_002);
        LoggingEventKryoVO nextMs = timeEvent(ts + 1, 124_000_001);

        Output out = new Output(2048, -1);
        kryo.writeObject(out, first);
        kryo.writeObject(out, sameMs);
        kryo.writeObject(out, nextMs);
        out.flush();

        Input in = new Input(out.toBytes());
        assertEquals(first, kryo.readObject(in, LoggingEventKryoVO.class));
        assertEquals(sameMs, kryo.readObject(in, LoggingEventKryoVO.class));
        assertEquals(nextMs, kryo.readObject(in, LoggingEventKryoVO.class));
    }

    private LoggingEventKryoVO timeEvent(long timestamp, int nanoseconds) {
        return new LoggingEventKryoVO.Builder()
                .withLoggerContextVO(loggerContextVO)
                .withLoggerName("test")
                .withThreadName("main")
                .withLevelInt(Level.INFO.levelInt)
                .withMessage("t")
                .withTimestamp(timestamp)
                .withNanoseconds(nanoseconds)
                .build();
    }



    // ==============================================================================
    static class BasicEventSupplier implements Supplier<LoggingEventKryoVO>  {
        private int counter = 0;
        LoggerContextVO  loggerContextVO;

        BasicEventSupplier(LoggerContextVO loggerContextVO) {
            this.loggerContextVO = loggerContextVO;
        }

        @Override
        public LoggingEventKryoVO get() {
            return new LoggingEventKryoVO.Builder().withThreadName("main")
                    .withLoggerContextVO(loggerContextVO).withLoggerName("test")
                    .withLevelInt(Level.INFO.levelInt).withMessage("Test message {}").withArgumentArray(new Object[]{counter++})
                    .build();
        }
    }

    static class LargeMessageEventSupplier implements Supplier<LoggingEventKryoVO>  {
        private int counter = 0;
        LoggerContextVO  loggerContextVO;

        LargeMessageEventSupplier(LoggerContextVO loggerContextVO) {
            this.loggerContextVO = loggerContextVO;
        }

        @Override
        public LoggingEventKryoVO get() {
            String largeMessage = "This is a large message ".repeat(100);
            return new LoggingEventKryoVO.Builder().withThreadName("main")
                    .withLoggerContextVO(loggerContextVO).withLoggerName("test")
                    .withLevelInt(Level.INFO.levelInt).withMessage(largeMessage).withArgumentArray(new Object[]{counter++})
                    .build();
        }
    }

    static class LargeStringArgSupplier implements Supplier<LoggingEventKryoVO> {
        LoggerContextVO loggerContextVO;

        LargeStringArgSupplier(LoggerContextVO loggerContextVO) {
            this.loggerContextVO = loggerContextVO;
        }

        @Override
        public LoggingEventKryoVO get() {
            return new LoggingEventKryoVO.Builder().withThreadName("main")
                    .withLoggerContextVO(loggerContextVO).withLoggerName("test")
                    .withLevelInt(Level.INFO.levelInt).withMessage("arg {}")
                    .withArgumentArray(new Object[] { "z".repeat(300) })
                    .build();
        }
    }

    static class NullMessageEventSupplier implements Supplier<LoggingEventKryoVO>  {
        LoggerContextVO  loggerContextVO;

        NullMessageEventSupplier(LoggerContextVO loggerContextVO) {
            this.loggerContextVO = loggerContextVO;
        }

        @Override
        public LoggingEventKryoVO get() {
            return new LoggingEventKryoVO.Builder().withThreadName("main")
                    .withLoggerContextVO(loggerContextVO).withLoggerName("test")
                    .withLevelInt(Level.INFO.levelInt).withMessage(null)
                    .build();
        }
    }

    /** Fluent {@code atInfo().addKeyValue(...).log()} — null message, KVP present. */
    static class NullMessageWithKvpSupplier implements Supplier<LoggingEventKryoVO> {
        LoggerContextVO loggerContextVO;

        NullMessageWithKvpSupplier(LoggerContextVO loggerContextVO) {
            this.loggerContextVO = loggerContextVO;
        }

        @Override
        public LoggingEventKryoVO get() {
            ArrayList<KeyValuePair> keyValuePairs = new ArrayList<>();
            keyValuePairs.add(new KeyValuePair("id", "42"));
            keyValuePairs.add(new KeyValuePair("sha1", "abc"));
            return new LoggingEventKryoVO.Builder().withThreadName("main")
                    .withLoggerContextVO(loggerContextVO).withLoggerName("test")
                    .withLevelInt(Level.INFO.levelInt).withMessage(null)
                    .withKeyValuePairList(keyValuePairs)
                    .build();
        }
    }

    static class FullEventSupplier implements Supplier<LoggingEventKryoVO>  {
        private int counter = 0;
        LoggerContextVO  loggerContextVO;
        IMarkerFactory markerFactory;

        FullEventSupplier(LoggerContextVO loggerContextVO, IMarkerFactory markerFactory) {
            this.loggerContextVO = loggerContextVO;
            this.markerFactory = markerFactory;
        }

        @Override
        public LoggingEventKryoVO get() {
            Throwable throwable = new Throwable("Test exception " + counter);
            ThrowableProxy throwableProxy = new ThrowableProxy(throwable);
            ThrowableProxyVO throwableProxyVO = ThrowableProxyVO.build(throwableProxy);
            ArrayList<Marker> markers = new ArrayList<>();
            markers.add(markerFactory.getMarker("MARKER_A"));
            markers.add(markerFactory.getMarker("MARKER_B"));

            ArrayList<KeyValuePair> keyValuePairs = new ArrayList<>();
            keyValuePairs.add(new KeyValuePair("key", "value"));

            return new LoggingEventKryoVO.Builder().withThreadName("main")
                    .withLoggerContextVO(loggerContextVO).withLoggerName("test")
                    .withLevelInt(Level.INFO.levelInt).withMessage("Test message {}").withArgumentArray(new Object[]{counter++})
                    .withTimestamp(123).withNanoseconds(123456789).withSequenceNumber(9876543210L)
                    .withMarkerList(markers)
                    .withThrowableProxy(throwableProxyVO)
                    .withKeyValuePairList(keyValuePairs)
                    .build();
        }
    }

}
