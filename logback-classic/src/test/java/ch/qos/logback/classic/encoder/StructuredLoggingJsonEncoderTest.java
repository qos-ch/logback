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
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusPrinter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// When running from an IDE, add the following on the command line
//
//          --add-opens ch.qos.logback.classic/ch.qos.logback.classic.jsonTest=ALL-UNNAMED
//
class StructuredLoggingJsonEncoderTest {

    int diff = RandomUtil.getPositiveInt();

    LoggerContext loggerContext = new LoggerContext();
    Logger logger = loggerContext.getLogger(StructuredLoggingJsonEncoderTest.class);

    JsonEncoder jsonEncoder = new StructuredLoggingJsonEncoder();

    BasicMarkerFactory markerFactory = new BasicMarkerFactory();

    Marker markerA = markerFactory.getMarker("A");

    Marker markerB = markerFactory.getMarker("B");

    ListAppender<ILoggingEvent> listAppender = new ListAppender();
    JsonStringToLoggingEventMapper stringToLoggingEventMapper = new JsonStringToLoggingEventMapper(markerFactory);

    LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();

    ObjectMapper objectMapper = new ObjectMapper();

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
        Object[] args = new String[] { "logback" };
        LoggingEvent event = new LoggingEvent("x", logger, Level.WARN, "hello {}", null, args);

        byte[] resultBytes = jsonEncoder.encode(event);
        String resultString = new String(resultBytes, StandardCharsets.UTF_8);

        JsonNode json = objectMapper.readTree(resultString);
        Long timestampSeconds = json.get("timestamp").get("seconds").asLong();
        assertEquals(event.getInstant().getEpochSecond(), timestampSeconds);
        assertEquals(event.getNanoseconds(), json.get("timestamp").get("nanos").asInt());

        assertNull(json.get("timestampSeconds"));
        assertNull(json.get("timestampNanos"));
        assertNull(json.get("time"));
        assertNull(json.get("arguments"));
        assertNull(json.get("level"));
        assertEquals("WARN", json.get("severity").asText());

        
 
    }

    @Test
    void withJoranWithoutTimestampSeconds() throws JsonProcessingException, JoranException, IOException {
        String configFilePathStr = ClassicTestConstants.JORAN_INPUT_PREFIX
                + "json/structuredLoggingJsonEncoder.xml";

        configure(configFilePathStr);
        Logger logger = loggerContext.getLogger(this.getClass().getName());
        logger.addAppender(listAppender);

        logger.debug("hello {}", "logback");

        Path outputFilePath = Path.of(ClassicTestConstants.OUTPUT_DIR_PREFIX + "json/test-" + diff + ".json");
        List<String> lines = Files.readAllLines(outputFilePath);
        int count = 1;
        assertEquals(count, lines.size());
        JsonNode json = objectMapper.readTree(lines.get(0));
        ILoggingEvent event = listAppender.list.get(0);

        assertEquals(event.getInstant().getEpochSecond(), json.get("timestampSeconds").asLong());
        assertEquals(event.getInstant().getNano(), json.get("timestampNanos").asLong());

        String time = json.get("time").asText();
        Instant timeAsInstant = Instant.parse(time);
        assertEquals(event.getInstant(), timeAsInstant);
        assertEquals("hello logback", json.get("message").asText());
    }

    void configure(String file) throws JoranException {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        loggerContext.putProperty("diff", "" + diff);
        jc.doConfigure(file);
    }

}