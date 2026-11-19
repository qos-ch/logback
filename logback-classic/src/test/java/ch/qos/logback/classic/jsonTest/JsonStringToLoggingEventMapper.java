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


package ch.qos.logback.classic.jsonTest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.JsonLoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.PubThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

/**
 * Reads the output of {@link ch.qos.logback.classic.encoder.JsonEncoder} with Jackson, independently of
 * {@link ch.qos.logback.classic.encoder.JsonEventReader}, as a check that the output is valid JSON.
 *
 * <p>Unlike {@code JsonEventReader}, it does not rebuild the common frames of nested throwables.</p>
 */
public class JsonStringToLoggingEventMapper {
    IMarkerFactory markerFactory;
    ObjectMapper objectMapper;

    public JsonStringToLoggingEventMapper(IMarkerFactory markerFactory) {
        this.markerFactory = markerFactory;

        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.addMixIn(JsonLoggingEvent.class, JsonLoggingEventMixIn.class);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(StackTraceElementProxy.class, new STEPDeserializer());
        module.addDeserializer(Level.class, new LevelDeserializer());
        module.addDeserializer(Marker.class, new MarkerDeserializer(markerFactory));
        module.addDeserializer(KeyValuePair.class, new KeyValuePairDeserializer());
        module.addDeserializer(LoggerContextVO.class, new LoggerContextVODeserializer());
        module.addDeserializer(PubThrowableProxy.class, new PubThrowableProxyDeserializer());
        objectMapper.registerModule(module);
    }

    public JsonLoggingEvent mapStringToLoggingEvent(String resultString) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(resultString);
        return objectMapper.treeToValue(jsonNode, JsonLoggingEvent.class);
    }

    /**
     * Parse a line into a Jackson tree, failing on invalid JSON.
     */
    public JsonNode readTree(String line) throws JsonProcessingException {
        return objectMapper.readTree(line);
    }

    /**
     * Convert an argument or key-value value as {@code JsonEventReader} does.
     */
    static Object nodeToArgument(JsonNode node) {
        if (node == null || node.isNull())
            return null;
        if (node.isBoolean())
            return node.booleanValue();
        if (node.isIntegralNumber()) {
            if (node.canConvertToInt())
                return node.intValue();
            if (node.canConvertToLong())
                return node.longValue();
            return node.bigIntegerValue();
        }
        if (node.isNumber())
            return node.doubleValue();
        if (node.isTextual())
            return node.textValue();
        return node.toString();
    }
}
