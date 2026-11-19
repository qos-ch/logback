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

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.PubThrowableProxy;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.util.List;
import java.util.Map;

/**
 * Jackson mix-in mapping the members written by {@link ch.qos.logback.classic.encoder.JsonEncoder}
 * to the setters of {@link ch.qos.logback.classic.encoder.JsonLoggingEvent}, which carries no Jackson
 * annotation of its own.
 */
abstract class JsonLoggingEventMixIn {

    @JsonProperty("timestamp")
    abstract void setTimeStamp(long timeStamp);

    @JsonProperty("context")
    abstract void setLoggerContextVO(LoggerContextVO loggerContextVO);

    @JsonProperty("markers")
    abstract void setMarkerList(List<Marker> markerList);

    @JsonProperty("mdc")
    abstract void setMDCPropertyMap(Map<String, String> mdcPropertyMap);

    @JsonProperty("kvpList")
    abstract void setKeyValuePairs(List<KeyValuePair> keyValuePairs);

    @JsonProperty("arguments")
    abstract void setArgumentArray(Object[] argumentArray);

    @JsonProperty("throwable")
    @JsonDeserialize(as = PubThrowableProxy.class)
    abstract void setThrowableProxy(IThrowableProxy throwableProxy);

    @JsonIgnore
    abstract Map<String, String> getMdc();

    @JsonIgnore
    abstract Map<String, String> getMDCPropertyMap();
}
