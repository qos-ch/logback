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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;

public class MarkerDeserializer extends StdDeserializer<Marker> {

    IMarkerFactory markerFactory;

    public  MarkerDeserializer(IMarkerFactory markerFactory) {
        this(null, markerFactory);
    }

    public  MarkerDeserializer(Class<?> vc, IMarkerFactory markerFactory) {
        super(vc);
        this.markerFactory = markerFactory;
    }

    @Override
    public Marker deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String markerStr = node.asText();
        Marker marker = markerFactory.getMarker(markerStr);
        return marker;
    }
}
