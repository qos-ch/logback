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

import ch.qos.logback.classic.encoder.JsonEncoder;
import ch.qos.logback.classic.spi.LoggerContextVO;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.event.KeyValuePair;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LoggerContextVODeserializer  extends StdDeserializer<LoggerContextVO> {

    public  LoggerContextVODeserializer() {
        this(null);
    }

    public  LoggerContextVODeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public LoggerContextVO deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JacksonException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        if(node.isObject()) {
            JsonNode nameNode = node.get(JsonEncoder.NAME_ATTR_NAME);
            String name = nameNode.asText();
            JsonNode bdayNode = node.get(JsonEncoder.BIRTHDATE_ATTR_NAME);
            long birthday = bdayNode.asLong();

            JsonNode propertiesNode = node.get(JsonEncoder.CONTEXT_PROPERTIES_ATTR_NAME);
            Map<String, String> propertiesMap = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> it = propertiesNode.fields();
            while(it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                String key = entry.getKey();
                String value = entry.getValue().asText();
                propertiesMap.put(key, value);
            }
            return  new LoggerContextVO(name, propertiesMap, birthday);
        }
        return null;
    }
}
