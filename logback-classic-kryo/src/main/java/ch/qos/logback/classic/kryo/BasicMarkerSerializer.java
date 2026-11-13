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

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarker;
import org.slf4j.helpers.BasicMarkerFactory;

public class BasicMarkerSerializer extends Serializer<BasicMarker> {
    BasicMarkerFactory basicMarkerFactory = new BasicMarkerFactory();

    @Override
    public void write(Kryo kryo, Output output, BasicMarker marker) {
        output.writeString(marker.getName());
    }

    @Override
    public BasicMarker read(Kryo kryo, Input input, Class<? extends BasicMarker> type) {
        String name = input.readString();
        BasicMarker marker = (BasicMarker) basicMarkerFactory.getMarker(name);
        return marker;
    }
}
