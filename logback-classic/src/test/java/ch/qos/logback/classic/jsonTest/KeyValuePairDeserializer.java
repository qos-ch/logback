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

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.event.KeyValuePair;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class KeyValuePairDeserializer  extends StdDeserializer<KeyValuePair>  {
    public  KeyValuePairDeserializer() {
        this(null);
    }


    public  KeyValuePairDeserializer(Class<?> vc) {
        super(vc);
    }


    @Override
    public KeyValuePair deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JacksonException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        if(node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            if(it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                String key = entry.getKey();
                String value = entry.getValue().asText();
                KeyValuePair kvp = new KeyValuePair(key, value);
                return kvp;
            }
        }

        return null;
    }
}
