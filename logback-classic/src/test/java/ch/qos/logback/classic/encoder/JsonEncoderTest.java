/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2023, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.classic.encoder;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.jsonTest.JsonLoggingEvent;
import ch.qos.logback.classic.jsonTest.JsonStringToLoggingEventMapper;
import ch.qos.logback.classic.jsonTest.ThrowableProxyComparator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusPrinter;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.helpers.BasicMarkerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// When running from an IDE, add the following on the command line
//
//          --add-opens ch.qos.logback.classic/ch.qos.logback.classic.jsonTest=ALL-UNNAMED
//
class JsonEncoderTest {

    int diff = RandomUtil.getPositiveInt();

    LoggerContext loggerContext = new LoggerContext();
    StatusChecker statusChecker = new StatusChecker(loggerContext);
    Logger logger = loggerContext.getLogger(JsonEncoderTest.class);

    JsonEncoder jsonEncoder = new JsonEncoder();

    BasicMarkerFactory markerFactory = new BasicMarkerFactory();

    Marker markerA = markerFactory.getMarker("A");

    Marker markerB = markerFactory.getMarker("B");

    ListAppender<ILoggingEvent> listAppender = new ListAppender();
    JsonStringToLoggingEventMapper stringToLoggingEventMapper = new JsonStringToLoggingEventMapper(markerFactory);

    LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();

