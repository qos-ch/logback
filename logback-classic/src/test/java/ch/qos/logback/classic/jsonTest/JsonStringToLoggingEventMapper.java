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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.JsonEncoder;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.PubThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonStringToLoggingEventMapper {
    IMarkerFactory markerFactory;


    public JsonStringToLoggingEventMapper(IMarkerFactory markerFactory) {
        this.markerFactory = markerFactory;
    }

    public JsonLoggingEvent mapStringToLoggingEvent(String resultString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(StackTraceElementProxy.class, new STEPDeserializer());
        module.addDeserializer(Level.class, new LevelDeserializer());
        module.addDeserializer(Marker.class, new MarkerDeserializer(markerFactory));
        module.addDeserializer(KeyValuePair.class, new KeyValuePairDeserializer());
        module.addDeserializer(LoggerContextVO.class, new LoggerContextVODeserializer());
        module.addDeserializer(PubThrowableProxy.class, new PubThrowableProxyDeserializer());

        objectMapper.registerModule(module);

        JsonNode jsonNode = objectMapper.readTree(resultString);
        JsonLoggingEvent resultEvent = objectMapper.treeToValue(jsonNode, JsonLoggingEvent.class);
        //buildLevel(jsonNode, resultEvent);

        //xbuildMarkersList(jsonNode, resultEvent);
        //xbuildKVPList(jsonNode, resultEvent);
        //buildThrowableProxy(jsonNode, resultEvent);
        return resultEvent;
    }

    private static void UNUSED_buildLevel(JsonNode jsonNode, JsonLoggingEvent resultEvent) {
        String levelStr = jsonNode.at("/"+ JsonEncoder.LEVEL_ATTR_NAME).asText();
        Level level = Level.toLevel(levelStr);
        resultEvent.level = level;
    }

    private void UNUSED_buildMarkersList(JsonNode jsonNode, JsonLoggingEvent resultEvent) {
        JsonNode markersNode = jsonNode.at("/"+JsonEncoder.MARKERS_ATTR_NAME);
        if(markersNode!=null && markersNode.isArray()) {
            List<Marker> markerList = new ArrayList<>();
            Iterator<JsonNode> itr = markersNode.iterator();
            while (itr.hasNext()) {
                JsonNode item=itr.next();
                String markerStr = item.asText();
                Marker marker = markerFactory.getMarker(markerStr);
                markerList.add(marker);
            }
            resultEvent.markerList = markerList;
        }
    }


    private void UNUSED_buildKVPList(JsonNode jsonNode, JsonLoggingEvent resultEvent) {
        JsonNode kvpNode = jsonNode.at("/"+JsonEncoder.KEY_VALUE_PAIRS_ATTR_NAME);
        if(kvpNode!=null && kvpNode.isArray()) {
            System.out.println("in buildKVPList");
            List<KeyValuePair> kvpList = new ArrayList<>();
            Iterator<JsonNode> itr = kvpNode.iterator();
            while (itr.hasNext()) {
                JsonNode item=itr.next();

                Map.Entry<String, JsonNode> entry = item.fields().next();
                String key = entry.getKey();
                String val = entry.getValue().asText();
                kvpList.add(new KeyValuePair(key, val));

             }
            resultEvent.kvpList =kvpList;
        }
    }

}
