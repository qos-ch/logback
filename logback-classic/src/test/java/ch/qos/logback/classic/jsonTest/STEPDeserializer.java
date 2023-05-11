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

package ch.qos.logback.classic.jsonTest;

import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;

import java.io.IOException;

public class STEPDeserializer extends StdDeserializer<StackTraceElementProxy> {

    public  STEPDeserializer() {
        this(null);
    }

    public  STEPDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public StackTraceElementProxy deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String className = node.get("className").asText();
        String methodName = node.get("methodName").asText();
        String fileName = node.get("fileName").asText();

        int lineNumber = (Integer) ((IntNode) node.get("lineNumber")).numberValue();

        StackTraceElement ste = new StackTraceElement(className, methodName, fileName, lineNumber);
        return new StackTraceElementProxy(ste);
    }
}