    @BeforeEach
    void setUp() {
        loggerContext.setName("test_" + diff);
        loggerContext.setMDCAdapter(logbackMDCAdapter);

        jsonEncoder.setContext(loggerContext);
        jsonEncoder.start();

        listAppender.setContext(loggerContext);
        listAppender.start();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void smoke() throws JsonProcessingException {
        LoggingEvent event = new LoggingEvent("x", logger, Level.WARN, "hello", null, null);

        byte[] resultBytes = jsonEncoder.encode(event);
        String resultString = new String(resultBytes, StandardCharsets.UTF_8);
        //System.out.println(resultString);

        JsonLoggingEvent resultEvent = stringToLoggingEventMapper.mapStringToLoggingEvent(resultString);
        compareEvents(event, resultEvent);

    }

    @Test
    void contextWithProperties() throws JsonProcessingException {
        loggerContext.putProperty("k", "v");
        loggerContext.putProperty("k" + diff, "v" + diff);

        LoggingEvent event = new LoggingEvent("x", logger, Level.WARN, "hello", null, null);

        byte[] resultBytes = jsonEncoder.encode(event);
        String resultString = new String(resultBytes, StandardCharsets.UTF_8);
        // System.out.println(resultString);

        JsonLoggingEvent resultEvent = stringToLoggingEventMapper.mapStringToLoggingEvent(resultString);
        compareEvents(event, resultEvent);

    }

    private static void compareEvents(LoggingEvent event, JsonLoggingEvent resultEvent) {
        assertEquals(event.getSequenceNumber(), resultEvent.getSequenceNumber());
        assertEquals(event.getTimeStamp(), resultEvent.getTimeStamp());
        assertEquals(event.getLevel(), resultEvent.getLevel());
        assertEquals(event.getLoggerName(), resultEvent.getLoggerName());
        assertEquals(event.getThreadName(), resultEvent.getThreadName());
        assertEquals(event.getMarkerList(), resultEvent.getMarkerList());
        assertEquals(event.getMDCPropertyMap(), resultEvent.getMDCPropertyMap());
        assertTrue(compareKeyValuePairLists(event.getKeyValuePairs(), resultEvent.getKeyValuePairs()));

        assertEquals(event.getLoggerContextVO(), resultEvent.getLoggerContextVO());
        assertTrue(ThrowableProxyComparator.areEqual(event.getThrowableProxy(), resultEvent.getThrowableProxy()));

        assertEquals(event.getMessage(), resultEvent.getMessage());
        assertEquals(event.getFormattedMessage(), resultEvent.getFormattedMessage());

        assertTrue(Arrays.equals(event.getArgumentArray(), resultEvent.getArgumentArray()));

    }

    private static boolean compareKeyValuePairLists(List<KeyValuePair> leftList, List<KeyValuePair> rightList) {
        if (leftList == rightList)
            return true;

        if (leftList == null || rightList == null)
            return false;

        int length = leftList.size();
        if (rightList.size() != length) {
            System.out.println("length discrepancy");
            return false;
        }

        //System.out.println("checking KeyValuePair lists");

        for (int i = 0; i < length; i++) {
            KeyValuePair leftKVP = leftList.get(i);
            KeyValuePair rightKVP = rightList.get(i);

            boolean result = Objects.equals(leftKVP.key, rightKVP.key) && Objects.equals(leftKVP.value, rightKVP.value);

            if (!result) {
                System.out.println("mismatch oin kvp " + leftKVP + " and " + rightKVP);
                return false;
            }
        }
        return true;

    }

    @Test
    void withMarkers() throws JsonProcessingException {
        LoggingEvent event = new LoggingEvent("x", logger, Level.WARN, "hello", null, null);
        event.addMarker(markerA);
        event.addMarker(markerB);

        byte[] resultBytes = jsonEncoder.encode(event);
        String resultString = new String(resultBytes, StandardCharsets.UTF_8);
        //System.out.println(resultString);

        JsonLoggingEvent resultEvent = stringToLoggingEventMapper.mapStringToLoggingEvent(resultString);
        compareEvents(event, resultEvent);
    }

    @Test
    void withArguments() throws JsonProcessingException {
        LoggingEvent event = new LoggingEvent("x", logger, Level.WARN, "hello", null, new Object[] { "arg1", "arg2" });

        byte[] resultBytes = jsonEncoder.encode(event);
        String resultString = new String(resultBytes, StandardCharsets.UTF_8);
        //System.out.println(resultString);

        JsonLoggingEvent resultEvent = stringToLoggingEventMapper.mapStringToLoggingEvent(resultString);
        compareEvents(event, resultEvent);
    }

    @Test
    void withKeyValuePairs() throws JsonProcessingException {
        LoggingEvent event = new LoggingEvent("x", logger, Level.WARN, "hello kvp", null,
                new Object[] { "arg1", "arg2" });
        event.addKeyValuePair(new KeyValuePair("k1", "v1"));
        event.addKeyValuePair(new KeyValuePair("k2", "v2"));

        byte[] resultBytes = jsonEncoder.encode(event);
        String resultString = new String(resultBytes, StandardCharsets.UTF_8);
        //System.out.println(resultString);
        JsonLoggingEvent resultEvent = stringToLoggingEventMapper.mapStringToLoggingEvent(resultString);
        compareEvents(event, resultEvent);
    }

    @Test
    void withFormattedMessage() throws JsonProcessingException {
        LoggingEvent event = new LoggingEvent("x", logger, Level.WARN, "hello {} {}", null,
                new Object[] { "arg1", "arg2" });
        jsonEncoder.setWithFormattedMessage(true);

        byte[] resultBytes = jsonEncoder.encode(event);
        String resultString = new String(resultBytes, StandardCharsets.UTF_8);
        System.out.println(resultString);

        JsonLoggingEvent resultEvent = stringToLoggingEventMapper.mapStringToLoggingEvent(resultString);
        compareEvents(event, resultEvent);
    }

    @Test
    void withMDC() throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        map.put("a", "b");

        LoggingEvent event = new LoggingEvent("x", logger, Level.WARN, "hello kvp", null,
                new Object[] { "arg1", "arg2" });
        Map<String, String> mdcMap = new HashMap<>();
        mdcMap.put("mdcK1", "v1");
        mdcMap.put("mdcK2", "v2");

        event.setMDCPropertyMap(mdcMap);

        byte[] resultBytes = jsonEncoder.encode(event);
        String resultString = new String(resultBytes, StandardCharsets.UTF_8);
        //System.out.println(resultString);
        JsonLoggingEvent resultEvent = stringToLoggingEventMapper.mapStringToLoggingEvent(resultString);
        compareEvents(event, resultEvent);
    }

    @Test
    void withThrowable() throws JsonProcessingException {
        Throwable t = new RuntimeException("test");
        LoggingEvent event = new LoggingEvent("in withThrowable test", logger, Level.WARN, "hello kvp", t, null);

        byte[] resultBytes = jsonEncoder.encode(event);
        String resultString = new String(resultBytes, StandardCharsets.UTF_8);
        JsonLoggingEvent resultEvent = stringToLoggingEventMapper.mapStringToLoggingEvent(resultString);
        compareEvents(event, resultEvent);
    }

    @Test
    void withThrowableHavingCause() throws JsonProcessingException {
        Throwable cause = new IllegalStateException("test cause");

        Throwable t = new RuntimeException("test", cause);

        LoggingEvent event = new LoggingEvent("in withThrowableHavingCause test", logger, Level.WARN, "hello kvp", t,
                null);

        byte[] resultBytes = jsonEncoder.encode(event);
        String resultString = new String(resultBytes, StandardCharsets.UTF_8);
        //System.out.println(resultString);
        JsonLoggingEvent resultEvent = stringToLoggingEventMapper.mapStringToLoggingEvent(resultString);
        compareEvents(event, resultEvent);
    }

    @Test
    void withThrowableHavingCyclicCause() throws JsonProcessingException {
        Throwable cause = new IllegalStateException("test cause");

        Throwable t = new RuntimeException("test", cause);
        cause.initCause(t);

        LoggingEvent event = new LoggingEvent("in withThrowableHavingCyclicCause test", logger, Level.WARN, "hello kvp",
                t, null);

        byte[] resultBytes = jsonEncoder.encode(event);
        String resultString = new String(resultBytes, StandardCharsets.UTF_8);
        //System.out.println(resultString);
        JsonLoggingEvent resultEvent = stringToLoggingEventMapper.mapStringToLoggingEvent(resultString);
        compareEvents(event, resultEvent);
    }

    @Test
    void withThrowableHavingSuppressed() throws JsonProcessingException {
        Throwable suppressed = new IllegalStateException("test suppressed");

        Throwable t = new RuntimeException("test");
        t.addSuppressed(suppressed);

        LoggingEvent event = new LoggingEvent("in withThrowableHavingCause test", logger, Level.WARN, "hello kvp", t,
                null);

        byte[] resultBytes = jsonEncoder.encode(event);
        String resultString = new String(resultBytes, StandardCharsets.UTF_8);
        //System.out.println(resultString);
        JsonLoggingEvent resultEvent = stringToLoggingEventMapper.mapStringToLoggingEvent(resultString);
        compareEvents(event, resultEvent);
    }

    void configure(String file) throws JoranException {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        loggerContext.putProperty("diff", "" + diff);
        jc.doConfigure(file);
    }

    @Test
    void withJoran() throws JoranException, IOException {
        String configFilePathStr = ClassicTestConstants.JORAN_INPUT_PREFIX + "json/jsonEncoder.xml";

        configure(configFilePathStr);
        Logger logger = loggerContext.getLogger(this.getClass().getName());
        logger.addAppender(listAppender);

        logger.debug("hello");
        logbackMDCAdapter.put("a1", "v1" + diff);
        logger.atInfo().addKeyValue("ik" + diff, "iv" + diff).addKeyValue("a", "b").log("bla bla \"x\" foobar");
        logbackMDCAdapter.put("a2", "v2" + diff);
        logger.atWarn().addMarker(markerA).setMessage("some warning message").log();
        logbackMDCAdapter.remove("a2");
        logger.atError().addKeyValue("ek" + diff, "v" + diff).setCause(new RuntimeException("an error"))
                .log("some error occurred");

        //StatusPrinter.print(loggerContext);

        Path outputFilePath = Path.of(ClassicTestConstants.OUTPUT_DIR_PREFIX + "json/test-" + diff + ".json");
        List<String> lines = Files.readAllLines(outputFilePath);
        int count = 4;
        assertEquals(count, lines.size());

        for (int i = 0; i < count; i++) {
            //System.out.println("i = " + i);
            LoggingEvent withnessEvent = (LoggingEvent) listAppender.list.get(i);
            JsonLoggingEvent resultEvent = stringToLoggingEventMapper.mapStringToLoggingEvent(lines.get(i));
            compareEvents(withnessEvent, resultEvent);
        }
    }

    @Test
    void withJoranAndEnabledFormattedMessage() throws JoranException, IOException {
        String configFilePathStr =
                ClassicTestConstants.JORAN_INPUT_PREFIX + "json/jsonEncoderAndEnabledFormattedMessage.xml";

        configure(configFilePathStr);
        Logger logger = loggerContext.getLogger(this.getClass().getName());

        //StatusPrinter.print(loggerContext);
        statusChecker.isWarningOrErrorFree(0);

        logger.atError().addKeyValue("ek1", "v1").addArgument("arg1").log("this is {}");

        Path outputFilePath = Path.of(ClassicTestConstants.OUTPUT_DIR_PREFIX + "json/test-" + diff + ".json");
        List<String> lines = Files.readAllLines(outputFilePath);

        int count = 1;
        assertEquals(count, lines.size());

        String withness = "{\"sequenceNumber\":0,\"level\":\"ERROR\",\"threadName\":\"main\","
                + "\"loggerName\":\"ch.qos.logback.classic.encoder.JsonEncoderTest\",\"mdc\": {},"
                + "\"kvpList\": [{\"ek1\":\"v1\"}],\"formattedMessage\":\"this is arg1\",\"throwable\":null}";

        assertEquals(withness, lines.get(0));
    }
}